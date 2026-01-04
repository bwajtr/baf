package com.wajtr.baf.ui.views.organization.members

import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.organization.member.DenialReason

/**
 * Shows a dialog explaining why a member management operation was denied.
 */
fun showOperationDeniedDialog(reason: DenialReason) {
    val (title, message) = when (reason) {
        DenialReason.LAST_OWNER_CANNOT_LEAVE -> Pair(
            i18n("members.last.owner.cannot.leave.title"),
            i18n("members.last.owner.cannot.leave.message")
        )
        DenialReason.LAST_OWNER_CANNOT_BE_REMOVED -> Pair(
            i18n("members.last.owner.cannot.remove.title"),
            i18n("members.last.owner.cannot.remove.message")
        )
        DenialReason.LAST_OWNER_ROLE_CANNOT_BE_CHANGED -> Pair(
            i18n("members.last.owner.cannot.change.role.title"),
            i18n("members.last.owner.cannot.change.role.message")
        )
    }

    val dialog = ConfirmDialog()
    dialog.setHeader(title)
    dialog.setText(message)
    dialog.setCancelable(false)
    dialog.setConfirmText(i18n("core.ui.common.ok"))
    dialog.open()
}
