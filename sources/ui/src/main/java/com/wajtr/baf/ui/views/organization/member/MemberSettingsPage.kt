package com.wajtr.baf.ui.views.organization.member

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.onClick
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLink
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.organization.member.MemberManagementService
import com.wajtr.baf.organization.member.MemberOperationResult
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.organization.member.UserRoleTenantService
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.components.BreadcrumbItem
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.ui.components.breadcrumb
import com.wajtr.baf.ui.components.userAvatar
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.ui.vaadin.extensions.showSuccessNotification
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.User
import com.wajtr.baf.user.UserRepository
import jakarta.annotation.security.RolesAllowed
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

const val MEMBER_SETTINGS_PAGE = "member"

data class MemberSettingsFormData(
    var organizationRole: String = UserRole.USER_ROLE,
    var isBillingManager: Boolean = false
)

@RolesAllowed(UserRole.OWNER_ROLE, UserRole.ADMIN_ROLE)
@Route(MEMBER_SETTINGS_PAGE, layout = MainLayout::class)
class MemberSettingsPage(
    private val userRepository: UserRepository,
    private val userRoleTenantService: UserRoleTenantService,
    private val memberManagementService: MemberManagementService,
    private val identity: Identity
) : MainLayoutPage(), HasUrlParameter<String> {

    private lateinit var user: User
    private lateinit var userId: UUID
    private val binder = Binder<MemberSettingsFormData>()
    private val formData = MemberSettingsFormData()

    private lateinit var roleSelectionComponent: RoleSelectionComponent

    init {
        style.set("display", "flex")
        style.set("flex-direction", "column")
        style.set("padding", "2rem")
    }

    override fun setParameter(event: BeforeEvent?, parameter: String) {
        userId = UUID.fromString(parameter)

        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")

        // Load user
        user = userRepository.findById(userId)
            ?: run {
                showErrorNotification(i18n("member.settings.user.not.found"))
                UI.getCurrent().navigate(MEMBERS_PAGE)
                return
            }

        // Load current roles
        val currentRoles = userRoleTenantService.getRolesForUserInTenant(userId, tenant.id)

        // Determine organization role (primary role: OWNER, ADMIN, or USER)
        formData.organizationRole = when {
            UserRole.OWNER_ROLE in currentRoles -> UserRole.OWNER_ROLE
            UserRole.ADMIN_ROLE in currentRoles -> UserRole.ADMIN_ROLE
            else -> UserRole.USER_ROLE
        }

        // Check for additional roles
        formData.isBillingManager = UserRole.BILLING_MANAGER_ROLE in currentRoles

        // Get allowed roles for this user
        val allowedRoles = memberManagementService.getAllowedRolesForUser(userId, tenant.id)

        buildUI(allowedRoles)
    }

    private fun buildUI(allowedRoles: Set<String>) {
        breadcrumb(
            BreadcrumbItem(RouterLink(i18n("members.page.header"), MembersPage::class.java)),
            BreadcrumbItem(i18n("core.ui.common.details"))
        )

        // Header with user name
        horizontalLayout(false, spacing = false) {
            spacing = "0.5rem"
            alignItems = FlexComponent.Alignment.CENTER
            style.set("margin-bottom", "2rem")

            userAvatar(user.name)

            val header = H1(user.name)
            header.style.set("font-size", "x-large")
            add(header)
        }

        val container = VerticalLayout().apply {
            isPadding = false
            maxWidth = "600px"
        }

        container.add(createBasicsSection())

        if (allowedRoles.size == 1
            && allowedRoles.first() == UserRole.OWNER_ROLE
            && identity.hasRole(UserRole.OWNER_ROLE)
        ) {
            // Show explanation that the last owner's role cannot be changed
            val warningSpan = Span(i18n("member.settings.role.last.owner.warning"))
            warningSpan.style.set("color", "var(--aura-red)")
            warningSpan.style.set("font-size", "0.875rem")
            warningSpan.style.set("display", "block")
            warningSpan.style.set("margin-bottom", "1rem")
            container.add(warningSpan)
        }

        roleSelectionComponent = RoleSelectionComponent(showAdditionalRights = true, allowedRoles = allowedRoles)
        container.add(roleSelectionComponent)

        // Save button
        container.button(i18n("member.settings.save")) {
            addThemeVariants(ButtonVariant.AURA_PRIMARY)
            onClick {
                saveMemberSettings()
            }
        }

        add(container)
        bindModel()
        binder.readBean(formData)
    }

    private fun createBasicsSection(): VerticalLayout {
        return VerticalLayout().apply {
            isPadding = false
            style.set("margin-bottom", "2rem")

            formLayout {
                setWidthFull()
                setAutoResponsive(true)

                // Email (readonly)
                addFormItem(
                    Span(user.email),
                    i18n("member.settings.basics.email")
                )

                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .withZone(ZoneId.systemDefault())
                val createdAt = formatter.format(user.createdAt)
                addFormItem(
                    Span(createdAt),
                    i18n("member.settings.basics.added")
                )
            }
        }
    }

    private fun bindModel() {
        binder.forField(roleSelectionComponent.organizationRoleGroup)
            .bind(
                { it.organizationRole },
                { formData, value -> formData.organizationRole = value }
            )

        binder.forField(roleSelectionComponent.billingManagerCheckbox)
            .bind(
                { it.isBillingManager },
                { formData, value -> formData.isBillingManager = value }
            )
    }

    private fun saveMemberSettings() {
        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")

        if (binder.writeBeanIfValid(formData)) {
            val primaryRole = formData.organizationRole
            val additionalRights = mutableSetOf<String>()
            if (formData.isBillingManager) {
                additionalRights.add(UserRole.BILLING_MANAGER_ROLE)
            }

            val result = memberManagementService.setUserRolesForTenant(
                userId, tenant.id, primaryRole, additionalRights
            )

            if (result is MemberOperationResult.Denied) {
                showOperationDeniedDialog(result.reason)
                return
            } else {
                showSuccessNotification(i18n("member.settings.update.success"))
                UI.getCurrent().navigate(MEMBERS_PAGE)
            }
        }
    }
}
