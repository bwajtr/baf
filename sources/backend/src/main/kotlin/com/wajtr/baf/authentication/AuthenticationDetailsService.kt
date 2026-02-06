package com.wajtr.baf.authentication

import com.wajtr.baf.db.jooq.tables.references.TENANT_MEMBER
import com.wajtr.baf.user.User
import com.wajtr.baf.user.UserRepository
import org.jooq.DSLContext
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthenticationDetailsService(
    private val dslContext: DSLContext,
    private val userRepository: UserRepository,
) {

    fun loadAuthenticationDetails(email: String, desiredTenantId: UUID? = null): AuthenticationDetails {
        val user = userRepository.loadUserByUsername(email)

        // if desiredTenantId is null then get the first tenant associated with this user
        val tenantId = desiredTenantId ?: dslContext
            .select(TENANT_MEMBER.TENANT_ID)
            .from(TENANT_MEMBER)
            .where(TENANT_MEMBER.USER_ID.eq(user.id))
            .limit(1)
            .fetchOne(TENANT_MEMBER.TENANT_ID) ?: throw NoTenantFoundException(email)

        // Load all roles for this user and tenant
        val roles = dslContext
            .select(TENANT_MEMBER.ROLE)
            .from(TENANT_MEMBER)
            .where(TENANT_MEMBER.USER_ID.eq(user.id))
            .and(TENANT_MEMBER.TENANT_ID.eq(tenantId))
            .fetch()
            .map { record -> SimpleGrantedAuthority("ROLE_${record.value1()}") }
            .toSet()

        if (roles.isEmpty()) {
            throw NoRolesFoundForUserAndTenant(email, tenantId)
        }

        return AuthenticationDetails(
            tenant = AuthenticatedTenant(id = tenantId),
            user = user,
            roles = roles
        )
    }
}

data class AuthenticationDetails(
    val tenant: AuthenticatedTenant,
    val user: User,
    val roles: Set<GrantedAuthority>
)

data class AuthenticatedTenant(
    val id: UUID,
)

class NoTenantFoundException(val email: String) : AuthenticationException("No tenant found for email $email")
class NoRolesFoundForUserAndTenant(val email: String, val tenantId: UUID) :
    AuthenticationException("No user roles found for email $email and $tenantId")