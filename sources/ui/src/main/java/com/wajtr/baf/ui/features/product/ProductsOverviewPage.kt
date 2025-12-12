package com.wajtr.baf.ui.features.product

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import com.wajtr.baf.features.product.ProductRepository
import com.wajtr.baf.ui.components.ApplicationView
import jakarta.annotation.security.PermitAll

@PermitAll
@Route("")
class ProductsOverviewPage(
    productRepository: ProductRepository,
    authenticationContext: AuthenticationContext
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

        button("Logout") {
            onClick {
                authenticationContext.logout()
            }
        }
    }

}