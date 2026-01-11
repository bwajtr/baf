package com.wajtr.baf.ui.base

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.dom.Style
import com.wajtr.baf.core.i18n.i18n
import jakarta.annotation.security.PermitAll

@PermitAll
class MainLayout(
    menuBuilder: MenuBuilder,
    userMenuBarComponent: UserMenuBarComponent
) : AppLayout() {

    init {
        primarySection = Section.DRAWER
        addToDrawer(
            createHeader(),
            Scroller(menuBuilder.build()),
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
}
