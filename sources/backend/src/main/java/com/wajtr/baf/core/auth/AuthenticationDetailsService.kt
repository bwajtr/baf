package com.wajtr.baf.core.auth

import org.jooq.DSLContext
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthenticationDetailsService(
    @Suppress("unused") private val dsl: DSLContext
) {

    fun loadAuthenticationDetails(email: String): AuthenticationDetails? {
        // TODO: Implement database query to fetch user's tenant and roles
        // This is a placeholder implementation that you should customize based on your database schema

        // Example implementation (adjust based on your actual schema):
        /*
        val userRecord = dsl.selectFrom(APP_USER)
            .where(APP_USER.EMAIL.eq(email))
            .or(APP_USER.EXTERNAL_ID.eq(subject))
            .fetchOne()
            ?: return null
        
        val roles = dsl.select(ROLE.NAME)
            .from(USER_ROLE)
            .join(ROLE).on(ROLE.ID.eq(USER_ROLE.ROLE_ID))
            .where(USER_ROLE.USER_ID.eq(userRecord.id))
            .fetch()
            .map { SimpleGrantedAuthority("ROLE_${it.value1()}") }
            .toSet()
        
        return AuthenticationDetails(
            tenantId = userRecord.tenantId,
            roles = roles
        )
        */

        // For now, return a default implementation for development
        // Replace this with actual database queries
        return AuthenticationDetails(
            tenant = AuthenticatedTenant(UUID.fromString("2dcab49d-8807-4888-bb69-2afd663e2743")), // developer_tenant_id
            user = AuthenticatedUser(UUID.randomUUID(), email, "Random Name"),
            roles = if (email == "user@gmail.com")
                setOf(SimpleGrantedAuthority("ROLE_" + USER_MANAGER_ROLE))
            else
                setOf(SimpleGrantedAuthority("ROLE_" + ADMIN_ROLE))
        )
    }
}

data class AuthenticationDetails(
    val tenant: AuthenticatedTenant,
    val user: AuthenticatedUser,
    val roles: Set<GrantedAuthority>
)


data class AuthenticatedUser(
    val id: UUID,
    val email: String,
    val name: String,
)

data class AuthenticatedTenant(
    val id: UUID,
)