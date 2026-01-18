package com.wajtr.baf.core.email.brevo

import com.wajtr.baf.core.email.EmailSender
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

/**
 * Email sender implementation using Brevo (formerly Sendinblue) API.
 *
 * This bean is only created when brevo.enabled=true.
 * Uses Brevo's Transactional Email API v3.
 *
 * @see <a href="https://developers.brevo.com/reference/sendtransacemail">Brevo API Documentation</a>
 * @author Bretislav Wajtr
 */
@Service
@ConditionalOnProperty(name = ["brevo.enabled"], havingValue = "true")
class BrevoEmailSender(
    private val brevoProperties: BrevoProperties
) : EmailSender {

    private val log = LoggerFactory.getLogger(BrevoEmailSender::class.java)

    private val restClient: RestClient = RestClient.builder()
        .baseUrl(brevoProperties.baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("api-key", brevoProperties.apiKey)
        .build()

    override fun sendEmail(to: String, subject: String, htmlContent: String): Boolean {
        if (brevoProperties.apiKey.isBlank()) {
            log.error("Brevo is enabled but API key is not configured. Check brevo.api-key property.")
            return false
        }

        if (brevoProperties.senderEmail.isBlank()) {
            log.error("Brevo is enabled but from-email is not configured. Check brevo.from-email property.")
            return false
        }

        return try {
            val request = BrevoEmailRequest(
                sender = BrevoSender(
                    email = brevoProperties.senderEmail,
                    name = brevoProperties.senderName
                ),
                to = listOf(BrevoRecipient(email = to)),
                subject = subject,
                htmlContent = htmlContent
            )

            val response = restClient.post()
                .uri("/v3/smtp/email")
                .body(request)
                .retrieve()
                .body(BrevoEmailResponse::class.java)

            log.info("Email sent successfully to $to via Brevo, messageId: ${response?.messageId}")
            true
        } catch (e: RestClientResponseException) {
            log.error(
                "Failed to send email to $to with subject '$subject'. Status: ${e.statusCode}, Response: ${e.responseBodyAsString}",
                e
            )
            false
        } catch (e: Exception) {
            log.error("Failed to send email to $to with subject '$subject'", e)
            false
        }
    }
}


/**
 * Request body for Brevo transactional email API.
 */
private data class BrevoEmailRequest(
    val sender: BrevoSender,
    val to: List<BrevoRecipient>,
    val subject: String,
    val htmlContent: String
)

/**
 * Sender information for Brevo email.
 */
private data class BrevoSender(
    val email: String,
    val name: String
)

/**
 * Recipient information for Brevo email.
 */
private data class BrevoRecipient(
    val email: String,
    val name: String? = null
)

/**
 * Response from Brevo transactional email API.
 */
private data class BrevoEmailResponse(
    val messageId: String? = null
)
