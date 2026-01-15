package com.wajtr.baf.user.emailverification

import com.wajtr.baf.core.email.EmailSender
import com.wajtr.baf.core.email.EmailTemplateService
import com.wajtr.baf.core.i18n.i18n
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * A Service for sending "verification URL" emails: A mail is sent to user is sent, which contains URL (or clickable button) ->
 * visiting such URL is enough to verify the users ownership of the email address.
 *
 * @author Bretislav Wajtr
 */
@Service
class EmailVerificationMailSender(
    private val emailSender: EmailSender,
    private val emailTemplateService: EmailTemplateService
) {
    private val log = LoggerFactory.getLogger(EmailVerificationMailSender::class.java)

    /**
     * Sends a verification email to the user.
     * 
     * @param emailAddress The email address to send the verification to
     * @param verificationUrl The URL the user should click to verify their email
     * @param locale Optional locale for the email template (defaults to request locale)
     * @return Returns true if the email was successfully sent, returns false if there was a problem sending the email
     */
    fun sendVerificationMail(emailAddress: String, verificationUrl: String, locale: Locale? = null): Boolean {

        log.info("Sending email verification to $emailAddress")
        
        val model = mapOf(
            "verificationUrl" to verificationUrl
        )
        
        return try {
            val htmlContent = emailTemplateService.processTemplate("email-verification", model, locale)
            val subject = i18n("email.verification.subject")
            emailSender.sendEmail(emailAddress, subject, htmlContent)
        } catch (e: Exception) {
            log.error("Failed to send verification email to $emailAddress", e)
            false
        }
    }
}
