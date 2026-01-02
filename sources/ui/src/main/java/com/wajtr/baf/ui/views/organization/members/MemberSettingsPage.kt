package com.wajtr.baf.ui.views.organization.members

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLink
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.organization.member.UserRoleTenantService
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.components.BreadcrumbItem
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.ui.components.breadcrumb
import com.wajtr.baf.ui.components.userAvatar
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.ui.vaadin.extensions.showSuccessNotification
import com.wajtr.baf.user.*
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
    private val identity: Identity
) : MainLayoutPage(), HasUrlParameter<String> {

    private lateinit var user: User
    private lateinit var userId: UUID
    private val binder = Binder<MemberSettingsFormData>()
    private val formData = MemberSettingsFormData()

    private lateinit var organizationRoleGroup: RadioButtonGroup<String>
    private lateinit var billingManagerCheckbox: Checkbox

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

        buildUI()
    }

    private fun buildUI() {
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
        container.add(createOrganizationRoleSection())
        container.add(createAdditionalRightsSection())

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

    private fun createOrganizationRoleSection(): VerticalLayout {
        return VerticalLayout().apply {
            isPadding = false
            style.set("margin-bottom", "2rem")

            add(H2(i18n("member.settings.role.section")))

            organizationRoleGroup = radioButtonGroup {
                setItems(UserRole.USER_ROLE, UserRole.ADMIN_ROLE, UserRole.OWNER_ROLE)
                setRenderer(ComponentRenderer { role ->
                    createRoleOption(role, i18n("member.settings.role.description.$role"))
                })
            }

            add(organizationRoleGroup)
        }
    }

    private fun createAdditionalRightsSection(): VerticalLayout {
        return VerticalLayout().apply {
            isPadding = false
            style.set("margin-bottom", "2rem")

            add(H2(i18n("member.settings.additional.section")))

            billingManagerCheckbox = checkBox {
                setLabelComponent(
                    createRoleOption(
                        UserRole.BILLING_MANAGER_ROLE,
                        i18n("member.settings.additional.description.${UserRole.BILLING_MANAGER_ROLE}")
                    )
                )
            }

            add(billingManagerCheckbox)
        }
    }

    private fun createRoleOption(roleName: String, description: String): Div {
        return Div().apply {
            val nameSpan = Span(i18n("role.$roleName"))
            nameSpan.style.set("font-weight", "bold")
            nameSpan.style.set("display", "block")

            val descSpan = Span(description)
            descSpan.style.set("font-size", "0.875rem")
            descSpan.style.set("color", "var(--vaadin-text-color-secondary)")
            descSpan.style.set("display", "block")
            descSpan.style.set("margin-top", "0.25rem")

            add(nameSpan, descSpan)
        }
    }

    private fun bindModel() {
        binder.forField(organizationRoleGroup)
            .bind(
                { it.organizationRole },
                { formData, value -> formData.organizationRole = value }
            )

        binder.forField(billingManagerCheckbox)
            .bind(
                { it.isBillingManager },
                { formData, value -> formData.isBillingManager = value }
            )
    }

    private fun saveMemberSettings() {
        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")

        if (binder.writeBeanIfValid(formData)) {
            // Build the set of roles
            val roles = mutableSetOf<String>()

            // Add primary organization role
            roles.add(formData.organizationRole)

            // Add additional roles
            if (formData.isBillingManager) {
                roles.add(UserRole.BILLING_MANAGER_ROLE)
            }

            // Update roles in database
            userRoleTenantService.setUserRolesForTenant(userId, tenant.id, roles)

            showSuccessNotification(i18n("member.settings.update.success"))
            UI.getCurrent().navigate(MEMBERS_PAGE)
        }
    }
}
