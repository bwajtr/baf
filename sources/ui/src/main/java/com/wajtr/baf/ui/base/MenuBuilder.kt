package com.wajtr.baf.ui.base

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.server.auth.AccessAnnotationChecker
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.views.organization.member.MembersPage
import com.wajtr.baf.ui.views.organization.settings.OrganizationSettingsPage
import com.wajtr.baf.ui.views.product.ProductsOverviewPage
import org.springframework.stereotype.Component as SpringComponent

/**
 * Builds the application navigation menu with proper hierarchy and security-aware visibility.
 * Menu items are only shown if the current user has access to the corresponding view.
 */
@SpringComponent
class MenuBuilder(
    private val accessAnnotationChecker: AccessAnnotationChecker
) {

    fun build(): SideNav {
        val nav = SideNav()

        // Top-level items
        addItemIfAccessible(nav, ProductsOverviewPage::class.java, "menu.products", VaadinIcon.CLIPBOARD_CHECK)

        // Organization section
        createOrganizationSection(nav)

        return nav
    }

    private fun createOrganizationSection(nav: SideNav) {

        val children = listOfNotNull(
            createItemIfAccessible(MembersPage::class.java, "menu.organization.members", VaadinIcon.USERS),
            createItemIfAccessible(OrganizationSettingsPage::class.java, "menu.organization.settings", VaadinIcon.COG)
        )

        // Only create the section if there are visible children
        if (children.isEmpty()) {
            return
        }

        // Create parent item without navigation path (non-link parent)
        val section = SideNavItem(i18n("menu.organization"))
        section.prefixComponent = Icon(VaadinIcon.BUILDING)
        section.isExpanded = true
        children.forEach { section.addItem(it) }

        nav.addItem(section)
    }

    private fun addItemIfAccessible(
        nav: SideNav,
        viewClass: Class<out Component>,
        i18nKey: String,
        icon: VaadinIcon
    ) {
        createItemIfAccessible(viewClass, i18nKey, icon)?.let { nav.addItem(it) }
    }

    private fun createItemIfAccessible(
        viewClass: Class<out Component>,
        i18nKey: String,
        icon: VaadinIcon
    ): SideNavItem? {
        if (!accessAnnotationChecker.hasAccess(viewClass)) {
            return null
        }

        return SideNavItem(i18n(i18nKey), viewClass, Icon(icon))
    }
}
