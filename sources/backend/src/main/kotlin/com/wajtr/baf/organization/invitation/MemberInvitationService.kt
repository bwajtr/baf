package com.wajtr.baf.organization.invitation

import com.wajtr.baf.authentication.AuthenticatedTenant
import com.wajtr.baf.core.commons.HttpServletUtils
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.core.tenants.TenantRepository
import com.wajtr.baf.organization.member.MemberManagementService
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.organization.member.UserRoleTenant
import com.wajtr.baf.organization.member.UserRoleTenantRepository
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.User
import com.wajtr.baf.user.validation.EmailValidator
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

const val ACCEPT_INVITATION_PAGE = "accept-invitation"

sealed class InviteMembersResult {
    data class Success(val invitationIds: List<UUID>) : InviteMembersResult()
    data class ValidationError(val messageKey: String, val parameter: String? = null) : InviteMembersResult()
}

sealed class AcceptInvitationResult {
    data object Success : AcceptInvitationResult()
    data class Error(val messageKey: String) : AcceptInvitationResult()
}

sealed class UpdateInvitationRoleResult {
    data object Success : UpdateInvitationRoleResult()
    data class Error(val messageKey: String) : UpdateInvitationRoleResult()
}

sealed class ResendInvitationResult {
    data object Success : ResendInvitationResult()
    data class Error(val messageKey: String) : ResendInvitationResult()
}

@Service
@Transactional
class MemberInvitationService(
    private val memberInvitationRepository: MemberInvitationRepository,
    private val userRoleTenantRepository: UserRoleTenantRepository,
    private val memberManagementService: MemberManagementService,
    private val identity: Identity,
    private val tenantRepository: TenantRepository,
    private val invitationMailSender: InvitationMailSender
) {

    private val logger = LoggerFactory.getLogger(MemberInvitationService::class.java)

    @PreAuthorize("hasAnyRole(${UserRole.OWNER_ROLE}, ${UserRole.ADMIN_ROLE})")
    fun inviteMembers(emailsInput: String, role: String): InviteMembersResult {

        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")
        val currentUser = identity.authenticatedUser

        // Parse and normalize emails
        val emailAddresses = emailsInput.split(",", ";", "\n")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        if (emailAddresses.isEmpty()) {
            return InviteMembersResult.ValidationError("members.invite.dialog.emails.required")
        }

        // Validate email format and check for duplicates
        val emailValidator = EmailValidator()
        for (emailAddress in emailAddresses) {
            if (!emailValidator.isValid(emailAddress)) {
                return InviteMembersResult.ValidationError("members.invite.dialog.email.invalid", emailAddress)
            }
            if (memberInvitationRepository.emailAlreadyInvited(emailAddress)) {
                return InviteMembersResult.ValidationError("members.invite.dialog.email.already.invited",emailAddress)
            }
            if (memberInvitationRepository.emailAlreadyMemberOfCurrentTenant(emailAddress)) {
                return InviteMembersResult.ValidationError("members.invite.dialog.email.already.member", emailAddress)
            }
        }

        // Create invitations
        val invitationIds = emailAddresses.map { emailAddress ->
            memberInvitationRepository.createInvitation(
                email = emailAddress,
                role = role,
                tenantId = tenant.id,
                invitedBy = currentUser.id
            )
        }

        sendInvitationEmailsToRecipients(tenant, currentUser, emailAddresses, invitationIds, role)

        return InviteMembersResult.Success(invitationIds)
    }

    private fun sendInvitationEmailsToRecipients(
        tenant: AuthenticatedTenant,
        currentUser: User,
        emailAddresses: List<String>,
        invitationIds: List<UUID>,
        role: String
    ) {
        // Get tenant details for the email
        val tenantDetails = tenantRepository.findById(tenant.id)
        val organizationName = tenantDetails?.organizationName ?: i18n("email.organization.personal")
        val inviterName = currentUser.name

        // Send invitation emails
        emailAddresses.forEachIndexed { index, email ->
            val invitationId = invitationIds[index]
            val acceptanceUrl = HttpServletUtils.getServerBaseUrl() + "/$ACCEPT_INVITATION_PAGE/$invitationId"

            val emailSent = invitationMailSender.sendInvitationEmail(
                emailAddress = email,
                acceptanceUrl = acceptanceUrl,
                inviterName = inviterName,
                organizationName = organizationName,
                role = role
            )

            if (emailSent) {
                logger.info("Sent invitation email for invitation $invitationId to $email")
            } else {
                logger.warn("Failed to send invitation email for invitation $invitationId to $email")
            }
        }
    }

    fun getAllInvitations() = memberInvitationRepository.getAllInvitations()

    fun deleteInvitationById(invitationId: UUID) = memberInvitationRepository.deleteInvitationById(invitationId)

    fun acceptInvitation(invitationId: UUID): AcceptInvitationResult {
        // Fetch the invitation
        val invitation = memberInvitationRepository.getInvitationForAcceptance(invitationId)
            ?: return AcceptInvitationResult.Error("invitation.accept.not.found")

        // Verify that current user's email matches invitation email
        val currentUserEmail = identity.authenticatedUser.email
        if (!currentUserEmail.equals(invitation.email, ignoreCase = true)) {
            return AcceptInvitationResult.Error("invitation.accept.email.mismatch")
        }

        // Check if user is already a member of the target tenant
        val currentUserId = identity.authenticatedUser.id
        if (userRoleTenantRepository.isUserMemberOfTenant(currentUserId, invitation.tenantId)) {
            // User is already a member, clean up the invitation
            memberInvitationRepository.deleteInvitationById(invitationId)
            return AcceptInvitationResult.Error("invitation.accept.already.member")
        }

        // Create entry in app_user_role_tenant
        userRoleTenantRepository.insertRole(
            UserRoleTenant(
                userId = currentUserId,
                role = invitation.role,
                tenantId = invitation.tenantId
            )
        )

        // Delete the invitation
        memberInvitationRepository.deleteInvitationById(invitationId)

        return AcceptInvitationResult.Success
    }

    @PreAuthorize("hasAnyRole(${UserRole.OWNER_ROLE}, ${UserRole.ADMIN_ROLE})")
    fun updateInvitationRole(invitationId: UUID, newRole: String): UpdateInvitationRoleResult {
        // Check if invitation exists
        memberInvitationRepository.getInvitationById(invitationId)
            ?: return UpdateInvitationRoleResult.Error("invitation.details.not.found")

        // Validate that the new role is in the allowed roles for invitations
        val allowedRoles = memberManagementService.getAllowedRolesForInvitation()
        if (newRole !in allowedRoles) {
            return UpdateInvitationRoleResult.Error("invitation.details.role.not.allowed")
        }

        // Update the role
        memberInvitationRepository.updateRole(invitationId, newRole)

        return UpdateInvitationRoleResult.Success
    }

    @PreAuthorize("hasAnyRole(${UserRole.OWNER_ROLE}, ${UserRole.ADMIN_ROLE})")
    fun resendInvitation(invitationId: UUID): ResendInvitationResult {
        val tenant = identity.authenticatedTenant
            ?: return ResendInvitationResult.Error("invitation.resend.no.tenant")

        // Fetch the invitation details
        val invitation = memberInvitationRepository.getInvitationById(invitationId)
            ?: return ResendInvitationResult.Error("invitation.details.not.found")

        // Get tenant details for the email
        val tenantDetails = tenantRepository.findById(tenant.id)
        val organizationName = tenantDetails?.organizationName ?: i18n("email.organization.personal")
        val inviterName = invitation.invitedByName ?: i18n("invitation.resend.inviter.unknown")

        // Build acceptance URL
        val acceptanceUrl = HttpServletUtils.getServerBaseUrl() + "/$ACCEPT_INVITATION_PAGE/$invitationId"

        // Send the invitation email
        val emailSent = invitationMailSender.sendInvitationEmail(
            emailAddress = invitation.email,
            acceptanceUrl = acceptanceUrl,
            inviterName = inviterName,
            organizationName = organizationName,
            role = invitation.role
        )

        if (!emailSent) {
            logger.warn("Failed to resend invitation email for invitation $invitationId to ${invitation.email}")
            return ResendInvitationResult.Error("invitation.resend.email.failed")
        }

        // Update the last invitation sent time
        memberInvitationRepository.updateLastInvitationSentTime(invitationId)

        logger.info("Resent invitation email for invitation $invitationId to ${invitation.email}")
        return ResendInvitationResult.Success
    }

}