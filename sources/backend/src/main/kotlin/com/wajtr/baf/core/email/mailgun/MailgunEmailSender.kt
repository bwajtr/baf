package com.wajtr.baf.core.email.mailgun

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.model.message.Message
import com.mailgun.util.EmailUtil
import com.wajtr.baf.core.email.EmailSender
import com.wajtr.baf.core.i18n.i18n
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/**
 * Email sender implementation using Mailgun API.
 *
 * This bean is only created when mailgun.enabled=true.
 * When disabled, LocalFilePreviewEmailSender is used instead.
 *
 * @author Bretislav Wajtr
 */
@Service
@ConditionalOnProperty(name = ["mailgun.enabled"], havingValue = "true")
class MailgunEmailSender(
    private val mailgunMessagesApi: MailgunMessagesApi?,
    private val mailgunProperties: MailgunProperties
) : EmailSender {

    private val log = LoggerFactory.getLogger(MailgunEmailSender::class.java)

    override fun sendEmail(to: String, subject: String, htmlContent: String): Boolean {
        if (mailgunMessagesApi == null) {
            log.error("Mailgun is enabled but API client is not configured. Check mailgun.apiKey and mailgun.domain properties.")
            return false
        }

        return try {
            val message = Message.builder()
                .from(EmailUtil.nameWithEmail(getFromName(), mailgunProperties.fromEmail))
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build()

            val response = mailgunMessagesApi.sendMessage(mailgunProperties.domain, message)
            log.info("Email sent successfully to $to, messageId: ${response.id}, message: ${response.message}")
            true
        } catch (e: Exception) {
            log.error("Failed to send email to $to with subject '$subject'", e)
            false
        }
    }

    /**
     * Gets the display name for the From field.
     * Uses the application title from i18n messages.
     */
    private fun getFromName(): String {
        return try {
            i18n("application.title")
        } catch (_: Exception) {
            "Application"
        }
    }
}