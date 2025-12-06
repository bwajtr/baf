package com.wajtr.baf.ui.features.product

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.li
import com.github.mvysny.karibudsl.v10.span
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.ul
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.lumo.LumoUtility
import com.wajtr.baf.features.product.ProductRepository
import com.wajtr.baf.ui.components.ApplicationView

@Route("")
class ProductsOverviewPage(
    productRepository: ProductRepository
) : ApplicationView() {

    init {
        style.setPadding("3rem")

        span("Yay, I'm here!")

        val products = productRepository.findAll()
        ul {
            products.forEach {
                li(it.toString())
            }
        }

        horizontalLayout {
            textField("Write into me")
            button("Click me")
        }
    }

}