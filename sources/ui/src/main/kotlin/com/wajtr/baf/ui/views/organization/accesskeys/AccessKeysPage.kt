package com.wajtr.baf.ui.views.organization.accesskeys

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.router.Route
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.organization.apikey.TenantApiKeyService
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.base.ViewToolbar
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.ui.vaadin.extensions.showSuccessNotification
import jakarta.annotation.security.RolesAllowed

const val ACCESS_KEYS_PAGE = "access-keys"

@RolesAllowed(UserRole.OWNER_ROLE, UserRole.ADMIN_ROLE)
@Route(ACCESS_KEYS_PAGE, layout = MainLayout::class)
class AccessKeysPage(
    private val tenantApiKeyService: TenantApiKeyService
) : MainLayoutPage() {

    private lateinit var apiKeyField: PasswordField
    private var currentApiKey: String = ""

    init {
        add(ViewToolbar(i18n("accesskeys.page.header")))

        verticalLayout(false) {
            maxWidth = "600px"

            // Description
            span(i18n("accesskeys.page.description")) {
                style.set("color", "var(--vaadin-text-color-secondary)")
                style.set("margin-bottom", "1rem")
            }

            // API key field (read-only PasswordField with built-in reveal toggle)
            apiKeyField = passwordField(i18n("accesskeys.field.label")) {
                isReadOnly = true
                width = "100%"
            }

            // Action buttons
            horizontalLayout {
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
                isSpacing = true

                button(i18n("accesskeys.button.copy"), VaadinIcon.COPY.create()) {
                    addThemeVariants(ButtonVariant.AURA_PRIMARY)
                    onClick { copyApiKeyToClipboard() }
                }

                button(i18n("accesskeys.button.reset"), VaadinIcon.REFRESH.create()) {
                    addThemeVariants(ButtonVariant.AURA_DANGER)
                    onClick { showResetConfirmation() }
                }
            }
        }

        loadApiKey()
    }

    private fun loadApiKey() {
        val tenantApiKey = tenantApiKeyService.getOrCreateApiKey()
        currentApiKey = tenantApiKey.apiKey
        apiKeyField.value = currentApiKey
    }

    private fun copyApiKeyToClipboard() {
        UI.getCurrent().page.executeJs(
            "navigator.clipboard.writeText($0).then(function() { })",
            currentApiKey
        )
        showSuccessNotification(i18n("accesskeys.copy.success"))
    }

    private fun showResetConfirmation() {
        val dialog = ConfirmDialog()
        dialog.setHeader(i18n("accesskeys.reset.confirm.title"))
        dialog.setText(i18n("accesskeys.reset.confirm.message"))

        dialog.setCancelable(true)
        dialog.setConfirmText(i18n("accesskeys.reset.confirm.yes"))
        dialog.setCancelText(i18n("accesskeys.reset.confirm.no"))

        dialog.setConfirmButtonTheme("error primary")

        dialog.addConfirmListener {
            resetApiKey()
        }

        dialog.open()
    }

    private fun resetApiKey() {
        val tenantApiKey = tenantApiKeyService.resetApiKey()
        currentApiKey = tenantApiKey.apiKey
        apiKeyField.value = currentApiKey
        showSuccessNotification(i18n("accesskeys.reset.success"))
    }
}
