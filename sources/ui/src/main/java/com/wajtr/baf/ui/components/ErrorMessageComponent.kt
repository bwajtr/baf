package com.wajtr.baf.ui.components

import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.textAlign
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.orderedlayout.BoxSizing
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.wajtr.baf.authentication.db.LOGIN_PATH
import com.wajtr.baf.core.i18n.i18n

/**
 * Error message with an icon, which is used mainly in the /account/... pages
 *
 * @author Bretislav Wajtr
 */
class ErrorMessageComponent(
    header: String,
    subHeader: String,
    text: String
) : VerticalLayout() {

    init {
        alignItems = FlexComponent.Alignment.CENTER
        maxWidth = "600px"
        width = "100%"
        textAlign = "center"
        boxSizing = BoxSizing.BORDER_BOX
        style.set("padding-left", "20px")
        style.set("padding-right", "20px")

        h4(header)

        image("frontend/img/error_large.png") {
            width = "80px"
        }

        p(subHeader)

        p {
            html(text)
            width = "100%"
        }

        button(i18n("users.back.to.login")) {
            onClick {
                UI.getCurrent().navigate(LOGIN_PATH)
            }
        }

    }

}
