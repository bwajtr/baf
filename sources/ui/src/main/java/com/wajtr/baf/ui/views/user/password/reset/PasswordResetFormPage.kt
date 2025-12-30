package com.wajtr.baf.ui.views.user.password.reset


import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.textAlign
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.validator.StringLengthValidator
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.wajtr.baf.authentication.db.LOGIN_PATH
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.components.ApplicationPage
import com.wajtr.baf.ui.vaadin.extensions.bindMutableProperty
import com.wajtr.baf.ui.views.user.common.UserAccountRelatedBaseLayout
import com.wajtr.baf.user.password.reset.PASSWORD_RESET_PAGE
import com.wajtr.baf.user.password.reset.PasswordResetResult
import com.wajtr.baf.user.password.reset.PasswordResetService
import com.wajtr.baf.user.validation.ValidPasswordConstants
import java.io.Serializable


/**
 * A form for submitting an email for which the password should be reset.
 *
 * @author Bretislav Wajtr
 */

data class ResetPasswordFormData(
    var token: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    var recaptchaResponse: String? = null
)

@Route(PASSWORD_RESET_PAGE, layout = UserAccountRelatedBaseLayout::class)
@AnonymousAllowed
class PasswordResetFormPage(
    private val passwordResetService: PasswordResetService,
) : ApplicationPage(), Serializable {

    private val formDataBinder = Binder<ResetPasswordFormData>()
    private lateinit var tokenField: TextField
    private lateinit var newPasswordField: PasswordField
    private lateinit var confirmPasswordField: PasswordField
    private lateinit var okButton: Button
    private lateinit var form: FormLayout
    private lateinit var formLayout: VerticalLayout

    init {
        createUI()
        bindModel()
    }

    private fun createUI() {
        formLayout = verticalLayout {
            maxWidth = "400px"

            h4(i18n("users.password.reset.header") + " " + i18n("application.title")) {
                style.set("margin-bottom", "0")
                textAlign = "center"
                width = "100%"
            }

            p(i18n("users.password.reset.instruction")) {
                textAlign = "center"
            }

            form = formLayout {
                maxWidth = "300px"
                alignSelf = FlexComponent.Alignment.CENTER

                tokenField = textField(i18n("users.password.reset.recovery.code")) {
                    width = "300px"
                    valueChangeMode = ValueChangeMode.LAZY
                }

                newPasswordField = passwordField(i18n("users.password.reset.newpassword")) {
                    width = "300px"
                    valueChangeMode = ValueChangeMode.LAZY
                }

                confirmPasswordField = passwordField(i18n("users.password.reset.confirmpassword")) {
                    width = "300px"
                    valueChangeMode = ValueChangeMode.LAZY
                }

                horizontalLayout {
                    style.set("margin-top", "15px")

                    okButton = button(i18n("users.password.reset.form.button")) {
                        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                        isEnabled = false
                        onClick {
                            resetPassword()
                        }
                    }

                    button(i18n("core.ui.common.cancel")) {
                        onClick { UI.getCurrent().navigate(LOGIN_PATH) }
                    }
                }
            }
        }
    }

    private fun bindModel() {
        formDataBinder.forField(tokenField)
            .asRequired(i18n("users.password.reset.token.required"))
            .withConverter(
                { it.trim() },
                { it }) // to trim value when saving to model, but leave unchanged when loading
            .bindMutableProperty(ResetPasswordFormData::token)

        formDataBinder.forField(newPasswordField)
            .asRequired(i18n("users.password.reset.password.required"))
            .withValidator(
                StringLengthValidator(
                    i18n(
                        "users.password.reset.password.size",
                        ValidPasswordConstants.MIN_LENGTH
                    ), ValidPasswordConstants.MIN_LENGTH, null
                )
            )
            .bindMutableProperty(ResetPasswordFormData::password)


        val confirmPasswordFieldBinder = formDataBinder.forField(confirmPasswordField)
            .asRequired(i18n("users.password.reset.confirm.password.required"))
            .withValidator(
                StringLengthValidator(
                    i18n(
                        "users.password.reset.confirm.password.size",
                        ValidPasswordConstants.MIN_LENGTH
                    ), ValidPasswordConstants.MIN_LENGTH, null
                )
            )
            .withValidator(
                { it == newPasswordField.value },
                i18n("users.password.reset.confirm.password.dontmatch")
            )
            .bindMutableProperty(ResetPasswordFormData::confirmPassword)

        newPasswordField.addValueChangeListener {
            if (confirmPasswordField.value.isNotEmpty()) {
                confirmPasswordFieldBinder.validate()
            }
        }

        formDataBinder.addStatusChangeListener { event ->
            okButton.isEnabled = event.binder.isValid
        }
    }

    private fun resetPassword() {
        val model = ResetPasswordFormData()
        if (formDataBinder.writeBeanIfValid(model)) {
            // strip whitespace characters including non-breaking space (trim() does not trim non-breaking spaces)
            // based on https://stackoverflow.com/a/28295733/1237636
            val tokenStr: String = model.token.replace(Regex("(^\\h*)|(\\h*$)"), "")

            val passwordResetResult = passwordResetService.performPasswordReset(tokenStr, model.password)
            when (passwordResetResult) {
                PasswordResetResult.INVALID_TOKEN -> {
                    tokenField.isInvalid = true
                    tokenField.errorMessage = i18n("users.password.reset.token.rejected")
                }

                PasswordResetResult.PASSWORD_CHANGED ->  // password was changed -> redirect to login with proper message
                    UI.getCurrent().page.setLocation("$LOGIN_PATH?confirmPasswordResetSuccess")
            }
        }
    }

}
