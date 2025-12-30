package com.wajtr.baf.ui.views.user.emailverification

import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.textAlign
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.router.*
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.wajtr.baf.authentication.db.LOGIN_PATH
import com.wajtr.baf.core.ApplicationProperties
import com.wajtr.baf.core.commons.HttpServletUtils
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.components.ApplicationPage
import com.wajtr.baf.ui.components.ErrorMessageComponent
import com.wajtr.baf.ui.views.user.common.UserAccountRelatedBaseLayout
import com.wajtr.baf.user.emailverification.EmailVerificationService
import com.wajtr.baf.user.emailverification.UserEmailVerificationStatus
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.io.Serializable

const val VERIFY_EMAIL_PAGE = "accounts/verify"
const val EMAIL_VERIFY_PAGE_SUBTITLE_SESSION_ATTR = "emailVerifyPageSubtitle"

/**
 *
 * @author Bretislav Wajtr
 */
@Route(VERIFY_EMAIL_PAGE, layout = UserAccountRelatedBaseLayout::class)
@AnonymousAllowed
class EmailVerificationPage(
    private val emailVerificationService: EmailVerificationService,
    private val applicationProperties: ApplicationProperties
) : ApplicationPage(), BeforeEnterObserver, Serializable, HasUrlParameter<String> {

    private lateinit var instructionLabel: Paragraph
    private var emailToVerify: String? = null

    fun initUI() {
        textAlign = "center"

        h3(i18n("users.emailverification.persisted.header"))

        val pageSubtitle = getPageSubtitleText()
        if (pageSubtitle != null) {
            p(pageSubtitle)
        }

        instructionLabel = p {
            html(
                i18n(
                    "users.emailverification.persisted.firstsend",
                    getEmailToVerify() ?: i18n("users.emailverification.persisted.nowhere")
                )
            )
        }

        image("images/email-checked.png")

        h5(i18n("users.emailverification.persisted.didntreceive"))

        div {
            span {
                html(i18n("users.emailverification.persisted.sendagain.pre"))
            }

            button(i18n("users.emailverification.persisted.sendagain.button")) {
                addThemeVariants(ButtonVariant.LUMO_SMALL)
                style.set("margin-left", "5px")
                isEnabled = getEmailToVerify() != null

                onClick {
                    val baseUrl = HttpServletUtils.getServerBaseUrl()
                    val emailToVerify = getEmailToVerify()
                    if (emailToVerify != null) {
                        val status: UserEmailVerificationStatus = emailVerificationService
                            .startEmailVerificationProcess(emailToVerify, baseUrl, true)
                        when {
                            status === UserEmailVerificationStatus.ALREADY_VERIFIED -> {
                                // email was already verified previously, just continue to application
                                UI.getCurrent().page.setLocation("/")
                            }

                            status === UserEmailVerificationStatus.EMAIL_SENDING_FAILED -> {
                                displayVerificationEmailSendingFailedMessage(
                                    getEmailToVerify() ?: i18n("users.emailverification.persisted.nowhere")
                                )
                            }

                            else -> {
                                instructionLabel.apply {
                                    removeAll()
                                    html(
                                        i18n(
                                            "users.emailverification.persisted.sendagain",
                                            getEmailToVerify() ?: i18n("users.emailverification.persisted.nowhere")
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        p(i18n("users.emailverification.persisted.contact", applicationProperties.appSupportEmailAddress))
    }

    private fun getEmailToVerify(): String? {
        return this.emailToVerify
    }

    private fun getPageSubtitleText(): String? {
        return VaadinSession.getCurrent().getAttribute(EMAIL_VERIFY_PAGE_SUBTITLE_SESSION_ATTR) as String?
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        val emailToVerify = getEmailToVerify()
        if (emailToVerify != null) {
            try {
                val baseUrl = HttpServletUtils.getServerBaseUrl()
                val status: UserEmailVerificationStatus =
                    emailVerificationService.startEmailVerificationProcess(
                        emailToVerify, baseUrl, false
                    )
                if (status === UserEmailVerificationStatus.ALREADY_VERIFIED) { // email was already verified previously, just continue to application
                    return event.rerouteTo("")
                } else if (status === UserEmailVerificationStatus.EMAIL_SENDING_FAILED) {
                    displayVerificationEmailSendingFailedMessage(emailToVerify)
                }
            } catch (_: UsernameNotFoundException) {
                // if such user does not exist, then simply redirect to Login
                UI.getCurrent().page.setLocation(LOGIN_PATH)
            }
        }
    }

    private fun displayVerificationEmailSendingFailedMessage(emailToVerify: String) {
        this.removeAll()
        val subtitle =
            getPageSubtitleText() ?: i18n("users.emailverification.persisted.failure.normal.subtitle")
        this.add(
            ErrorMessageComponent(
                i18n("users.emailverification.persisted.failure.header"),
                subtitle,
                i18n(
                    "users.emailverification.persisted.failure.message",
                    emailToVerify,
                    applicationProperties.appSupportEmailAddress
                )
            )
        )
    }

    override fun setParameter(event: BeforeEvent, parameter: String?) {
        this.emailToVerify = parameter
        initUI()
    }

}
