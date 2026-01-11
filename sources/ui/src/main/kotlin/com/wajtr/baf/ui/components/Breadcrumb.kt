package com.wajtr.baf.ui.components

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.init
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.html.Nav
import com.vaadin.flow.component.html.OrderedList


class Breadcrumb() : Nav() {
    private val list: OrderedList

    init {
        addClassName("breadcrumb")
        setAriaLabel("Breadcrumb")

        this.list = OrderedList()
        add(this.list)
    }

    constructor(vararg items: BreadcrumbItem) : this() {
        this.list.add(*items)
    }

    fun add(vararg items: BreadcrumbItem) {
        this.list.add(*items)
    }

    fun remove(vararg items: BreadcrumbItem) {
        this.list.remove(*items)
    }

    override fun removeAll() {
        this.list.removeAll()
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).breadcrumb(
    vararg items: BreadcrumbItem,
    block: (@VaadinDsl Breadcrumb).() -> Unit = {}
) = init(Breadcrumb(*items), block)