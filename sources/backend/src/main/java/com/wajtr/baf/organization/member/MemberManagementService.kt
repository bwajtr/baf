package com.wajtr.baf.organization.member

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Result of a member management operation
 */
sealed class MemberOperationResult {
    data object Allowed : MemberOperationResult()
    data class Denied(val reason: DenialReason) : MemberOperationResult()
}

enum class DenialReason {
    LAST_OWNER_CANNOT_LEAVE,
    LAST_OWNER_CANNOT_BE_REMOVED,
    LAST_OWNER_ROLE_CANNOT_BE_CHANGED
}

/**
 * Service that encapsulates business rules for member management operations.
 * This service should be used for all member-related operations to ensure
 * consistent application of business rules across UI and future API endpoints.
 */
@Service
@Transactional
class MemberManagementService(
    private val userRoleTenantService: UserRoleTenantService
) {

    /**
     * Checks if a user can leave the specified organization.
     *
     * Business rules:
     * - The last owner of an organization cannot leave
     */
    fun canUserLeaveOrganization(userId: UUID, tenantId: UUID): MemberOperationResult {
        if (userRoleTenantService.isUserLastOwnerInTenant(userId, tenantId)) {
            return MemberOperationResult.Denied(DenialReason.LAST_OWNER_CANNOT_LEAVE)
        }
        return MemberOperationResult.Allowed
    }

    /**
     * Checks if a user can be removed from the specified organization.
     *
     * Business rules:
     * - The last owner of an organization cannot be removed
     */
    fun canUserBeRemoved(userId: UUID, tenantId: UUID): MemberOperationResult {
        if (userRoleTenantService.isUserLastOwnerInTenant(userId, tenantId)) {
            return MemberOperationResult.Denied(DenialReason.LAST_OWNER_CANNOT_BE_REMOVED)
        }
        return MemberOperationResult.Allowed
    }

    /**
     * Sets the roles for a user in a tenant, after validating that the role change is allowed.
     */
    fun setUserRolesForTenant(
        userId: UUID,
        tenantId: UUID,
        primaryRole: String,
        additionalRights: Set<String>
    ): MemberOperationResult {
        // Validate role change is allowed
        val roleChangeResult = canUserRoleBeChanged(userId, tenantId, primaryRole)
        if (roleChangeResult is MemberOperationResult.Denied) {
            return roleChangeResult
        }

        val roles = setOf(primaryRole) + additionalRights

        // All OK, Update roles in database
        userRoleTenantService.writeUserRolesForTenant(userId, tenantId, roles)
        return MemberOperationResult.Allowed
    }

    /**
     * Checks if a user's role can be changed to the specified new role.
     *
     * Business rules:
     * - The last owner cannot have their role changed away from OWNER
     */
    fun canUserRoleBeChanged(userId: UUID, tenantId: UUID, newRole: String): MemberOperationResult {
        // If the new role is OWNER, always allowed
        if (newRole == UserRole.OWNER_ROLE) {
            return MemberOperationResult.Allowed
        }

        // If changing away from OWNER, check if user is last owner
        if (userRoleTenantService.isUserLastOwnerInTenant(userId, tenantId)) {
            return MemberOperationResult.Denied(DenialReason.LAST_OWNER_ROLE_CANNOT_BE_CHANGED)
        }

        return MemberOperationResult.Allowed
    }

    /**
     * Returns the set of roles that the user can be changed to.
     * If the user is the last owner, only OWNER role is allowed.
     */
    fun getAllowedRolesForUser(userId: UUID, tenantId: UUID): Set<String> {
        val allRoles = setOf(UserRole.USER_ROLE, UserRole.ADMIN_ROLE, UserRole.OWNER_ROLE)

        if (userRoleTenantService.isUserLastOwnerInTenant(userId, tenantId)) {
            return setOf(UserRole.OWNER_ROLE)
        }

        return allRoles
    }
}
