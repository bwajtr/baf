package com.wajtr.baf.user.password.reset

import com.wajtr.baf.core.email.EmailSender
import com.wajtr.baf.core.email.EmailTemplateService
import com.wajtr.baf.core.i18n.i18n
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.Assert
import java.util.*

/**
 * A Service for sending "password reset" emails
 *
 * @author Bretislav Wajtr
 */
@Service
class PasswordResetMailSender(
    private val emailSender: EmailSender,
    private val emailTemplateService: EmailTemplateService
) {

    private val log = LoggerFactory.getLogger(PasswordResetMailSender::class.java)

    /**
     * Sends a password reset email to the user.
     * 
     * @param emailAddress The email address to send the reset link to
     * @param passwordResetUrl The URL the user should click to reset their password
     * @param resetToken The reset token (for logging purposes)
     * @param locale Optional locale for the email template (defaults to request locale)
     * @return Returns true if the email was successfully sent, returns false if there was a problem
     */
    fun sendPasswordResetEmail(
        emailAddress: String,
        passwordResetUrl: String,
        resetToken: String,
        locale: Locale? = null
    ): Boolean {
        Assert.hasText(passwordResetUrl, "Reset URL must not be empty, the user would get email with no URL otherwise!")
        Assert.hasText(resetToken, "There is no point sending a reset email with no token")

        log.info("Sending password reset email to $emailAddress")

        val model = mapOf(
            "resetUrl" to passwordResetUrl,
            "expirationDurationMinutes" to PASSWORD_RESET_TOKEN_EXPIRY_MINUTES
        )

        return try {
            val htmlContent = emailTemplateService.processTemplate("password-reset", model, locale)
            val subject = i18n("email.password.reset.subject")
            emailSender.sendEmail(emailAddress, subject, htmlContent)
        } catch (e: Exception) {
            log.error("Failed to send password reset email to $emailAddress", e)
            false
        }
    }
}