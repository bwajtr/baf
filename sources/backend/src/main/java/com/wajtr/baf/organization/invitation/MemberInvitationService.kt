package com.wajtr.baf.organization.invitation

import com.wajtr.baf.core.commons.HttpServletUtils
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.user.Identity
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

@Service
@Transactional
class MemberInvitationService(
    private val memberInvitationRepository: MemberInvitationRepository,
    private val identity: Identity
) {

    private val logger = LoggerFactory.getLogger(MemberInvitationService::class.java)

    @PreAuthorize("hasAnyRole(${UserRole.OWNER_ROLE}, ${UserRole.ADMIN_ROLE})")
    fun inviteMembers(emailsInput: String, role: String): InviteMembersResult {

        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")
        val currentUser = identity.authenticatedUser

        // Parse and normalize emails
        val emails = emailsInput.split(",", ";", "\n")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        if (emails.isEmpty()) {
            return InviteMembersResult.ValidationError("members.invite.dialog.emails.required")
        }

        // Validate email format and check for duplicates
        val emailValidator = EmailValidator()
        for (email in emails) {
            if (!emailValidator.isValid(email)) {
                return InviteMembersResult.ValidationError("members.invite.dialog.email.invalid", email)
            }
            if (memberInvitationRepository.emailAlreadyInvited(email)) {
                return InviteMembersResult.ValidationError("members.invite.dialog.email.already.invited",email)
            }
            if (memberInvitationRepository.emailAlreadyMemberOfCurrentTenant(email)) {
                return InviteMembersResult.ValidationError("members.invite.dialog.email.already.member", email)
            }
        }

        // Create invitations
        val invitationIds = emails.map { email ->
            memberInvitationRepository.createInvitation(
                email = email,
                role = role,
                tenantId = tenant.id,
                invitedBy = currentUser.id
            )
        }

        invitationIds.forEach { id ->
            val acceptanceUrl = HttpServletUtils.getServerBaseUrl() + "/$ACCEPT_INVITATION_PAGE/" + id
            logger.info("Created invitation $id. Url is $acceptanceUrl")
        }

        return InviteMembersResult.Success(invitationIds)
    }

    fun getAllInvitations() = memberInvitationRepository.getAllInvitations()

    fun deleteInvitationById(invitationId: UUID) = memberInvitationRepository.deleteInvitationById(invitationId)

}