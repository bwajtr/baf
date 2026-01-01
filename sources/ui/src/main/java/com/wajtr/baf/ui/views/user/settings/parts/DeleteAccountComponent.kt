package com.wajtr.baf.ui.views.user.settings.parts

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.html
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v10.span
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.internal.HtmlUtils
import com.vaadin.flow.spring.security.AuthenticationContext
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.UserRepository
import com.wajtr.baf.user.UserRoleTenantService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class DeleteAccountComponent(
    private val identity: Identity,
    private val userRepository: UserRepository,
    private val userRoleTenantService: UserRoleTenantService,
    private val authenticationContext: AuthenticationContext
) : VerticalLayout() {

    private val deleteButton: Button
    private val warningText: Span
    private val ownerWarningText: Span

    init {
        isPadding = false

        // Header in red
        val header = H2(i18n("user.settings.account.delete.header"))
        header.style.set("color", "var(--aura-red)")
        add(header)

        // Warning text
        warningText = span(i18n("user.settings.account.delete.warning")) {
            style.set("margin-bottom", "1rem")
        }

        // Owner warning text (initially hidden)
        ownerWarningText = span {
            style.set("margin-bottom", "1rem")
            isVisible = false
        }

        // Delete button in red
        deleteButton = button(i18n("user.settings.account.delete.button")) {
            addThemeVariants(ButtonVariant.AURA_DANGER)
            onClick {
                showDeleteConfirmation()
            }
        }

        checkOwnershipAndUpdateUI()
    }

    private fun checkOwnershipAndUpdateUI() {
        val user = identity.authenticatedUser
        val ownedTenants = userRoleTenantService.getTenantsWhereUserIsOwner(user.id)

        if (ownedTenants.isNotEmpty()) {
            // User is owner in some organizations - disable delete and show warning
            val organizationNames = ownedTenants.joinToString(", ") { HtmlUtils.escape(it.organizationName) }
            ownerWarningText.html(i18n("user.settings.account.delete.owner.warning", organizationNames))
            ownerWarningText.isVisible = true
            warningText.isVisible = false
            deleteButton.isEnabled = false
        } else {
            // User can delete account
            ownerWarningText.isVisible = false
            warningText.isVisible = true
            deleteButton.isEnabled = true
        }
    }

    private fun showDeleteConfirmation() {
        val dialog = ConfirmDialog()
        dialog.setHeader(i18n("user.settings.account.delete.confirm.title"))
        dialog.setText(i18n("user.settings.account.delete.confirm.message"))
        
        dialog.setCancelable(true)
        dialog.setConfirmText(i18n("user.settings.account.delete.confirm.yes"))
        dialog.setCancelText(i18n("user.settings.account.delete.confirm.no"))
        
        dialog.setConfirmButtonTheme("error primary")
        
        dialog.addConfirmListener {
            deleteAccount()
        }
        
        dialog.open()
    }

    private fun deleteAccount() {
        val user = identity.authenticatedUser

        try {
            val success = userRepository.remove(user.id)
            
            if (success) {
                authenticationContext.logout()
            } else {
                showErrorNotification(i18n("user.settings.account.delete.failure"))
            }
        } catch (_: Exception) {
            showErrorNotification(i18n("user.settings.account.delete.failure"))
        }
    }

}