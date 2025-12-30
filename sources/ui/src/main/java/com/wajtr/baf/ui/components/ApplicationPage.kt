package com.wajtr.baf.ui.components

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.HasDynamicTitle
import com.wajtr.baf.core.i18n.i18n

/**
 * A common base for all routes/views in the application.
 *
 * @author Bretislav Wajtr
 */
open class ApplicationPage : Div(), HasDynamicTitle {

    override fun getPageTitle(): String {
        return i18n("application.title")
    }
}
