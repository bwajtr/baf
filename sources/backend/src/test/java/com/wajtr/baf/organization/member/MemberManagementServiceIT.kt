package com.wajtr.baf.organization.member

import com.wajtr.baf.test.AuthenticationTestHelper
import com.wajtr.baf.test.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration test for MemberManagementService.
 * Tests business rules for member management operations with real database and Spring context.
 * 
 * Integration tests should:
 * - Extend BaseIntegrationTest to get Testcontainers and test helpers
 * - Have suffix *IT.kt (e.g., MemberManagementServiceIT.kt)
 * - NO need for @SpringBootTest - BaseIntegrationTest already has it
 * - Load test data in @BeforeEach using databaseTestHelper.loadBasicTestData()
 * - Set authentication context using authenticationTestHelper.loginAs*()
 * - Test real business logic with actual database operations
 * - Use AssertJ assertions (assertThat) for fluent, readable assertions
 * 
 * The @Transactional annotation (from BaseIntegrationTest) ensures that all
 * database changes are rolled back after each test, maintaining test isolation.
 */
class MemberManagementServiceIT : BaseIntegrationTest() {

    @Autowired
    private lateinit var memberManagementService: MemberManagementService

    @Test
    fun `canMemberBeRemoved should allow admin to remove regular member`() {
        // Given: Jane is logged in as ADMIN
        authenticationTestHelper.loginAsAdmin()

        // When: Admin tries to remove Joe (regular USER)
        val result = memberManagementService.canMemberBeRemoved(
            AuthenticationTestHelper.JOE_USER_ID,
            AuthenticationTestHelper.TENANT_1_ID
        )

        // Then: Operation should be allowed
        assertThat(result)
            .describedAs("Admin should be able to remove regular members")
            .isInstanceOf(MemberOperationResult.Allowed::class.java)
    }

    @Test
    fun `canMemberBeRemoved should deny non-owner from removing owner`() {
        // Given: Jane is logged in as ADMIN (not OWNER)
        authenticationTestHelper.loginAsAdmin()

        // When: Admin tries to remove Josh (OWNER)
        val result = memberManagementService.canMemberBeRemoved(
            AuthenticationTestHelper.JOSH_OWNER_ID,
            AuthenticationTestHelper.TENANT_1_ID
        )

        // Then: Operation should be denied
        assertThat(result)
            .describedAs("Non-owner should not be able to remove owners")
            .isInstanceOf(MemberOperationResult.Denied::class.java)
        
        assertThat((result as MemberOperationResult.Denied).reason)
            .describedAs("Denial reason should indicate only owners can remove other owners")
            .isEqualTo(DenialReason.ONLY_OWNER_CAN_REMOVE_OWNER)
    }

    @Test
    fun `canMemberBeRemoved should deny removing last owner`() {
        // When: Owner tries to remove himself (he's the only owner in Tenant 1)
        val result = memberManagementService.canMemberBeRemoved(
            AuthenticationTestHelper.JOSH_OWNER_ID,
            AuthenticationTestHelper.TENANT_1_ID
        )

        // Then: Operation should be denied
        assertThat(result)
            .describedAs("Last owner should not be removable")
            .isInstanceOf(MemberOperationResult.Denied::class.java)
        
        assertThat((result as MemberOperationResult.Denied).reason)
            .describedAs("Denial reason should indicate last owner cannot be removed")
            .isEqualTo(DenialReason.LAST_OWNER_CANNOT_BE_REMOVED)
    }

    @Test
    fun `canUserLeaveOrganization should deny last owner from leaving`() {
        // When: Owner tries to leave the organization
        val result = memberManagementService.canUserLeaveOrganization(
            AuthenticationTestHelper.JOSH_OWNER_ID,
            AuthenticationTestHelper.TENANT_1_ID
        )

        // Then: Operation should be denied
        assertThat(result)
            .describedAs("Last owner should not be able to leave")
            .isInstanceOf(MemberOperationResult.Denied::class.java)
        
        assertThat((result as MemberOperationResult.Denied).reason)
            .describedAs("Denial reason should indicate last owner cannot leave")
            .isEqualTo(DenialReason.LAST_OWNER_CANNOT_LEAVE)
    }

    @Test
    fun `canUserLeaveOrganization should allow regular user to leave`() {
        // Given: Joe is logged in as regular USER
        authenticationTestHelper.loginAsUser()

        // When: User tries to leave the organization
        val result = memberManagementService.canUserLeaveOrganization(
            AuthenticationTestHelper.JOE_USER_ID,
            AuthenticationTestHelper.TENANT_1_ID
        )

        // Then: Operation should be allowed
        assertThat(result)
            .describedAs("Regular users should be able to leave organization")
            .isInstanceOf(MemberOperationResult.Allowed::class.java)
    }

    @Test
    fun `canUserRoleBeChanged should deny non-owner from changing role to OWNER`() {
        // Given: Jane is logged in as ADMIN (not OWNER)
        authenticationTestHelper.loginAsAdmin()

        // When: Admin tries to promote Joe to OWNER role
        val result = memberManagementService.canUserRoleBeChanged(
            AuthenticationTestHelper.JOE_USER_ID,
            AuthenticationTestHelper.TENANT_1_ID,
            UserRole.OWNER_ROLE
        )

        // Then: Operation should be denied
        assertThat(result)
            .describedAs("Non-owner should not be able to grant OWNER role")
            .isInstanceOf(MemberOperationResult.Denied::class.java)
        
        assertThat((result as MemberOperationResult.Denied).reason)
            .describedAs("Denial reason should indicate only owners can grant OWNER role")
            .isEqualTo(DenialReason.ONLY_OWNER_CAN_GRANT_OR_REVOKE_OWNER_ROLE)
    }

    @Test
    fun `canUserRoleBeChanged should allow owner to change role to OWNER`() {
        // When: Owner tries to promote Joe to OWNER role
        val result = memberManagementService.canUserRoleBeChanged(
            AuthenticationTestHelper.JOE_USER_ID,
            AuthenticationTestHelper.TENANT_1_ID,
            UserRole.OWNER_ROLE
        )

        // Then: Operation should be allowed
        assertThat(result)
            .describedAs("Owner should be able to grant OWNER role to other users")
            .isInstanceOf(MemberOperationResult.Allowed::class.java)
    }

    @Test
    fun `getAllowedRolesForUser should return only OWNER for last owner`() {
        // When: Getting allowed roles for Josh (the last owner)
        val allowedRoles = memberManagementService.getAllowedRolesForUser(
            AuthenticationTestHelper.JOSH_OWNER_ID,
            AuthenticationTestHelper.TENANT_1_ID
        )

        // Then: Only OWNER role should be allowed
        assertThat(allowedRoles)
            .describedAs("Last owner should only be allowed to keep OWNER role")
            .containsExactly(UserRole.OWNER_ROLE)
    }

    @Test
    fun `getAllowedRolesForUser should return USER and ADMIN for non-owner when requester is not owner`() {
        // Given: Jane is logged in as ADMIN (not OWNER)
        authenticationTestHelper.loginAsAdmin()

        // When: Getting allowed roles for Joe (regular user)
        val allowedRoles = memberManagementService.getAllowedRolesForUser(
            AuthenticationTestHelper.JOE_USER_ID,
            AuthenticationTestHelper.TENANT_1_ID
        )

        // Then: Only USER and ADMIN roles should be allowed (no OWNER)
        assertThat(allowedRoles)
            .describedAs("Non-owner admins should only be able to assign USER and ADMIN roles")
            .containsExactlyInAnyOrder(UserRole.USER_ROLE, UserRole.ADMIN_ROLE)
    }
}
