package com.wajtr.baf.organization.invitation

import com.wajtr.baf.core.email.EmailSender
import com.wajtr.baf.core.email.EmailTemplateService
import com.wajtr.baf.core.i18n.i18n
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service for sending member invitation emails.
 *
 * @author Bretislav Wajtr
 */
@Service
class InvitationMailSender(
    private val emailSender: EmailSender,
    private val emailTemplateService: EmailTemplateService
) {

    private val log = LoggerFactory.getLogger(InvitationMailSender::class.java)

    /**
     * Sends an invitation email to a potential new member.
     *
     * @param emailAddress The email address to send the invitation to
     * @param acceptanceUrl The URL the user should click to accept the invitation
     * @param inviterName The name of the person who sent the invitation
     * @param organizationName The name of the organization the user is being invited to
     * @param role The role the user will have in the organization (e.g., "ROLE_ADMIN")
     * @param locale Optional locale for the email template (defaults to request locale)
     * @return Returns true if the email was successfully sent, returns false if there was a problem
     */
    fun sendInvitationEmail(
        emailAddress: String,
        acceptanceUrl: String,
        inviterName: String,
        organizationName: String,
        role: String,
        locale: Locale? = null
    ): Boolean {

        log.info("Sending invitation email to $emailAddress for organization '$organizationName'")

        // Convert role like "ROLE_ADMIN" to "ADMIN" for i18n lookup
        val roleKey = role.removePrefix("ROLE_")
        val localizedRole = i18n("role.$roleKey")

        val model = mapOf(
            "acceptanceUrl" to acceptanceUrl,
            "inviterName" to inviterName,
            "organizationName" to organizationName,
            "role" to localizedRole
        )

        return try {
            val htmlContent = emailTemplateService.processTemplate("member-invitation", model, locale)
            val subject = i18n("email.invitation.subject", organizationName)
            emailSender.sendEmail(emailAddress, subject, htmlContent)
        } catch (e: Exception) {
            log.error("Failed to send invitation email to $emailAddress", e)
            false
        }
    }
}
