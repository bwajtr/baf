package com.wajtr.baf.ui.utils.browser

import com.vaadin.flow.server.AppShellSettings

/**
 *
 * @author Bretislav Wajtr
 */
object FavIconConfigurator {
    fun configureFavIcon(settings: AppShellSettings?) {
        settings?.addFavIcon("icon", "images/favicon.png", "32x32")
    }
}
