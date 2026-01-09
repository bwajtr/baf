package com.wajtr.baf.organization.invitation

import com.wajtr.baf.db.jooq.Tables.*
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
            MEMBER_INVITATION.ID,
            MEMBER_INVITATION.EMAIL,
            MEMBER_INVITATION.ROLE,
        )
            .from(MEMBER_INVITATION)
            .where(MEMBER_INVITATION.TENANT_ID.eq(identity.authenticatedTenant?.id))
            .fetch { record ->
                MemberInvitation(
                    id = record.get(MEMBER_INVITATION.ID),
                    email = record.get(MEMBER_INVITATION.EMAIL),
                    role = record.get(MEMBER_INVITATION.ROLE),
                )
            }
    }

    fun deleteInvitationById(invitationId: UUID): Int {
        return dslContext.deleteFrom(MEMBER_INVITATION)
            .where(MEMBER_INVITATION.ID.eq(invitationId))
            .execute()
    }

    fun createInvitation(email: String, role: String, tenantId: UUID, invitedBy: UUID): UUID {
        // invitation with the OWNER_ROLE can be created only if current user is OWNER
        if (role == UserRole.OWNER_ROLE && !identity.hasRole(UserRole.OWNER_ROLE)) {
            throw IllegalArgumentException("Only owners can invite new owners")
        }

        return dslContext.insertInto(MEMBER_INVITATION)
            .set(MEMBER_INVITATION.EMAIL, email.lowercase().trim())
            .set(MEMBER_INVITATION.ROLE, role)
            .set(MEMBER_INVITATION.TENANT_ID, tenantId)
            .set(MEMBER_INVITATION.INVITED_BY, invitedBy)
            .returning(MEMBER_INVITATION.ID)
            .fetchOne()!!
            .get(MEMBER_INVITATION.ID)
    }

    fun emailAlreadyInvited(email: String): Boolean {
        return dslContext.fetchExists(
            dslContext.selectFrom(MEMBER_INVITATION)
                .where(MEMBER_INVITATION.EMAIL.equalIgnoreCase(email.trim()))
                .and((MEMBER_INVITATION.TENANT_ID.eq(identity.authenticatedTenant?.id)))
        )
    }

    fun emailAlreadyMemberOfCurrentTenant(email: String): Boolean {
        val tenantId = identity.authenticatedTenant?.id ?: return false
        return dslContext.fetchExists(
            dslContext.select()
                .from(APP_USER)
                .join(APP_USER_ROLE_TENANT).on(APP_USER.ID.eq(APP_USER_ROLE_TENANT.USER_ID))
                .where(APP_USER.EMAIL.equalIgnoreCase(email.trim()))
                .and(APP_USER_ROLE_TENANT.TENANT_ID.eq(tenantId))
        )
    }

    fun getInvitationById(invitationId: UUID): MemberInvitationDetails? {
        return dslContext.select(
            MEMBER_INVITATION.ID,
            MEMBER_INVITATION.EMAIL,
            MEMBER_INVITATION.ROLE,
            MEMBER_INVITATION.CREATED_AT,
            APP_USER.NAME
        )
            .from(MEMBER_INVITATION)
            .leftJoin(APP_USER).on(MEMBER_INVITATION.INVITED_BY.eq(APP_USER.ID))
            .where(MEMBER_INVITATION.ID.eq(invitationId))
            .and((MEMBER_INVITATION.TENANT_ID.eq(identity.authenticatedTenant?.id)))
            .fetchOne { record ->
                MemberInvitationDetails(
                    id = record.get(MEMBER_INVITATION.ID),
                    email = record.get(MEMBER_INVITATION.EMAIL),
                    role = record.get(MEMBER_INVITATION.ROLE),
                    createdAt = record.get(MEMBER_INVITATION.CREATED_AT),
                    invitedByName = record.get(APP_USER.NAME),
                )
            }
    }

    fun updateRole(invitationId: UUID, role: String): Int {
        return dslContext.update(MEMBER_INVITATION)
            .set(MEMBER_INVITATION.ROLE, role)
            .where(MEMBER_INVITATION.ID.eq(invitationId))
            .and((MEMBER_INVITATION.TENANT_ID.eq(identity.authenticatedTenant?.id)))
            .execute()
    }

    fun getInvitationForAcceptance(invitationId: UUID): InvitationAcceptanceDetails? {
        return dslContext.select(
            MEMBER_INVITATION.ID,
            MEMBER_INVITATION.EMAIL,
            MEMBER_INVITATION.ROLE,
            MEMBER_INVITATION.TENANT_ID,
            TENANT.ORGANIZATION_NAME,
            APP_USER.NAME
        )
            .from(MEMBER_INVITATION)
            .join(TENANT).on(MEMBER_INVITATION.TENANT_ID.eq(TENANT.ID))
            .leftJoin(APP_USER).on(MEMBER_INVITATION.INVITED_BY.eq(APP_USER.ID))
            .where(MEMBER_INVITATION.ID.eq(invitationId))
            .fetchOne { record ->
                InvitationAcceptanceDetails(
                    id = record.get(MEMBER_INVITATION.ID),
                    email = record.get(MEMBER_INVITATION.EMAIL),
                    role = record.get(MEMBER_INVITATION.ROLE),
                    tenantId = record.get(MEMBER_INVITATION.TENANT_ID),
                    organizationName = record.get(TENANT.ORGANIZATION_NAME),
                    invitedByName = record.get(APP_USER.NAME),
                )
            }
    }

}