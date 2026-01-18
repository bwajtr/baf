package com.wajtr.baf.core.email

/**
 * Interface for sending emails.
 * 
 * Implementations can send emails via different providers (Brevo, SMTP, etc.)
 * or save them to the filesystem for development/testing.
 *
 * @author Bretislav Wajtr
 */
interface EmailSender {
    
    /**
     * Sends an email with both HTML and plain text content.
     * 
     * @param to The recipient email address
     * @param subject The email subject
     * @param htmlContent The HTML content of the email
     * @param textContent The plain text content of the email (for clients that don't support HTML)
     * @return true if the email was sent successfully, false on error
     */
    fun sendEmail(to: String, subject: String, htmlContent: String, textContent: String): Boolean
}
