package com.wajtr.baf.ui.views.user.common

import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.router.RouterLayout
import java.util.*

/**
 *
 * @author Bretislav Wajtr
 */
open class UserAccountRelatedBaseLayout(
    contactUsFooterComponent: ContactUsFooterComponent
) : Div(), RouterLayout {

    private val viewDisplayContainer: HorizontalLayout = horizontalLayout {
        setSizeFull()
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        style.set("padding", "50px 15px 15px 15px")
    }

    init {

        add(contactUsFooterComponent)
    }

    override fun showRouterLayoutContent(content: HasElement?) {
        if (content != null) {
            viewDisplayContainer.element
                .appendChild(Objects.requireNonNull(content.element))
        }
    }

}
