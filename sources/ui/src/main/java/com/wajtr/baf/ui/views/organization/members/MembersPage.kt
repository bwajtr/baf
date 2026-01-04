package com.wajtr.baf.ui.views.organization.members

import com.github.mvysny.karibudsl.v10.flexGrow
import com.github.mvysny.karibudsl.v10.flexLayout
import com.github.mvysny.karibudsl.v10.span
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import com.wajtr.baf.authentication.AuthenticatedTenant
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.organization.invitation.MemberInvitationService
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.organization.member.UserRoleTenantService
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.base.ViewToolbar
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.ui.components.UserAvatar
import com.wajtr.baf.ui.vaadin.extensions.showSuccessNotification
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.User
import com.wajtr.baf.user.UserRepository
import jakarta.annotation.security.PermitAll
import java.util.*

const val MEMBERS_PAGE = "members"

sealed interface MemberGridItem {
    data class ActiveMember(
        val user: User,
        val roles: List<String>
    ) : MemberGridItem

    data class InvitedMember(
        val invitationId: UUID,
        val email: String,
        val role: String
    ) : MemberGridItem
}

@PermitAll
@Route(MEMBERS_PAGE, layout = MainLayout::class)
class MembersPage(
    private val identity: Identity,
    private val userRoleTenantService: UserRoleTenantService,
    private val userRepository: UserRepository,
    private val authenticationContext: AuthenticationContext,
    private val memberInvitationService: MemberInvitationService,
) : MainLayoutPage() {

    private lateinit var grid: Grid<MemberGridItem>

    init {
        style.set("display", "flex")
        style.set("flex-direction", "column")
        flexLayout {
            flexDirection = FlexLayout.FlexDirection.COLUMN
            flexGrow = 1.0
            maxWidth = "1200px"
            add(ViewToolbar(i18n("members.page.header"), createInviteMembersButton()))
            add(createGrid())
        }

        loadMembers()
    }

    private fun createInviteMembersButton(): Button {
        return Button(i18n("members.invite.members.button.label"), VaadinIcon.PLUS.create()).apply {
            addThemeVariants(ButtonVariant.AURA_PRIMARY)
            addClassName("aura-accent-green")
            style.set("--vaadin-button-primary-background", "var(--aura-green)")
            addClickListener {
                InviteMembersDialog(identity, memberInvitationService) { loadMembers() }.open()
            }
            isEnabled = identity.hasRole(UserRole.OWNER_ROLE) || identity.hasRole(UserRole.ADMIN_ROLE)
        }
    }

    private fun createGrid(): Grid<MemberGridItem> {
        grid = Grid(MemberGridItem::class.java, false)
        grid.setWidthFull()
        grid.flexGrow = 1.0

        // Column 1: User info (avatar, name, email)
        grid.addComponentColumn { member -> createUserInfoColumn(member) }
            .setHeader(i18n("members.column.user")).setFlexGrow(2)

        // Column 2: Roles
        grid.addColumn { member ->
            when (member) {
                is MemberGridItem.ActiveMember -> member.roles.joinToString(", ") { i18n("role.$it") }
                is MemberGridItem.InvitedMember -> i18n(
                    "members.invitation.role.invited.as",
                    i18n("role.${member.role}")
                )
            }
        }.setHeader(i18n("members.column.roles")).setFlexGrow(1)

        // Column 3: Actions
        grid.addComponentColumn { member -> createActionsColumn(member) }
            .setHeader(i18n("members.column.actions")).setFlexGrow(0).setAutoWidth(true)


        grid.setSelectionMode(Grid.SelectionMode.NONE)
        if (identity.hasRole(UserRole.OWNER_ROLE) || identity.hasRole(UserRole.ADMIN_ROLE)) {
            grid.addItemClickListener {
                when (val item = it.item) {
                    is MemberGridItem.ActiveMember -> {
                        UI.getCurrent().navigate("$MEMBER_SETTINGS_PAGE/${item.user.id}")
                    }

                    is MemberGridItem.InvitedMember -> {
                        UI.getCurrent().navigate("$INVITATION_DETAILS_PAGE/${item.invitationId}")
                    }
                }
            }
            grid.addClassNames("pointer-cursor-on-rows")
        }

        return grid
    }

    private fun createUserInfoColumn(member: MemberGridItem): Div {
        val container = Div()
        container.style.set("display", "flex")
        container.style.set("align-items", "center")
        container.style.set("gap", "0.75rem")

        val (avatarName, primaryText, secondaryText) = when (member) {
            is MemberGridItem.ActiveMember -> {
                Triple(member.user.name, member.user.name, member.user.email)
            }

            is MemberGridItem.InvitedMember -> {
                Triple(null, member.email, i18n("members.invitation.label"))
            }
        }

        val avatar = if (avatarName != null) {
            UserAvatar(avatarName)
        } else Div().apply { width = "28px" }
        avatar.style.set("flex-shrink", "0")

        val textContainer = VerticalLayout().apply {
            isSpacing = false
            isPadding = false

            span(primaryText) {
                style.set("font-weight", "500")
            }
            span(secondaryText) {
                style.set("font-size", "0.8rem")
                style.set("line-height", "1rem")
                style.set("color", "var(--vaadin-text-color-secondary)")
            }
        }

        container.add(avatar, textContainer)

        return container
    }

    private fun createActionsColumn(member: MemberGridItem): Component {
        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")

        return when (member) {
            is MemberGridItem.ActiveMember -> {
                createLeaveOrRemoveButton(member, tenant)
            }

            is MemberGridItem.InvitedMember -> {
                createCancelInvitationButton(member)
            }
        }
    }

    private fun createLeaveOrRemoveButton(
        member: MemberGridItem.ActiveMember,
        tenant: AuthenticatedTenant
    ): Component {
        val currentUser = identity.authenticatedUser
        val isCurrentUser = member.user.id == currentUser.id
        return if (isCurrentUser) {
            createLeaveButtonIfPossible(currentUser, tenant)
        } else {
            createRemoveButton(member, tenant)
        }
    }

    private fun createRemoveButton(
        member: MemberGridItem.ActiveMember,
        tenant: AuthenticatedTenant
    ): Button {
        val removeButton = Button(i18n("members.action.remove"))
        removeButton.addThemeVariants(ButtonVariant.AURA_DANGER)

        removeButton.addClickListener {
            showRemoveConfirmation(member.user.id, tenant.id)
        }
        removeButton.isEnabled = identity.hasRole(UserRole.OWNER_ROLE) || identity.hasRole(UserRole.ADMIN_ROLE)

        return removeButton
    }

    private fun createLeaveButtonIfPossible(
        currentUser: User,
        tenant: AuthenticatedTenant
    ): Component {
        // Check if user is member of other organizations
        val userTenants = userRepository.resolveTenantIdsOfUser(currentUser.id)
        val hasOtherTenants = userTenants.size > 1

        return if (hasOtherTenants) {
            // Show "Leave" button for current user
            val leaveButton = Button(i18n("members.action.leave"))
            leaveButton.addThemeVariants(ButtonVariant.AURA_DANGER)

            leaveButton.addClickListener {
                showLeaveConfirmation(currentUser.id, tenant.id)
            }

            leaveButton
        } else {
            Span()
        }
    }

    private fun createCancelInvitationButton(
        invitation: MemberGridItem.InvitedMember
    ): Button {
        val cancelButton = Button(i18n("members.action.cancel"))
        cancelButton.addThemeVariants(ButtonVariant.AURA_DANGER)

        cancelButton.addClickListener {
            showCancelInvitationConfirmation(invitation.invitationId)
        }

        cancelButton.isEnabled = identity.hasRole(UserRole.OWNER_ROLE) || identity.hasRole(UserRole.ADMIN_ROLE)

        return cancelButton
    }

    private fun showLeaveConfirmation(userId: UUID, tenantId: UUID) {
        val dialog = ConfirmDialog()
        dialog.setHeader(i18n("members.confirm.leave.title"))
        dialog.setText(i18n("members.confirm.leave.message"))

        dialog.setCancelable(true)
        dialog.setConfirmText(i18n("members.confirm.leave.yes"))
        dialog.setCancelText(i18n("members.confirm.leave.no"))

        dialog.setConfirmButtonTheme("error primary")

        dialog.addConfirmListener {
            leaveOrganization(userId, tenantId)
        }

        dialog.open()
    }

    private fun showRemoveConfirmation(userId: UUID, tenantId: UUID) {
        val dialog = ConfirmDialog()
        dialog.setHeader(i18n("members.confirm.remove.title"))
        dialog.setText(i18n("members.confirm.remove.message"))

        dialog.setCancelable(true)
        dialog.setConfirmText(i18n("members.confirm.remove.yes"))
        dialog.setCancelText(i18n("members.confirm.remove.no"))

        dialog.setConfirmButtonTheme("error primary")

        dialog.addConfirmListener {
            removeUserFromOrganization(userId, tenantId)
        }

        dialog.open()
    }

    private fun showCancelInvitationConfirmation(invitationId: UUID) {
        val dialog = ConfirmDialog()
        dialog.setHeader(i18n("members.confirm.cancel.title"))
        dialog.setText(i18n("members.confirm.cancel.message"))

        dialog.setCancelable(true)
        dialog.setConfirmText(i18n("members.confirm.cancel.yes"))
        dialog.setCancelText(i18n("members.confirm.cancel.no"))

        dialog.setConfirmButtonTheme("error primary")

        dialog.addConfirmListener {
            cancelInvitation(invitationId)
        }

        dialog.open()
    }

    /**
     * Removes a user from an organization and logs out the user upon successful removal.
     * This method displays a success or error notification based on the operation's outcome.
     *
     * @param userId The unique identifier of the user to be removed from the organization.
     * @param tenantId The unique identifier of the tenant (organization) the user is leaving.
     */
    private fun leaveOrganization(userId: UUID, tenantId: UUID) {
        userRoleTenantService.removeUserFromTenant(userId, tenantId)
        authenticationContext.logout()
    }

    /**
     * Removes a user from a specified tenant (organization) and deletes the user completely
     * if they are not associated with any other tenants. Displays a success or error notification
     * based on the operation's outcome and updates the members grid on successful removal.
     *
     * @param userId The unique identifier of the user to be removed.
     * @param tenantId The unique identifier of the tenant (organization) from which the user will be removed.
     */
    private fun removeUserFromOrganization(userId: UUID, tenantId: UUID) {
        // Remove user from this tenant
        userRoleTenantService.removeUserFromTenant(userId, tenantId)

        // Check if user is member of other tenants and if not remove user completely
        val userTenants = userRepository.resolveTenantIdsOfUser(userId)
        if (userTenants.isEmpty()) {
            userRepository.remove(userId)
        }

        showSuccessNotification(i18n("members.remove.success"))
        loadMembers() // Reload grid
    }

    /**
     * Cancels a member invitation by deleting it from the database.
     * Displays a success or error notification based on the operation's outcome
     * and updates the members grid on successful cancellation.
     *
     * @param invitationId The unique identifier of the invitation to be cancelled.
     */
    private fun cancelInvitation(invitationId: UUID) {
        memberInvitationService.deleteInvitationById(invitationId)
        showSuccessNotification(i18n("members.cancel.success"))
        loadMembers() // Reload grid
    }

    private fun loadMembers() {
        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")

        // Load active members
        val userIds = userRoleTenantService.getUserIdsForTenant(tenant.id)
        val activeMembers = userIds.mapNotNull { userId ->
            val user = userRepository.findById(userId)
            if (user != null) {
                val roles = userRoleTenantService.getRolesForUserInTenant(userId, tenant.id)
                MemberGridItem.ActiveMember(user, roles)
            } else {
                null
            }
        }

        // Load invitations
        val invitations = memberInvitationService.getAllInvitations().map { invitation ->
            MemberGridItem.InvitedMember(
                invitationId = invitation.id,
                email = invitation.email,
                role = invitation.role
            )
        }

        // Combine and set items
        val allItems = activeMembers + invitations
        grid.setItems(allItems)
    }

}