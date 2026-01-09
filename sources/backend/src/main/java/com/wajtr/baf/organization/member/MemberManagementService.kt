package com.wajtr.baf.organization.member

import com.wajtr.baf.user.Identity
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
    LAST_OWNER_ROLE_CANNOT_BE_CHANGED,
    ONLY_OWNER_CAN_GRANT_OR_REVOKE_OWNER_ROLE,
    ONLY_OWNER_CAN_REMOVE_OWNER
}

/**
 * Service that encapsulates business rules for member management operations.
 * This service should be used for all member-related operations to ensure
 * consistent application of business rules across UI and future API endpoints.
 */
@Service
@Transactional
class MemberManagementService(
    private val userRoleTenantService: UserRoleTenantService,
    private val identity: Identity
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
     * - Only owners can remove other owners
     * - The last owner of an organization cannot be removed
     */
    fun canUserBeRemoved(userId: UUID, tenantId: UUID): MemberOperationResult {
        // Check if target user is an owner - only owners can remove other owners
        if (userRoleTenantService.isUserOwnerInTenant(userId, tenantId)) {
            if (!identity.hasRole(UserRole.OWNER_ROLE)) {
                return MemberOperationResult.Denied(DenialReason.ONLY_OWNER_CAN_REMOVE_OWNER)
            }
            // Check if this is the last owner
            if (userRoleTenantService.isUserLastOwnerInTenant(userId, tenantId)) {
                return MemberOperationResult.Denied(DenialReason.LAST_OWNER_CANNOT_BE_REMOVED)
            }
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
     * - Only owners can grant or revoke the OWNER role to other users
     * - The last owner cannot have their role changed away from OWNER
     */
    fun canUserRoleBeChanged(userId: UUID, tenantId: UUID, newRole: String): MemberOperationResult {
        // If the new role is OWNER or target user is OWNER, check if current user is an owner
        val isTargetUserOwner = userRoleTenantService.isUserOwnerInTenant(userId, tenantId)
        if ((newRole == UserRole.OWNER_ROLE && !isTargetUserOwner)
            || (isTargetUserOwner && newRole != UserRole.OWNER_ROLE)
        ) {
            if (!identity.hasRole(UserRole.OWNER_ROLE)) {
                return MemberOperationResult.Denied(DenialReason.ONLY_OWNER_CAN_GRANT_OR_REVOKE_OWNER_ROLE)
            }
        }

        // If changing away from OWNER, check if user is last owner in the tenant
        if (userRoleTenantService.isUserLastOwnerInTenant(userId, tenantId) && newRole != UserRole.OWNER_ROLE) {
            return MemberOperationResult.Denied(DenialReason.LAST_OWNER_ROLE_CANNOT_BE_CHANGED)
        }

        return MemberOperationResult.Allowed
    }

    /**
     * Returns the set of roles that the user can be changed to.
     *
     * Business rules:
     * - If the user is the last owner, only OWNER role is allowed
     * - Only owners can grant or revoke the OWNER role, so ADMINs can grant only USER or ADMIN to USERs or ADMINs
     */
    fun getAllowedRolesForUser(userId: UUID, tenantId: UUID): Set<String> {
        // If the target user is the last owner, only OWNER role is allowed
        if (userRoleTenantService.isUserLastOwnerInTenant(userId, tenantId)) {
            return setOf(UserRole.OWNER_ROLE)
        }

        // If current user is not an owner, they cannot grant or revoke OWNER role
        if (!identity.hasRole(UserRole.OWNER_ROLE)) {
            return if (userRoleTenantService.isUserOwnerInTenant(userId, tenantId))
                setOf(UserRole.OWNER_ROLE) // this prevents to select any other role
            else
                setOf(UserRole.USER_ROLE, UserRole.ADMIN_ROLE)
        }

        // If current user is an owner, they can grant all roles
        return setOf(UserRole.USER_ROLE, UserRole.ADMIN_ROLE, UserRole.OWNER_ROLE)
    }

    fun getAllowedRolesForInvitation(): Set<String> {
        // Role combobox - only owners can invite with OWNER role
        return if (identity.hasRole(UserRole.OWNER_ROLE)) {
            setOf(UserRole.USER_ROLE, UserRole.ADMIN_ROLE, UserRole.OWNER_ROLE)
        } else {
            setOf(UserRole.USER_ROLE, UserRole.ADMIN_ROLE)
        }
    }
}
