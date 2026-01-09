package com.wajtr.baf.ui.views.organization.member

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.organization.invitation.InviteMembersResult
import com.wajtr.baf.organization.invitation.MemberInvitationService
import com.wajtr.baf.organization.member.MemberManagementService
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.ui.vaadin.extensions.showSuccessNotification

class InviteMembersDialog(
    private val memberInvitationService: MemberInvitationService,
    memberManagementService: MemberManagementService,
    private val onInvitationsSent: () -> Unit
) : Dialog() {

    private val emailsField: TextArea
    private val roleComboBox: ComboBox<String>

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

        val availableRoles = memberManagementService.getAllowedRolesForInvitation()
        roleComboBox = ComboBox<String>(i18n("members.invite.dialog.role.label")).apply {
            setItems(availableRoles)
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
        val result = memberInvitationService.inviteMembers(
            emailsInput = emailsField.value,
            role = roleComboBox.value
        )

        when (result) {
            is InviteMembersResult.ValidationError -> {
                val errorMessage = if (result.parameter != null) {
                    i18n(result.messageKey, result.parameter!!)
                } else {
                    i18n(result.messageKey)
                }
                showErrorNotification(errorMessage)
            }
            is InviteMembersResult.Success -> {
                close()
                showSuccessNotification(i18n("members.invite.dialog.success"))
                onInvitationsSent()
            }
        }
    }
}
