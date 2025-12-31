package com.wajtr.baf.ui.base

import com.github.mvysny.karibudsl.v10.avatar
import com.github.mvysny.karibudsl.v10.menuBar
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v10.span
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.SubMenu
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.menubar.MenuBarVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.security.AuthenticationContext
import com.wajtr.baf.authentication.ChangeAuthenticatedTenantService
import com.wajtr.baf.authentication.TenantSwitchResult
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.core.tenants.Tenant
import com.wajtr.baf.core.tenants.TenantRepository
import com.wajtr.baf.ui.vaadin.extensions.showNotification
import com.wajtr.baf.ui.views.legal.PUBLIC_PRIVACY_POLICY_VIEW
import com.wajtr.baf.ui.views.legal.PUBLIC_TERMS_OF_SERVICE_VIEW
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.UserRepository
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import java.util.*

@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class UserMenuBarComponent(
    private val identity: Identity,
    authenticationContext: AuthenticationContext,
    private val tenantRepository: TenantRepository,
    private val userRepository: UserRepository,
    private val changeAuthenticatedTenantService: ChangeAuthenticatedTenantService
) : HorizontalLayout() {

    init {
        isPadding = true
        isSpacing = false
        alignItems = FlexComponent.Alignment.CENTER
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        setWidthFull()

        avatar(identity.authenticatedUser.name) {
            colorIndex = 4
            style.set("--vaadin-avatar-size", "2rem")
        }

        menuBar {
            addThemeVariants(MenuBarVariant.AURA_TERTIARY)

            val item = addItem(createLabelComponent(identity))
            val subMenu: SubMenu = item.subMenu
            subMenu.addItem("User Settings")
            addOrganizationsSectionAsSubmenu(subMenu)
            subMenu.addSeparator()
            subMenu.addItem(i18n("legal.documents.termsOfService")).apply {
                onClick {
                    UI.getCurrent().page.open(PUBLIC_TERMS_OF_SERVICE_VIEW, "_blank")
                }
            }
            subMenu.addItem(i18n("legal.documents.privacyPolicy")).apply {
                onClick {
                    UI.getCurrent().page.open(PUBLIC_PRIVACY_POLICY_VIEW, "_blank")
                }
            }
            subMenu.addSeparator()
            subMenu.addItem(i18n("user.login.logout.label")).apply {
                onClick {
                    authenticationContext.logout()
                }
            }
        }

    }

    private fun addOrganizationsSectionAsSubmenu(subMenu: SubMenu) {
        val userTenantIds = userRepository.resolveTenantIdsOfUser(identity.authenticatedUser.id)
        if (userTenantIds.size > 1) {
            createOrganizationsSubmenu(subMenu, userTenantIds)
        }
    }

    private fun createOrganizationsSubmenu(
        subMenu: SubMenu,
        tenantIds: List<UUID>
    ) {
        val organizationsSubMenu = subMenu.addItem(i18n("user.menu.bar.switch.organization")).subMenu
        tenantIds.forEach { tenantId ->
            addOrganizationOption(tenantId, organizationsSubMenu)
        }
    }

    private fun addOrganizationOption(tenantId: UUID, organizationsSubMenu: SubMenu) {
        val tenant = tenantRepository.findById(tenantId)
        if (tenant != null) {
            organizationsSubMenu.addItem(tenant.organizationName).apply {
                if (isCurrentTenant(tenant)) {
                    isCheckable = true
                    isChecked = true
                }
                onClick {
                    switchToTenant(tenant)
                }
            }
        }
    }

    private fun switchToTenant(tenant: Tenant) {
        val tenantId = tenant.id
        if (tenantId != null) {
            val result = changeAuthenticatedTenantService.switchToTenant(tenantId)
            when (result) {
                TenantSwitchResult.TENANT_CHANGED -> UI.getCurrent().page.setLocation("/") // reload UI completely
                TenantSwitchResult.NOT_ALLOWED -> showNotification(i18n("user.menu.bar.cannot.switch.organization"))
            }
        }
    }

    private fun isCurrentTenant(tenant: Tenant): Boolean = tenant.id == identity.authenticatedTenant?.id

    private fun createLabelComponent(identity: Identity): Component {
        val authenticatedTenant = identity.authenticatedTenant
        return if (authenticatedTenant != null) {
            val tenant = tenantRepository.findById(authenticatedTenant.id)
            if (tenant != null && tenant.organizationName.isNotBlank()) {
                VerticalLayout().apply {
                    isSpacing = false
                    isPadding = false

                    span(identity.authenticatedUser.name)
                    span(tenant.organizationName).apply {
                        style.set("font-size", "0.7rem")
                        style.set("line-height", "0.8rem")
                        style.set("color", "var(--vaadin-text-color-secondary)")
                    }
                }
            } else Span(identity.authenticatedUser.name)
        } else Span(identity.authenticatedUser.name)
    }

}