package com.wajtr.baf.ui.views.user.login

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.dom.Element
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.OptionalParameter
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.wajtr.baf.authentication.db.EmailNotVerifiedException
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.components.ApplicationView
import com.wajtr.baf.ui.security.LOGIN_PATH
import com.wajtr.baf.ui.views.user.common.UserAccountRelatedBaseLayout
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.WebAttributes
import java.io.Serializable

/**
 *
 * @author Bretislav Wajtr
 */

@Route(LOGIN_PATH, layout = UserAccountRelatedBaseLayout::class)
@AnonymousAllowed
class LoginPage(
    private val clientRegistrationRepository: ClientRegistrationRepository?,
    private val request: HttpServletRequest? = null,
    // TODO fix when bruteforcing is implemented
    // private val suspiciousClientsService: SuspiciousClientsService,
) : ApplicationView(), Serializable, HasUrlParameter<String> {
    // TODO remember me hidden input - currently not used
//    private lateinit var keepMeSignedForTodayCheck: Checkbox
    private lateinit var messageLabel: Paragraph
    private lateinit var emailField: EmailField
    private lateinit var passwordField: PasswordField

    init {
        val loginFormContent = createLoginFormContent()

        val formElement = Element("form")
        formElement.setAttribute("id", "loginform")
        formElement.setAttribute("method", "post")
        formElement.setAttribute("action", "/accounts/login")

        formElement.appendChild(loginFormContent.element)

        element.appendChild(formElement)
    }

    @Suppress("UNCHECKED_CAST")
    private fun createLoginFormContent(): VerticalLayout {
        return VerticalLayout().apply {
            maxWidth = "290px"
            isPadding = false
            themeList.remove("spacing")
            themeList.add("spacing-xs")

            h3(i18n("user.login.welcome.message")) {
                style.set("margin-bottom", "0")
                style.set("margin-top", "4px")
            }

            messageLabel = p()

            emailField = emailField(i18n("user.login.email")) {
                width = "290px"
                element.setAttribute("name", "email")
                setId("name")
            }

            passwordField = passwordField(i18n("user.login.password")) {
                width = "290px"
                isRevealButtonVisible = false
                element.setAttribute("name", "password")
                setId("password")
            }


            div {
                width = "100%"

                // TODO remember me hidden input - currently not used
//                val rememberMeHiddenInput = HiddenInput("remember-me").apply {
//                    value = getRememberMeCookieValue().toString()
//                }
//                add(rememberMeHiddenInput)

//                keepMeSignedForTodayCheck =
//                    checkBox(i18n("user.login.keep_me_signed_in_for_today")) {
//                        value = getRememberMeCookieValue()
//                        style.set("font-size", "small")
//                        style.set("max-width", "160px")
//                        style.set("float", "left")
//                        addValueChangeListener {
//                            setRememberMeCookieValue(it.value)
//                            rememberMeHiddenInput.value = it.value.toString()
//                        }
//                    }

                // TODO password reset not used
//                routerLink(
//                    text = i18n("user.login.forgot.password"),
//                    viewType = PasswordResetPreparationView::class
//                ) {
//                    style.set("font-size", "small")
//                    style.set("float", "right")
//                }
            }

            // TODO bowser hidden inputs - currently not used
//            add(BowserHiddenInputs())

            // TODO recaptcha component - currently not used
//            if (suspiciousClientsService.isSuspiciousClient(CoreContext.getCurrentRequest())) {
//                reCaptchaComponent = reCaptchaComponentFactory.createRecaptchaComponentForHtmlForm()
//                add(reCaptchaComponent)
//            }

            button(i18n("user.login.signin")) {
                style.set("margin-top", "15px")
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                setId("submitbutton")
            }

            if (hasOAuth2Clients()) {
                verticalLayout(false) {
                    alignItems = FlexComponent.Alignment.CENTER

                    horizontalLayout {
                        setWidthFull()
                        style.set("margin-top", "2rem")
                        style.set("margin-bottom", "0.5rem")
                        alignItems = FlexComponent.Alignment.CENTER

                        hr { flexGrow = 1.0 }
                        span(i18n("user.login.oauth2.clients.header.text"))
                        hr { flexGrow = 1.0 }
                    }

                    if (clientRegistrationRepository != null && clientRegistrationRepository is Iterable<*>) {
                        for (registration in clientRegistrationRepository as Iterable<ClientRegistration>) {
                            val clientName = registration.clientName
                            button(i18n("user.login.signin.with.oauth2.client", clientName)) {
                                addThemeVariants(ButtonVariant.AURA_ACCENT)
                                onClick {
                                    UI.getCurrent().page.setLocation("/oauth2/authorization/${registration.registrationId}")
                                }
                            }
                        }
                    }
                }
            }


            // We submit the form so a good old POST is issued and the whole Spring Security authentication&autohrization filter chain have a chance to
            // initialize the session.
            UI.getCurrent()
                .page.executeJs("document.getElementById('submitbutton').addEventListener('click', () => document.getElementById('loginform').submit());")
        }
    }

    private fun showLoginError() {
        // TODO update this when captcha is implemented
//        if (this.reCaptchaComponent != null) {
//            showErrorMessage(i18n("user.login.failure.incl.captcha"))
//        } else {
        showErrorMessage(getErrorExplanation())
//        }
        emailField.clear()
        passwordField.clear()
    }

    fun getErrorExplanation(): String {
        val session = request?.getSession(false)
        // this attribute is set by the SimpleUrlAuthenticationFailureHandler which is configured
        // by default to redirect back to the login page with a ?error parameter in URL
        val authenticationException = session?.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) as Exception?
        return if (authenticationException != null) {
            messageByAuthException(authenticationException)
        } else "Unknown login error"
    }

    private fun messageByAuthException(authenticationException: Exception): String {
        return when (authenticationException) {
            is UsernameNotFoundException -> authenticationException.message ?: "User not found"
            is BadCredentialsException -> i18n("user.login.failure")
            is EmailNotVerifiedException -> i18n("user.login.failure.email.not.verified")
            else -> "Unknown login error: " + authenticationException.javaClass.simpleName
        }
    }

    override fun setParameter(
        event: BeforeEvent,
        @OptionalParameter parameter: String?
    ) {
        when (event.location.queryParameters.queryString) {
            "error" -> showLoginError()
            "inactivity" -> showErrorMessage(i18n("user.login.loggedout.inactivity"))
            "confirmSuccess" -> showSuccessMessage(i18n("user.login.emailverified"))
            "confirmPasswordResetSuccess" -> showSuccessMessage(i18n("user.login.passwordupdated"))

            else -> clearMessage()
        }
    }

    private fun clearMessage() {
        messageLabel.text = ""
    }

    private fun showSuccessMessage(message: String) {
        messageLabel.style.set("color", "green")
        messageLabel.text = message
    }

    private fun showErrorMessage(message: String) {
        messageLabel.style.set("color", "red")
        messageLabel.text = message
    }

    private fun hasOAuth2Clients(): Boolean {
        // will not be null if there are some spring.security.oauth2.client.* properties defined in
        // application properties. We have an example configuration defined in application-development.properties
        // that is activated when you run the app with the "development" profile (jvm parameter -Dspring.profiles.active=development)
        return clientRegistrationRepository != null
    }
}
