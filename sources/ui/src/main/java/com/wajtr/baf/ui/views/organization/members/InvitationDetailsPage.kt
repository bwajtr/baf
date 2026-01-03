package com.wajtr.baf.ui.views.organization.members

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLink
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.organization.invitation.MemberInvitationDetails
import com.wajtr.baf.organization.invitation.MemberInvitationService
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.components.BreadcrumbItem
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.ui.components.breadcrumb
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.ui.vaadin.extensions.showSuccessNotification
import jakarta.annotation.security.RolesAllowed
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

const val INVITATION_DETAILS_PAGE = "invitation"

data class InvitationDetailsFormData(
    var organizationRole: String = UserRole.USER_ROLE
)

@RolesAllowed(UserRole.OWNER_ROLE, UserRole.ADMIN_ROLE)
@Route(INVITATION_DETAILS_PAGE, layout = MainLayout::class)
class InvitationDetailsPage(
    private val memberInvitationService: MemberInvitationService
) : MainLayoutPage(), HasUrlParameter<String> {

    private lateinit var invitation: MemberInvitationDetails
    private lateinit var invitationId: UUID
    private val binder = Binder<InvitationDetailsFormData>()
    private val formData = InvitationDetailsFormData()

    private lateinit var organizationRoleGroup: RadioButtonGroup<String>

    init {
        style.set("display", "flex")
        style.set("flex-direction", "column")
        style.set("padding", "2rem")
    }

    override fun setParameter(event: BeforeEvent, parameter: String) {
        invitationId = UUID.fromString(parameter)

        invitation = memberInvitationService.getInvitationById(invitationId)
            ?: run {
                showErrorNotification(i18n("invitation.details.not.found"))
                event.rerouteTo(MEMBERS_PAGE)
                return
            }

        formData.organizationRole = invitation.role

        buildUI()
    }

    override fun getPageTitle(): String {
        return i18n("invitation.details.page.header", invitation.email)
    }

    private fun buildUI() {
        breadcrumb(
            BreadcrumbItem(RouterLink(i18n("members.page.header"), MembersPage::class.java)),
            BreadcrumbItem(i18n("core.ui.common.details"))
        )

        // Header with email
        val header = H1(i18n("invitation.details.page.header", invitation.email))
        header.style.set("font-size", "x-large")
        header.style.set("margin-bottom", "2rem")
        add(header)

        val container = VerticalLayout().apply {
            isPadding = false
            maxWidth = "600px"
        }

        container.add(createBasicsSection())
        container.add(createOrganizationRoleSection())

        // Save button
        container.button(i18n("invitation.details.save")) {
            addThemeVariants(ButtonVariant.AURA_PRIMARY)
            onClick {
                saveInvitationSettings()
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

                // Invited on (readonly)
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .withZone(ZoneId.systemDefault())
                val createdAt = formatter.format(invitation.createdAt)
                addFormItem(
                    Span(createdAt),
                    i18n("invitation.details.created.at")
                )

                // Invited by (readonly)
                val invitedByName = invitation.invitedByName ?: i18n("invitation.details.invited.by.unknown")
                addFormItem(
                    Span(invitedByName),
                    i18n("invitation.details.invited.by")
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
    }

    private fun saveInvitationSettings() {
        if (binder.writeBeanIfValid(formData)) {
            memberInvitationService.updateRole(invitationId, formData.organizationRole)
            showSuccessNotification(i18n("invitation.details.update.success"))
            UI.getCurrent().navigate(MEMBERS_PAGE)
        }
    }
}
