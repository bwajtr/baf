package com.wajtr.baf.ui.base

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout

class ViewToolbar(viewTitle: String?, vararg components: Component?) : Composite<HorizontalLayout>() {
    init {
        val layout = getContent()
        layout.addClassName("main-layout-view-toolbar")
        layout.isPadding = false
        layout.isWrap = true
        layout.setWidthFull()

        val drawerToggle = DrawerToggle()

        val title = H1(viewTitle)

        val toggleAndTitle = HorizontalLayout(drawerToggle, title)
        toggleAndTitle.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        layout.add(toggleAndTitle)
        layout.setFlexGrow(1.0, toggleAndTitle)

        if (components.size > 0) {
            val actions = HorizontalLayout(*components)
            actions.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN)
            layout.add(actions)
        }
    }

    companion object {
        fun group(vararg components: Component?): Component {
            val group = HorizontalLayout(*components)
            group.setWrap(true)
            return group
        }
    }
}
