package com.wajtr.baf.ui.views.organization.member

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onClick
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.organization.invitation.ACCEPT_INVITATION_PAGE
import com.wajtr.baf.organization.invitation.AcceptInvitationResult
import com.wajtr.baf.organization.invitation.InvitationAcceptanceDetails
import com.wajtr.baf.organization.invitation.MemberInvitationRepository
import com.wajtr.baf.organization.invitation.MemberInvitationService
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import jakarta.annotation.security.PermitAll
import java.util.*

const val SESSION_ATTR_ORGANIZATION_ADDED = "organization-added"

@PermitAll
@Route(ACCEPT_INVITATION_PAGE, layout = MainLayout::class)
class AcceptInvitationPage(
    private val memberInvitationRepository: MemberInvitationRepository,
    private val memberInvitationService: MemberInvitationService
) : MainLayoutPage(), HasUrlParameter<String> {

    private lateinit var invitation: InvitationAcceptanceDetails
    private lateinit var invitationId: UUID

    init {
        setSizeFull()
        style.set("display", "flex")
        style.set("justify-content", "center")
        style.set("align-items", "center")
        style.set("padding", "2rem")
        style.set("background-color", "var(--vaadin-background-secondary)")
    }

    override fun setParameter(event: BeforeEvent, parameter: String) {
        invitationId = UUID.fromString(parameter)

        invitation = memberInvitationRepository.getInvitationForAcceptance(invitationId)
            ?: run {
                showErrorNotification(i18n("invitation.accept.not.found"))
                event.rerouteTo("/")
                return
            }

        buildUI()
    }

    override fun getPageTitle(): String {
        return i18n("invitation.accept.page.header", invitation.organizationName)
    }

    private fun buildUI() {
        val card = VerticalLayout().apply {
            isPadding = true
            isSpacing = true
            width = "100%"
            maxWidth = "500px"
            style.set("background-color", "var(--vaadin-background-primary)")
            style.set("border", "1px solid var(--vaadin-contrast-20pct)")
            style.set("border-radius", "var(--vaadin-radius-l)")
            style.set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.1)")
            style.set("padding", "2.5rem")
            style.set("text-align", "center")
            defaultHorizontalComponentAlignment = Alignment.CENTER
        }

        // Header
        val header = H2(i18n("invitation.accept.page.header", invitation.organizationName))
        header.style.set("margin", "0 0 1.5rem 0")
        card.add(header)

        // Invited by text
        val inviterName = invitation.invitedByName
        val roleName = i18n("role.${invitation.role}")
        val invitedByText = if (inviterName != null) {
            i18n("invitation.accept.invited.by", inviterName, invitation.organizationName, roleName)
        } else {
            i18n("invitation.accept.invited.by.unknown", invitation.organizationName, roleName)
        }
        val invitedByParagraph = Paragraph(invitedByText)
        invitedByParagraph.style.set("margin", "0.5rem 0")
        card.add(invitedByParagraph)

        // Sent to text
        val sentToParagraph = Paragraph(i18n("invitation.accept.sent.to", invitation.email))
        sentToParagraph.style.set("margin", "0.5rem 0")
        sentToParagraph.style.set("color", "var(--vaadin-text-color-secondary)")
        card.add(sentToParagraph)

        // Accept button
        card.button(i18n("invitation.accept.button")) {
            addThemeVariants(ButtonVariant.AURA_PRIMARY)
            addClassName("aura-accent-green")
            style.set("--vaadin-button-primary-background", "var(--aura-green)")
            onClick {
                acceptInvitation()
            }
        }

        add(card)
    }

    private fun acceptInvitation() {
        when (val result = memberInvitationService.acceptInvitation(invitationId)) {
            is AcceptInvitationResult.Success -> {
                // Set session attribute to show the organization switch hint
                VaadinSession.getCurrent().setAttribute(SESSION_ATTR_ORGANIZATION_ADDED, true)

                // Navigate to root to reload UI
                UI.getCurrent().page.setLocation("/")
            }
            is AcceptInvitationResult.Error -> {
                showErrorNotification(i18n(result.messageKey))
                UI.getCurrent().navigate("/")
            }
        }
    }
}
