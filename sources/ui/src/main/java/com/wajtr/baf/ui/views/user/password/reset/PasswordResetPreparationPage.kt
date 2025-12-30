package com.wajtr.baf.ui.views.user.password.reset

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.validator.EmailValidator
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.wajtr.baf.authentication.db.LOGIN_PATH
import com.wajtr.baf.core.ApplicationProperties
import com.wajtr.baf.core.commons.HttpServletUtils
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.components.ApplicationPage
import com.wajtr.baf.ui.components.ErrorMessageComponent
import com.wajtr.baf.ui.vaadin.extensions.bindMutableProperty
import com.wajtr.baf.ui.views.user.common.UserAccountRelatedBaseLayout
import com.wajtr.baf.ui.views.user.emailverification.EMAIL_VERIFY_PAGE_SUBTITLE_SESSION_ATTR
import com.wajtr.baf.ui.views.user.emailverification.VERIFY_EMAIL_PAGE
import com.wajtr.baf.user.password.reset.PASSWORD_RESET_PREPARATION_PAGE
import com.wajtr.baf.user.password.reset.PasswordResetInitiationResult
import com.wajtr.baf.user.password.reset.PasswordResetService
import java.io.Serializable

/**
 * A form for submitting an email for which the password should be reset.
 *
 * @author Bretislav Wajtr
 */
data class ResetPasswordPreparationFormData(var email: String = "")

@Route(PASSWORD_RESET_PREPARATION_PAGE, layout = UserAccountRelatedBaseLayout::class)
@AnonymousAllowed
class PasswordResetPreparationPage(
    private val passwordResetService: PasswordResetService,
    private val applicationProperties: ApplicationProperties
) : ApplicationPage(), Serializable {

    private val formDataBinder = Binder<ResetPasswordPreparationFormData>()
    private lateinit var emailTextField: EmailField
    private lateinit var okButton: Button
    private lateinit var form: FormLayout
    private lateinit var formLayout: VerticalLayout


    init {
        createUI()
        bindModel()
    }

    private fun createUI() {
        formLayout = verticalLayout {
            maxWidth = "330px"

            h1(i18n("users.password.reset.prep.header") + " " + i18n("application.title")) {
                style.set("margin-bottom", "1rem")
            }

            form = formLayout {
                emailTextField = emailField(i18n("users.password.reset.prep.enter.email")) {
                    width = "300px"
                    valueChangeMode = ValueChangeMode.LAZY
                    element.setAttribute("name", "email")
                    isClearButtonVisible = true
                }

                horizontalLayout {
                    style.set("margin-top", "15px")

                    okButton = button(i18n("users.password.reset.prep.send.email")) {
                        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                        isEnabled = false
                        onClick {
                            sendEmailAndDisplayConfirmationMessage()
                        }
                    }

                    // TODO test this
                    button(i18n("core.ui.common.cancel")) {
                        onClick { UI.getCurrent().navigate(LOGIN_PATH) }
                    }
                }
            }
        }
    }

    private fun bindModel() {
        formDataBinder.forField(emailTextField)
            .asRequired(i18n("users.password.reset.prep.email.required"))
            .withValidator(EmailValidator(i18n("users.password.reset.prep.email.invalid")))
            .withConverter(
                { it.trim() },
                { it }) // to trim value when saving to model, but leave unchanged when loading
            .bindMutableProperty(ResetPasswordPreparationFormData::email)
        formDataBinder.addStatusChangeListener { event ->
            okButton.isEnabled = event.binder.isValid
        }

    }

    private fun sendEmailAndDisplayConfirmationMessage() {
        val model = ResetPasswordPreparationFormData()
        if (formDataBinder.writeBeanIfValid(model)) {
            val baseUrl = HttpServletUtils.getServerBaseUrl()
            val result: PasswordResetInitiationResult =
                passwordResetService.initiatePasswordResetProcess(model.email, baseUrl)

            when (result) {
                PasswordResetInitiationResult.EMAIL_NOT_VERIFIED -> {
                    // TODO test this
                    VaadinSession.getCurrent().setAttribute(
                        EMAIL_VERIFY_PAGE_SUBTITLE_SESSION_ATTR,
                        i18n("users.password.reset.cannotResetUntilVerified")
                    )
                    UI.getCurrent().navigate("$VERIFY_EMAIL_PAGE/${model.email}")
                }

                PasswordResetInitiationResult.EMAIL_SENDING_FAILED -> {
                    this.removeAll()
                    this.add(
                        ErrorMessageComponent(
                            i18n("users.password.reset.mailsendfailure.header"),
                            i18n("users.password.reset.mailsendfailure.subheader"),
                            i18n(
                                "users.password.reset.mailsendfailure.message",
                                model.email,
                                applicationProperties.appSupportEmailAddress
                            )
                        )
                    )
                }

                else -> {
                    // indicate success (although the preparation process might fail silently on the background - this is
                    // a security measure so we do not leak information about existing user accounts in the system)
                    form.isVisible = false
                    formLayout.apply {
                        p(i18n("users.password.reset.prep.information"))
                    }
                }
            }
        }
    }

}
