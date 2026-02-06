package com.wajtr.baf.organization.invitation

import com.wajtr.baf.db.jooq.tables.references.*
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.user.Identity
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

data class MemberInvitation(
    val id: UUID,
    val email: String,
    val role: String,
)

data class MemberInvitationDetails(
    val id: UUID,
    val email: String,
    val role: String,
    val createdAt: OffsetDateTime,
    val invitedByName: String?,
)

data class InvitationAcceptanceDetails(
    val id: UUID,
    val email: String,
    val role: String,
    val tenantId: UUID,
    val organizationName: String,
    val invitedByName: String?,
)

@Service
@Transactional
class MemberInvitationRepository(
    private val dslContext: DSLContext,
    private val identity: Identity
) {

    fun getAllInvitations(): List<MemberInvitation> {
        return dslContext.select(
            TENANT_MEMBER_INVITATION.ID,
            TENANT_MEMBER_INVITATION.EMAIL,
            TENANT_MEMBER_INVITATION.ROLE,
        )
            .from(TENANT_MEMBER_INVITATION)
            .where(TENANT_MEMBER_INVITATION.TENANT_ID.eq(identity.authenticatedTenant?.id))
            .fetch { record ->
                MemberInvitation(
                    id = record.get(TENANT_MEMBER_INVITATION.ID)!!,
                    email = record.get(TENANT_MEMBER_INVITATION.EMAIL)!!,
                    role = record.get(TENANT_MEMBER_INVITATION.ROLE)!!,
                )
            }
    }

    fun deleteInvitationById(invitationId: UUID): Int {
        return dslContext.deleteFrom(TENANT_MEMBER_INVITATION)
            .where(TENANT_MEMBER_INVITATION.ID.eq(invitationId))
            .execute()
    }

    fun createInvitation(email: String, role: String, tenantId: UUID, invitedBy: UUID): UUID {
        // invitation with the OWNER_ROLE can be created only if current user is OWNER
        if (role == UserRole.OWNER_ROLE && !identity.hasRole(UserRole.OWNER_ROLE)) {
            throw IllegalArgumentException("Only owners can invite new owners")
        }

        return dslContext.insertInto(TENANT_MEMBER_INVITATION)
            .set(TENANT_MEMBER_INVITATION.EMAIL, email.lowercase().trim())
            .set(TENANT_MEMBER_INVITATION.ROLE, role)
            .set(TENANT_MEMBER_INVITATION.TENANT_ID, tenantId)
            .set(TENANT_MEMBER_INVITATION.INVITED_BY, invitedBy)
            .returning(TENANT_MEMBER_INVITATION.ID)
            .fetchOne()!!
            .get(TENANT_MEMBER_INVITATION.ID)!!
    }

    fun emailAlreadyInvited(email: String): Boolean {
        return dslContext.fetchExists(
            dslContext.selectFrom(TENANT_MEMBER_INVITATION)
                .where(TENANT_MEMBER_INVITATION.EMAIL.equalIgnoreCase(email.trim()))
                .and((TENANT_MEMBER_INVITATION.TENANT_ID.eq(identity.authenticatedTenant?.id)))
        )
    }

    fun emailAlreadyMemberOfCurrentTenant(email: String): Boolean {
        val tenantId = identity.authenticatedTenant?.id ?: return false
        return dslContext.fetchExists(
            dslContext.select()
                .from(USER_ACCOUNT)
                .join(TENANT_MEMBER).on(USER_ACCOUNT.ID.eq(TENANT_MEMBER.USER_ID))
                .where(USER_ACCOUNT.EMAIL.equalIgnoreCase(email.trim()))
                .and(TENANT_MEMBER.TENANT_ID.eq(tenantId))
        )
    }

    fun getInvitationById(invitationId: UUID): MemberInvitationDetails? {
        return dslContext.select(
            TENANT_MEMBER_INVITATION.ID,
            TENANT_MEMBER_INVITATION.EMAIL,
            TENANT_MEMBER_INVITATION.ROLE,
            TENANT_MEMBER_INVITATION.CREATED_AT,
            USER_ACCOUNT.NAME
        )
            .from(TENANT_MEMBER_INVITATION)
            .leftJoin(USER_ACCOUNT).on(TENANT_MEMBER_INVITATION.INVITED_BY.eq(USER_ACCOUNT.ID))
            .where(TENANT_MEMBER_INVITATION.ID.eq(invitationId))
            .and((TENANT_MEMBER_INVITATION.TENANT_ID.eq(identity.authenticatedTenant?.id)))
            .fetchOne { record ->
                MemberInvitationDetails(
                    id = record.get(TENANT_MEMBER_INVITATION.ID)!!,
                    email = record.get(TENANT_MEMBER_INVITATION.EMAIL)!!,
                    role = record.get(TENANT_MEMBER_INVITATION.ROLE)!!,
                    createdAt = record.get(TENANT_MEMBER_INVITATION.CREATED_AT)!!,
                    invitedByName = record.get(USER_ACCOUNT.NAME),
                )
            }
    }

    fun updateRole(invitationId: UUID, role: String): Int {
        return dslContext.update(TENANT_MEMBER_INVITATION)
            .set(TENANT_MEMBER_INVITATION.ROLE, role)
            .where(TENANT_MEMBER_INVITATION.ID.eq(invitationId))
            .and((TENANT_MEMBER_INVITATION.TENANT_ID.eq(identity.authenticatedTenant?.id)))
            .execute()
    }

    fun updateLastInvitationSentTime(invitationId: UUID): Int {
        return dslContext.update(TENANT_MEMBER_INVITATION)
            .set(TENANT_MEMBER_INVITATION.LAST_INVITATION_SENT_TIME, OffsetDateTime.now())
            .where(TENANT_MEMBER_INVITATION.ID.eq(invitationId))
            .and(TENANT_MEMBER_INVITATION.TENANT_ID.eq(identity.authenticatedTenant?.id))
            .execute()
    }

    fun getInvitationForAcceptance(invitationId: UUID): InvitationAcceptanceDetails? {
        return dslContext.select(
            TENANT_MEMBER_INVITATION.ID,
            TENANT_MEMBER_INVITATION.EMAIL,
            TENANT_MEMBER_INVITATION.ROLE,
            TENANT_MEMBER_INVITATION.TENANT_ID,
            TENANT.ORGANIZATION_NAME,
            USER_ACCOUNT.NAME
        )
            .from(TENANT_MEMBER_INVITATION)
            .join(TENANT).on(TENANT_MEMBER_INVITATION.TENANT_ID.eq(TENANT.ID))
            .leftJoin(USER_ACCOUNT).on(TENANT_MEMBER_INVITATION.INVITED_BY.eq(USER_ACCOUNT.ID))
            .where(TENANT_MEMBER_INVITATION.ID.eq(invitationId))
            .fetchOne { record ->
                InvitationAcceptanceDetails(
                    id = record.get(TENANT_MEMBER_INVITATION.ID)!!,
                    email = record.get(TENANT_MEMBER_INVITATION.EMAIL)!!,
                    role = record.get(TENANT_MEMBER_INVITATION.ROLE)!!,
                    tenantId = record.get(TENANT_MEMBER_INVITATION.TENANT_ID)!!,
                    organizationName = record.get(TENANT.ORGANIZATION_NAME)!!,
                    invitedByName = record.get(USER_ACCOUNT.NAME),
                )
            }
    }

}