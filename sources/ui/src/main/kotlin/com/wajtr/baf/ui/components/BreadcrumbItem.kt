package com.wajtr.baf.ui.components

import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.ListItem
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.router.RouterLink

class BreadcrumbItem private constructor() : ListItem() {

    constructor(text: String, anchorLink: String? = null) : this() {
        if (anchorLink != null) {
            add(Anchor(anchorLink, text))
        } else {
            add(Span(text))
        }
    }

    constructor(routerLink: RouterLink) : this() {
        add(routerLink)
    }

    init {
        style.set("display", "flex")
    }
}
