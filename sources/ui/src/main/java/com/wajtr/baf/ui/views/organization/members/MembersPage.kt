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
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import com.wajtr.baf.authentication.AuthenticatedTenant
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.base.ViewToolbar
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.ui.components.UserAvatar
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.ui.vaadin.extensions.showSuccessNotification
import com.wajtr.baf.user.*
import jakarta.annotation.security.RolesAllowed
import java.util.*

const val MEMBERS_PAGE = "members"

data class MemberGridItem(
    val user: User,
    val roles: List<String>
)

@RolesAllowed(UserRole.OWNER_ROLE, UserRole.ADMIN_ROLE)
@Route(MEMBERS_PAGE, layout = MainLayout::class)
@Menu(order = 3.0, icon = "vaadin:cog")
class MembersPage(
    private val identity: Identity,
    private val userRoleTenantService: UserRoleTenantService,
    private val userRepository: UserRepository,
    private val authenticationContext: AuthenticationContext
) : MainLayoutPage() {

    private lateinit var grid: Grid<MemberGridItem>

    init {
        style.set("display", "flex")
        style.set("flex-direction", "column")
        flexLayout {
            flexDirection = FlexLayout.FlexDirection.COLUMN
            flexGrow = 1.0
            maxWidth = "1200px"
            add(ViewToolbar(i18n("members.page.header")))
            add(createGrid())
        }

        loadMembers()
    }

    private fun createGrid(): Grid<MemberGridItem> {
        grid = Grid(MemberGridItem::class.java, false)
        grid.setWidthFull()
        grid.flexGrow = 1.0

        // Column 1: User info (avatar, name, email)
        grid.addComponentColumn { member -> createUserInfoColumn(member) }
            .setHeader(i18n("members.column.user")).setFlexGrow(2)

        // Column 2: Roles
        grid.addColumn { member -> member.roles.joinToString(", ") { i18n("role.$it") } }
            .setHeader(i18n("members.column.roles")).setFlexGrow(1)

        // Column 3: Actions
        grid.addComponentColumn { member -> createActionsColumn(member) }
            .setHeader(i18n("members.column.actions")).setFlexGrow(0).setAutoWidth(true)


        grid.setSelectionMode(Grid.SelectionMode.NONE)
        grid.addItemClickListener {
            UI.getCurrent().navigate("$MEMBER_SETTINGS_PAGE/${it.item.user.id}")
        }
        grid.addClassNames("pointer-cursor-on-rows")

        return grid
    }

    private fun createUserInfoColumn(member: MemberGridItem): Div {
        val container = Div()
        container.style.set("display", "flex")
        container.style.set("align-items", "center")
        container.style.set("gap", "0.75rem")

        val avatar = UserAvatar(member.user.name)
        avatar.style.set("flex-shrink", "0")

        val textContainer = VerticalLayout().apply {
            isSpacing = false
            isPadding = false

            span(member.user.name) {
                style.set("font-weight", "500")
            }
            span(member.user.email) {
                style.set("font-size", "0.8rem")
                style.set("line-height", "1rem")
                style.set("color", "var(--vaadin-text-color-secondary)")
            }
        }

        container.add(avatar, textContainer)

        return container
    }

    private fun createActionsColumn(member: MemberGridItem): Component {
        val currentUser = identity.authenticatedUser
        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")

        val isCurrentUser = member.user.id == currentUser.id
        return if (isCurrentUser) {
            createLeaveButtonIfPossible(currentUser, tenant)
        } else {
            createRemoveButton(member, tenant)
        }
    }

    private fun createRemoveButton(
        member: MemberGridItem,
        tenant: AuthenticatedTenant
    ): Button {
        val removeButton = Button(i18n("members.action.remove"))
        removeButton.addThemeVariants(ButtonVariant.AURA_DANGER)

        removeButton.addClickListener {
            showRemoveConfirmation(member.user.id, tenant.id)
        }

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

    /**
     * Removes a user from an organization and logs out the user upon successful removal.
     * This method displays a success or error notification based on the operation's outcome.
     *
     * @param userId The unique identifier of the user to be removed from the organization.
     * @param tenantId The unique identifier of the tenant (organization) the user is leaving.
     */
    private fun leaveOrganization(userId: UUID, tenantId: UUID) {
        try {
            userRoleTenantService.removeUserFromTenant(userId, tenantId)
            authenticationContext.logout()
        } catch (_: Exception) {
            showErrorNotification(i18n("members.leave.failure"))
        }
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
        try {
            // Remove user from this tenant
            userRoleTenantService.removeUserFromTenant(userId, tenantId)

            // Check if user is member of other tenants and if not remove user completely
            val userTenants = userRepository.resolveTenantIdsOfUser(userId)
            if (userTenants.isEmpty()) {
                userRepository.remove(userId)
            }

            showSuccessNotification(i18n("members.remove.success"))
            loadMembers() // Reload grid
        } catch (_: Exception) {
            showErrorNotification(i18n("members.remove.failure"))
        }
    }

    private fun loadMembers() {
        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")

        val userIds = userRoleTenantService.getUserIdsForTenant(tenant.id)
        val members = userIds.mapNotNull { userId ->
            val user = userRepository.findById(userId)
            if (user != null) {
                val roles = userRoleTenantService.getRolesForUserInTenant(userId, tenant.id)
                MemberGridItem(user, roles)
            } else {
                null
            }
        }

        grid.setItems(members)
    }

}