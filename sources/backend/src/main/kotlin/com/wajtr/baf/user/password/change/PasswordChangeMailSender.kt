package com.wajtr.baf.user.password.change

import com.wajtr.baf.core.email.EmailSender
import com.wajtr.baf.core.email.EmailTemplateService
import com.wajtr.baf.core.i18n.i18n
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

/**
 * Service for sending password change notification emails.
 * 
 * This email is sent as a security measure to inform users that their password
 * has been changed, allowing them to take action if the change was not made by them.
 *
 * @author Bretislav Wajtr
 */
@Service
class PasswordChangeMailSender(
    private val emailSender: EmailSender,
    private val emailTemplateService: EmailTemplateService
) {
    private val log = LoggerFactory.getLogger(PasswordChangeMailSender::class.java)

    /**
     * Sends a password change notification email to the user.
     *
     * @param emailAddress The email address to send the notification to
     * @param locale Optional locale for the email template and date formatting (defaults to request locale)
     * @return Returns true if the email was successfully sent, returns false if there was a problem
     */
    fun sendPasswordChangedNotification(emailAddress: String, locale: Locale, zoneId: ZoneId): Boolean {

        log.info("Sending password change notification to $emailAddress")

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(locale)
            .withZone(zoneId)
        val changeDateTime = formatter.format(Instant.now())

        val model = mapOf(
            "emailAddress" to emailAddress,
            "changeDateTime" to changeDateTime
        )

        return try {
            val htmlContent = emailTemplateService.processTemplate("password-changed", model, locale)
            val textContent = emailTemplateService.processPlainTextTemplate("password-changed", model, locale)
            val subject = i18n("email.password.changed.subject")
            emailSender.sendEmail(emailAddress, subject, htmlContent, textContent)
        } catch (e: Exception) {
            log.error("Failed to send password change notification to $emailAddress", e)
            false
        }
    }
}
