package com.wajtr.baf.test

import com.wajtr.baf.authentication.AuthenticatedTenant
import com.wajtr.baf.user.User
import com.wajtr.baf.user.UserRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*

/**
 * Helper for setting up authentication context in integration tests.
 * Uses the test users defined in integration-test-basic-database-content.sql
 * 
 * This helper sets up the Spring Security context with real User objects and
 * roles, simulating how authentication works in production (without OAuth2 flow).
 * 
 * Usage:
 * ```
 * authenticationTestHelper.loginAsOwner()
 * // ... test code that requires OWNER role
 * ```
 */
@Component
class AuthenticationTestHelper(
    private val userRepository: UserRepository
) {

    companion object {
        // Test user IDs from integration-test-basic-database-content.sql
        val JOE_USER_ID: UUID = UUID.fromString("019b5aa6-97b6-7358-8ffe-bb68f70c8fc6")
        val JANE_ADMIN_ID: UUID = UUID.fromString("019b5aa6-cd48-75f9-8b74-59878b0ea7d9")
        val JOSH_OWNER_ID: UUID = UUID.fromString("019b5aa6-eae4-76f0-9077-571f50df349b")
        val WILLIAM_OWNER_ID: UUID = UUID.fromString("019b5ab7-72c3-739d-b548-b13d1d59fe11")

        // Test tenant IDs
        val TENANT_1_ID: UUID = UUID.fromString("019b25f2-3cc6-761c-9e6e-1c0d279bfd30")
        val TENANT_2_ID: UUID = UUID.fromString("019b25f2-6e55-7f32-bf82-9e2d116873ce")
    }

    /**
     * Login as Joe User (USER role) in Tenant 1
     * Email: joe.user@acme.com
     * Password: test
     */
    fun loginAsUser(): User {
        return loginAs(JOE_USER_ID, TENANT_1_ID, setOf("ROLE_USER"))
    }

    /**
     * Login as Jane Admin (ADMIN role) in Tenant 1
     * Email: jane.admin@acme.com
     * Password: test
     * 
     * Note: ADMIN role typically includes USER privileges
     */
    fun loginAsAdmin(): User {
        return loginAs(JANE_ADMIN_ID, TENANT_1_ID, setOf("ROLE_ADMIN"))
    }

    /**
     * Login as Josh Owner (OWNER role) in Tenant 1
     * Email: josh.owner@acme.com
     * Password: test
     * 
     * Note: OWNER role typically includes ADMIN and USER privileges,
     * plus BILLING_MANAGER in this test setup
     */
    fun loginAsOwner(): User {
        return loginAs(
            JOSH_OWNER_ID, 
            TENANT_1_ID, 
            setOf("ROLE_OWNER", "ROLE_BILLING_MANAGER")
        )
    }

    /**
     * Login as William Owner (OWNER role) in Tenant 2
     * Email: william.owner@acme.com
     * Password: test
     * 
     * Use this to test cross-tenant scenarios
     */
    fun loginAsOwnerTenant2(): User {
        return loginAs(
            WILLIAM_OWNER_ID, 
            TENANT_2_ID, 
            setOf("ROLE_OWNER", "ROLE_BILLING_MANAGER")
        )
    }

    /**
     * Login as specific user with specific roles and tenant context.
     * 
     * @param userId The UUID of the user from the database
     * @param tenantId The UUID of the tenant for multi-tenant context
     * @param roles Set of role names (must include "ROLE_" prefix, e.g., "ROLE_USER")
     * @return The authenticated User object
     * @throws IllegalStateException if user not found in database
     */
    fun loginAs(userId: UUID, tenantId: UUID, roles: Set<String>): User {
        val user = userRepository.findById(userId) 
            ?: throw IllegalStateException("User $userId not found. Did you call databaseTestHelper.loadBasicTestData()?")
        
        val authorities = roles.map { SimpleGrantedAuthority(it) }
        val tenant = AuthenticatedTenant(tenantId)

        val authentication = UsernamePasswordAuthenticationToken(user, null, authorities)
        authentication.details = tenant

        SecurityContextHolder.getContext().authentication = authentication
        return user
    }

    /**
     * Clear authentication context. You typically don't need to call this manually.
     */
    fun logout() {
        SecurityContextHolder.clearContext()
    }
}
