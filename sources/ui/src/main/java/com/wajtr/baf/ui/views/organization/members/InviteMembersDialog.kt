package com.wajtr.baf.ui.views.organization.members

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.wajtr.baf.core.commons.HttpServletUtils
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.organization.invitation.MemberInvitationService
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.ui.vaadin.extensions.showSuccessNotification
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.validation.EmailValidator

class InviteMembersDialog(
    private val identity: Identity,
    private val memberInvitationService: MemberInvitationService,
    private val onInvitationsSent: () -> Unit
) : Dialog() {

    private val emailsField: TextArea
    private val roleComboBox: ComboBox<String>

    private val logger = org.slf4j.LoggerFactory.getLogger(InviteMembersDialog::class.java)

    init {
        headerTitle = i18n("members.invite.dialog.header")
        width = "500px"

        val content = VerticalLayout().apply {
            isPadding = false
            isSpacing = true
        }

        // Description
        content.add(Paragraph(i18n("members.invite.dialog.description")))

        // Email addresses text area
        emailsField = TextArea(i18n("members.invite.dialog.emails.label")).apply {
            placeholder = i18n("members.invite.dialog.emails.placeholder")
            setWidthFull()
            minHeight = "100px"
        }
        content.add(emailsField)

        // Role combobox
        roleComboBox = ComboBox<String>(i18n("members.invite.dialog.role.label")).apply {
            setItems(UserRole.USER_ROLE, UserRole.ADMIN_ROLE, UserRole.OWNER_ROLE)
            setItemLabelGenerator { role -> i18n("role.$role") }
            value = UserRole.USER_ROLE
            setWidthFull()
        }
        content.add(roleComboBox)

        // Button bar
        val buttonBar = HorizontalLayout().apply {
            setWidthFull()
            justifyContentMode = FlexComponent.JustifyContentMode.END

            val cancelButton = Button(i18n("core.ui.common.cancel")).apply {
                addClickListener { close() }
            }

            val sendButton = Button(i18n("members.invite.dialog.send.button")).apply {
                addThemeVariants(ButtonVariant.AURA_PRIMARY)
                addClickListener { sendInvitations() }
            }

            add(cancelButton, sendButton)
        }
        content.add(buttonBar)

        add(content)
    }

    private fun sendInvitations() {
        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")
        val currentUser = identity.authenticatedUser

        // Parse and validate emails
        val emails = emailsField.value.split(",", ";", "\n")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        if (emails.isEmpty()) {
            showErrorNotification(i18n("members.invite.dialog.emails.required"))
            return
        }

        // Validate email format and check for duplicates
        val emailValidator = EmailValidator()
        for (email in emails) {
            if (!emailValidator.isValid(email)) {
                showErrorNotification(i18n("members.invite.dialog.email.invalid", email))
                return
            }
            if (memberInvitationService.emailAlreadyInvited(email)) {
                showErrorNotification(i18n("members.invite.dialog.email.already.invited", email))
                return
            }
        }

        // Create invitations
        for (email in emails) {
            val invitationId = memberInvitationService.createInvitation(
                email = email,
                role = roleComboBox.value,
                tenantId = tenant.id,
                invitedBy = currentUser.id
            )
            val acceptanceUrl = HttpServletUtils.getServerBaseUrl() + "/$ACCEPT_INVITATION_PAGE/" + invitationId
            logger.info("Created invitation $invitationId for email $email with role ${roleComboBox.value}. Url is $acceptanceUrl")
        }

        close()
        showSuccessNotification(i18n("members.invite.dialog.success"))
        onInvitationsSent()
    }
}
