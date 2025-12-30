package com.wajtr.baf.user

import com.wajtr.baf.db.jooq.Tables.APP_USER_ROLE_TENANT
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


data class UserRoleTenant(
    val userId: UUID,
    val role: String,
    val tenantId: UUID
)

@Service
@Transactional
class UserRoleService(
    private val dslContext: DSLContext
) {

    fun insertRole(userRoleTenant: UserRoleTenant) {
        dslContext.insertInto(APP_USER_ROLE_TENANT)
            .set(APP_USER_ROLE_TENANT.USER_ID, userRoleTenant.userId)
            .set(APP_USER_ROLE_TENANT.ROLE, userRoleTenant.role)
            .set(APP_USER_ROLE_TENANT.TENANT_ID, userRoleTenant.tenantId)
            .execute()
    }

}