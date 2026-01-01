package com.wajtr.baf.user

import com.wajtr.baf.db.jooq.Tables.APP_USER_ROLE_TENANT
import com.wajtr.baf.db.jooq.Tables.TENANT
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
class UserRoleTenantService(
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
                    tenantId = record.get(TENANT.ID),
                    organizationName = record.get(TENANT.ORGANIZATION_NAME)
                )
            }
    }

}