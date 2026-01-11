package com.wajtr.baf.ui.views.organization.settings.parts

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v10.span
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.spring.security.AuthenticationContext
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.core.tenants.TenantRepository
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.UserRepository
import com.wajtr.baf.organization.member.UserRoleTenantRepository
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class DeleteOrganizationComponent(
    private val identity: Identity,
    private val tenantRepository: TenantRepository,
    private val userRepository: UserRepository,
    private val userRoleTenantRepository: UserRoleTenantRepository,
    private val authenticationContext: AuthenticationContext
) : VerticalLayout() {

    private val deleteButton: Button
    private val warningText: Span

    init {
        isPadding = false

        // Header in red
        val header = H2(i18n("organization.delete.header"))
        header.style.set("color", "var(--aura-red)")
        add(header)

        // Warning text
        warningText = span(i18n("organization.delete.warning")) {
            style.set("margin-bottom", "1rem")
        }

        // Delete button in red
        deleteButton = button(i18n("organization.delete.button")) {
            addThemeVariants(ButtonVariant.AURA_DANGER)
            onClick {
                showDeleteConfirmation()
            }
        }
    }

    private fun showDeleteConfirmation() {
        val dialog = ConfirmDialog()
        dialog.setHeader(i18n("organization.delete.confirm.title"))
        dialog.setText(i18n("organization.delete.confirm.message"))

        dialog.setCancelable(true)
        dialog.setConfirmText(i18n("organization.delete.confirm.yes"))
        dialog.setCancelText(i18n("organization.delete.confirm.no"))

        dialog.setConfirmButtonTheme("error primary")

        dialog.addConfirmListener {
            deleteOrganization()
        }

        dialog.open()
    }

    private fun deleteOrganization() {
        val tenant = identity.authenticatedTenant
            ?: throw IllegalStateException("No authenticated tenant found")

        // Get all user IDs in this tenant
        val userIds = userRoleTenantRepository.getUserIdsForTenant(tenant.id)

        // Delete all users in the tenant
        userIds.forEach { userId ->
            val userTenants = userRepository.resolveTenantIdsOfUser(userId)
            if (userTenants.size == 1 && userTenants[0] == tenant.id) {
                // if this is the only tenant of this user, then remove the user along with the tenant
                userRepository.remove(userId)
            }
        }

        // Delete the tenant itself (CASCADE will clean up all data in the database)
        val success = tenantRepository.deleteById(tenant.id) > 0

        if (success) {
            // Logout after successful deletion
            authenticationContext.logout()
        } else {
            showErrorNotification(i18n("organization.delete.failure"))
        }
    }

}
