package com.wajtr.baf.organization.invitation

import com.wajtr.baf.db.jooq.Tables.APP_USER
import com.wajtr.baf.db.jooq.Tables.MEMBER_INVITATION
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

@Service
@Transactional
class MemberInvitationService(
    private val dslContext: DSLContext
) {

    fun getAllInvitations(): List<MemberInvitation> {
        return dslContext.select(
            MEMBER_INVITATION.ID,
            MEMBER_INVITATION.EMAIL,
            MEMBER_INVITATION.ROLE,
        )
            .from(MEMBER_INVITATION)
            .fetch { record ->
                MemberInvitation(
                    id = record.get(MEMBER_INVITATION.ID),
                    email = record.get(MEMBER_INVITATION.EMAIL),
                    role = record.get(MEMBER_INVITATION.ROLE),
                )
            }
    }

    fun deleteInvitation(invitationId: UUID): Int {
        return dslContext.deleteFrom(MEMBER_INVITATION)
            .where(MEMBER_INVITATION.ID.eq(invitationId))
            .execute()
    }

    fun createInvitation(email: String, role: String, tenantId: UUID, invitedBy: UUID): UUID {
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
            .execute()
    }

}