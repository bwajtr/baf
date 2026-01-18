package com.wajtr.baf.core.email.brevo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuration properties for Brevo (formerly Sendinblue) email sending.
 *
 * @author Bretislav Wajtr
 */
@ConfigurationProperties(prefix = "brevo")
data class BrevoProperties(
    /** Whether Brevo email sending is enabled. When false, emails are not sent via Brevo. */
    val enabled: Boolean = false,
    /** Brevo API key (starts with 'xkeysib-') */
    val apiKey: String = "",
    /** Email address to use in the From field */
    val senderEmail: String = "",
    /** Display name to use in the From field (optional) */
    val senderName: String = "",

    /** Optional base URL for Brevo API (default: https://api.brevo.com) */
    val baseUrl: String = "https://api.brevo.com"
)

/**
 * Configuration for Brevo email sending.
 *
 * @author Bretislav Wajtr
 */
@Configuration
@EnableConfigurationProperties(BrevoProperties::class)
class BrevoConfiguration
