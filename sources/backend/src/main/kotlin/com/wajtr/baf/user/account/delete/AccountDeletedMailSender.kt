package com.wajtr.baf.user.account.delete

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
 * Service for sending account deletion confirmation emails.
 * 
 * This email is sent as a confirmation and security measure to inform users that their
 * account has been permanently deleted, allowing them to take action if the deletion
 * was not made by them.
 */
@Service
class AccountDeletedMailSender(
    private val emailSender: EmailSender,
    private val emailTemplateService: EmailTemplateService
) {
    private val log = LoggerFactory.getLogger(AccountDeletedMailSender::class.java)

    /**
     * Sends an account deletion confirmation email to the user.
     *
     * @param emailAddress The email address to send the notification to
     * @param locale The locale for the email template and date formatting
     * @param zoneId The timezone for formatting the deletion date/time
     * @return Returns true if the email was successfully sent, returns false if there was a problem
     */
    fun sendAccountDeletedNotification(emailAddress: String, locale: Locale, zoneId: ZoneId): Boolean {

        log.info("Sending account deletion confirmation to $emailAddress")

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(locale)
            .withZone(zoneId)
        val deletionDateTime = formatter.format(Instant.now())

        val model = mapOf(
            "emailAddress" to emailAddress,
            "deletionDateTime" to deletionDateTime,
            "applicationName" to i18n("application.title")
        )

        return try {
            val htmlContent = emailTemplateService.processTemplate("account-deleted", model, locale)
            val subject = i18n("email.account.deleted.subject")
            emailSender.sendEmail(emailAddress, subject, htmlContent)
        } catch (e: Exception) {
            log.error("Failed to send account deletion confirmation to $emailAddress", e)
            false
        }
    }
}
