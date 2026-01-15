package com.wajtr.baf.core.email

/**
 * Interface for sending emails.
 * 
 * Implementations can send emails via different providers (Mailgun, SMTP, etc.)
 * or save them to the filesystem for development/testing.
 *
 * @author Bretislav Wajtr
 */
interface EmailSender {
    
    /**
     * Sends an email with HTML content.
     * 
     * @param to The recipient email address
     * @param subject The email subject
     * @param htmlContent The HTML content of the email
     * @return true if the email was sent successfully, false on error
     */
    fun sendEmail(to: String, subject: String, htmlContent: String): Boolean
}
