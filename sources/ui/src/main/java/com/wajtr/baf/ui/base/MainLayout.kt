package com.wajtr.baf.ui.base

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.dom.Style
import com.vaadin.flow.server.menu.MenuConfiguration
import com.vaadin.flow.server.menu.MenuEntry
import com.wajtr.baf.core.i18n.i18n
import jakarta.annotation.security.PermitAll
import java.util.function.Consumer

@PermitAll
class MainLayout(
    userMenuBarComponent: UserMenuBarComponent
) : AppLayout() {

    init {
        primarySection = Section.DRAWER
        addToDrawer(
            createHeader(),
            Scroller(createSideNav()),
            userMenuBarComponent
        )
    }

    private fun createHeader(): Component {
        val appLogo = Image("images/appbrand/logo.png", "logo")
        appLogo.addClassName("app-logo")

        val appName = Span(i18n("application.title"))
        appName.style.setFontWeight(Style.FontWeight.BOLD)
        appName.style.set("font-size", "1.2rem")

        val header = HorizontalLayout(appLogo, appName)
        header.style.set("padding-left", "var(--vaadin-padding-m)")
        header.alignItems = FlexComponent.Alignment.CENTER
        header.style.set("margin-bottom", "1.5rem")
        header.style.set("margin-top", "1.2rem")
        return header
    }

    private fun createSideNav(): SideNav {
        val nav = SideNav()
        MenuConfiguration.getMenuEntries()
            .forEach(Consumer { entry: MenuEntry? -> nav.addItem(createSideNavItem(entry!!)) })
        return nav
    }

    private fun createSideNavItem(menuEntry: MenuEntry): SideNavItem {
        if (menuEntry.icon() != null) {
            return SideNavItem(menuEntry.title(), menuEntry.path(), Icon(menuEntry.icon()))
        } else {
            return SideNavItem(menuEntry.title(), menuEntry.path())
        }
    }
}
