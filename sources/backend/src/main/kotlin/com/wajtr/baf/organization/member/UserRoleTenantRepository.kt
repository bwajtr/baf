package com.wajtr.baf.organization.member

import com.wajtr.baf.db.jooq.tables.references.APP_USER_ROLE_TENANT
import com.wajtr.baf.db.jooq.tables.references.TENANT
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


data class UserRoleTenant(
    val userId: UUID,
    val role: String,
    val tenantId: UUID
)

data class TenantIdAndName(
    val tenantId: UUID,
    val organizationName: String
)

@Service
@Transactional
class UserRoleTenantRepository(
    private val dslContext: DSLContext
) {

    fun insertRole(userRoleTenant: UserRoleTenant) {
        dslContext.insertInto(APP_USER_ROLE_TENANT)
            .set(APP_USER_ROLE_TENANT.USER_ID, userRoleTenant.userId)
            .set(APP_USER_ROLE_TENANT.ROLE, userRoleTenant.role)
            .set(APP_USER_ROLE_TENANT.TENANT_ID, userRoleTenant.tenantId)
            .execute()
    }

    fun getTenantsWhereUserIsOwner(userId: UUID): List<TenantIdAndName> {
        return dslContext.select(TENANT.ID, TENANT.ORGANIZATION_NAME)
            .from(APP_USER_ROLE_TENANT)
            .join(TENANT).on(TENANT.ID.eq(APP_USER_ROLE_TENANT.TENANT_ID))
            .where(APP_USER_ROLE_TENANT.USER_ID.eq(userId))
            .and(APP_USER_ROLE_TENANT.ROLE.eq(UserRole.OWNER_ROLE))
            .fetch { record ->
                TenantIdAndName(
                    tenantId = record.get(TENANT.ID)!!,
                    organizationName = record.get(TENANT.ORGANIZATION_NAME)!!
                )
            }
    }

    fun getUserIdsForTenant(tenantId: UUID): List<UUID> {
        return dslContext.selectDistinct(APP_USER_ROLE_TENANT.USER_ID)
            .from(APP_USER_ROLE_TENANT)
            .where(APP_USER_ROLE_TENANT.TENANT_ID.eq(tenantId))
            .fetchInto(UUID::class.java)
    }

    fun getRolesForUserInTenant(userId: UUID, tenantId: UUID): List<String> {
        return dslContext.select(APP_USER_ROLE_TENANT.ROLE)
            .from(APP_USER_ROLE_TENANT)
            .where(APP_USER_ROLE_TENANT.USER_ID.eq(userId))
            .and(APP_USER_ROLE_TENANT.TENANT_ID.eq(tenantId))
            .fetchInto(String::class.java)
    }

    fun isUserMemberOfTenant(userId: UUID, tenantId: UUID): Boolean {
        return dslContext.fetchExists(
            dslContext.selectFrom(APP_USER_ROLE_TENANT)
                .where(APP_USER_ROLE_TENANT.USER_ID.eq(userId))
                .and(APP_USER_ROLE_TENANT.TENANT_ID.eq(tenantId))
        )
    }

    fun countOwnersInTenant(tenantId: UUID): Int {
        return dslContext.selectCount()
            .from(APP_USER_ROLE_TENANT)
            .where(APP_USER_ROLE_TENANT.TENANT_ID.eq(tenantId))
            .and(APP_USER_ROLE_TENANT.ROLE.eq(UserRole.OWNER_ROLE))
            .fetchOne(0, Int::class.java) ?: 0
    }

    fun isUserLastOwnerInTenant(userId: UUID, tenantId: UUID): Boolean {
        val userRoles = getRolesForUserInTenant(userId, tenantId)
        if (UserRole.OWNER_ROLE !in userRoles) {
            return false
        }
        return countOwnersInTenant(tenantId) == 1
    }

    fun isUserOwnerInTenant(userId: UUID, tenantId: UUID): Boolean {
        val userRoles = getRolesForUserInTenant(userId, tenantId)
        return UserRole.OWNER_ROLE in userRoles
    }

    fun removeUserFromTenant(userId: UUID, tenantId: UUID): Int {
        return dslContext.deleteFrom(APP_USER_ROLE_TENANT)
            .where(APP_USER_ROLE_TENANT.USER_ID.eq(userId))
            .and(APP_USER_ROLE_TENANT.TENANT_ID.eq(tenantId))
            .execute()
    }

    fun writeUserRolesForTenant(userId: UUID, tenantId: UUID, roles: Set<String>) {
        // Remove all existing roles for user in tenant
        dslContext.deleteFrom(APP_USER_ROLE_TENANT)
            .where(APP_USER_ROLE_TENANT.USER_ID.eq(userId))
            .and(APP_USER_ROLE_TENANT.TENANT_ID.eq(tenantId))
            .execute()

        // Insert new roles
        roles.forEach { role ->
            dslContext.insertInto(APP_USER_ROLE_TENANT)
                .set(APP_USER_ROLE_TENANT.USER_ID, userId)
                .set(APP_USER_ROLE_TENANT.ROLE, role)
                .set(APP_USER_ROLE_TENANT.TENANT_ID, tenantId)
                .execute()
        }
    }

}