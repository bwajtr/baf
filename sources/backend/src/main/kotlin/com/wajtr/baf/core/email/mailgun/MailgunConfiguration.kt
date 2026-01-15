package com.wajtr.baf.core.email.mailgun

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.client.MailgunClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration properties for Mailgun email sending.
 *
 * @author Bretislav Wajtr
 */
@ConfigurationProperties(prefix = "mailgun")
data class MailgunProperties(
    /** Whether email sending is enabled. When false, emails are logged but not sent. */
    val enabled: Boolean = false,
    /** Mailgun API key */
    val apiKey: String = "",
    /** Mailgun sending domain */
    val domain: String = "",
    /** Email address to use in the From field */
    val fromEmail: String = "",
    /** Optional base URL for Mailgun API (use EU URL for EU accounts) */
    val baseUrl: String? = null
)

/**
 * Configuration for Mailgun email sending.
 *
 * @author Bretislav Wajtr
 */
@Configuration
@EnableConfigurationProperties(MailgunProperties::class)
class MailgunConfiguration(private val properties: MailgunProperties) {

    /**
     * Creates the Mailgun Messages API client.
     * Returns null if Mailgun is not enabled or not configured.
     */
    @Bean
    fun mailgunMessagesApi(): MailgunMessagesApi? {
        if (!properties.enabled) {
            return null
        }
        
        if (properties.apiKey.isBlank() || properties.domain.isBlank()) {
            return null
        }

        val config = if (!properties.baseUrl.isNullOrBlank()) {
            MailgunClient.config(properties.baseUrl, properties.apiKey)
        } else {
            MailgunClient.config(properties.apiKey)
        }
        
        return config.createApi(MailgunMessagesApi::class.java)
    }
}
