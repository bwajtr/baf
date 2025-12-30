package com.wajtr.baf.ui.views.user.registration

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.Autocomplete
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.validator.EmailValidator
import com.vaadin.flow.data.validator.StringLengthValidator
import com.vaadin.flow.data.value.ValueChangeMode
import com.wajtr.baf.authentication.db.DatabaseBasedAuthenticationProvider
import com.wajtr.baf.authentication.db.EmailNotVerifiedException
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.components.ApplicationView
import com.wajtr.baf.ui.vaadin.extensions.bindMutableProperty
import com.wajtr.baf.ui.vaadin.extensions.ensureSessionTimeZoneIsSet
import com.wajtr.baf.ui.views.legal.PUBLIC_PRIVACY_POLICY_VIEW
import com.wajtr.baf.ui.views.legal.PUBLIC_TERMS_OF_SERVICE_VIEW
import com.wajtr.baf.ui.views.user.emailverification.VERIFY_EMAIL_VIEW
import com.wajtr.baf.user.registration.UserRegistrationService
import com.wajtr.baf.user.validation.ValidPasswordConstants
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import java.io.Serializable

/**
 *
 * @author Bretislav Wajtr
 */
data class RegistrationFormData(
    var fullName: String = "",
    var email: String = "",
    var password: String = "",
    var termsOfServiceConsent: Boolean = false,
    var recaptchaResponse: String? = null
)

abstract class AbstractRegistrationView(
    protected val databaseBasedAuthenticationProvider: DatabaseBasedAuthenticationProvider,
    protected val userRegistrationService: UserRegistrationService
) : ApplicationView(), Serializable {

    protected val formDataBinder = Binder<RegistrationFormData>()

    protected lateinit var fullNameField: TextField
    protected lateinit var emailTextField: EmailField
    protected lateinit var passwordField: PasswordField
    protected lateinit var termsOfServiceConsentCheckbox: Checkbox
    protected lateinit var okButton: Button
    protected lateinit var form: FormLayout
    protected lateinit var formLayout: VerticalLayout


    protected abstract fun createUI()

    protected abstract fun processRegistration(regData: RegistrationFormData)

    override fun onAttach(attachEvent: AttachEvent?) {
        UI.getCurrent().ensureSessionTimeZoneIsSet {
            createUI()
        }
    }

    protected fun createRegistrationFormComponent(): FormLayout {
        return FormLayout().apply {
            maxWidth = "300px"

            fullNameField = textField(i18n("users.registration.fullname")) {
                width = "300px"
                valueChangeMode = ValueChangeMode.LAZY
            }

            emailTextField = emailField(i18n("users.registration.email")) {
                width = "300px"
                valueChangeMode = ValueChangeMode.LAZY
                autocomplete = Autocomplete.USERNAME
                isClearButtonVisible = true
            }

            passwordField = passwordField(i18n("users.registration.password")) {
                width = "300px"
                autocomplete = Autocomplete.NEW_PASSWORD
                valueChangeMode = ValueChangeMode.LAZY
            }

            termsOfServiceConsentCheckbox = checkBox {
                this.setLabelComponent(
                    htmlSpan(
                        i18n(
                            "users.registration.consent.termsOfService",
                            PUBLIC_TERMS_OF_SERVICE_VIEW, PUBLIC_PRIVACY_POLICY_VIEW
                        )
                    )
                )
            }

            horizontalLayout {
                style.set("margin-top", "15px")
                justifyContentMode = FlexComponent.JustifyContentMode.CENTER

                okButton = button(i18n("users.registration.button")) {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE)
                    isEnabled = false
                    onClick {
                        registerUser()
                    }
                }

            }
        }
    }

    protected fun bindModel() {
        formDataBinder.forField(fullNameField)
            .asRequired(i18n("users.registration.fullname.required"))
            .withValidator(
                StringLengthValidator(
                    i18n(
                        "users.registration.fullname.max.length",
                        100
                    ), null, 100
                )
            )
            .withConverter(
                { it.trim() },
                { it }) // to trim value when saving to model, but leave unchanged when loading
            .bindMutableProperty(RegistrationFormData::fullName)

        formDataBinder.forField(emailTextField)
            .asRequired(i18n("users.registration.email.required"))
            .withValidator(EmailValidator(i18n("users.registration.email.valid")))
            .withValidator(
                StringLengthValidator(
                    i18n(
                        "users.registration.email.max.length",
                        100
                    ), null, 100
                )
            )
            .withConverter(
                { it.trim() },
                { it }) // to trim value when saving to model, but leave unchanged when loading
            .bindMutableProperty(RegistrationFormData::email)


        formDataBinder.forField(passwordField)
            .asRequired(i18n("users.registration.password.required"))
            .withValidator(
                StringLengthValidator(
                    i18n(
                        "users.registration.password.length",
                        ValidPasswordConstants.MIN_LENGTH
                    ), ValidPasswordConstants.MIN_LENGTH, null
                )
            )
            .bindMutableProperty(RegistrationFormData::password)

        formDataBinder.forField(termsOfServiceConsentCheckbox)
            .asRequired(i18n("users.registration.consent.termsOfService.required"))
            .bindMutableProperty(RegistrationFormData::termsOfServiceConsent)

        formDataBinder.addStatusChangeListener { event ->
            okButton.isEnabled = event.binder.isValid
        }
    }

    private fun registerUser() {
        val model = RegistrationFormData()
        if (formDataBinder.writeBeanIfValid(model)) {
            processRegistration(model)
        }
    }

    protected fun performLogin(
        email: String,
        password: String
    ) {
        try {
            val authentication: Authentication = databaseBasedAuthenticationProvider.authenticate(
                UsernamePasswordAuthenticationToken(email, password)
            )
            SecurityContextHolder.getContext().authentication = authentication
            UI.getCurrent().page.setLocation("/")
        } catch (loginFailedException: AuthenticationException) { // Note that it authentication may fail - for example if email verifications are active then it'll definitely fail at this point
            if (loginFailedException is EmailNotVerifiedException) {
                SecurityContextHolder.clearContext()
                UI.getCurrent().page.setLocation("$VERIFY_EMAIL_VIEW/$email")
            } else throw loginFailedException
        }
    }

}
