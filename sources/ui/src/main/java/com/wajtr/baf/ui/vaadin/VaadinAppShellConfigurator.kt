package com.wajtr.baf.ui.vaadin

import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Viewport
import com.vaadin.flow.server.AppShellSettings
import com.vaadin.flow.theme.aura.Aura
import com.wajtr.baf.ui.utils.browser.FavIconConfigurator

@Viewport("width=device-width, initial-scale=1, user-scalable=no")
@StyleSheet(Aura.STYLESHEET)
class VaadinAppShellConfigurator : AppShellConfigurator {

    override fun configurePage(settings: AppShellSettings?) {
        FavIconConfigurator.configureFavIcon(settings)
    }

}