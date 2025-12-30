package com.wajtr.baf.ui.views.product

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.user.Identity
import com.wajtr.baf.features.product.ProductRepository
import com.wajtr.baf.ui.components.ApplicationPage
import jakarta.annotation.security.PermitAll

@PermitAll
@Route("")
class ProductsOverviewPage(
    productRepository: ProductRepository,
    authenticationContext: AuthenticationContext,
    identity: Identity
) : ApplicationPage() {

    init {
        style.setPadding("3rem")

        h2("User info and granted authorities")
        ul {
            li("Current user id: " + identity.authenticatedUser.id)
            li("Current user email: " + identity.authenticatedUser.email)
            li("Current user name: " + identity.authenticatedUser.name)
            li("Tenant ID: " + identity.authenticatedTenant?.id)
            li("Granted authorities: " + identity.grantedAuthorities?.joinToString(", "))
        }

        h2("Products findAll()")
        val products = productRepository.findAll()
        ul {
            products.forEach {
                li(it.toString())
            }
        }

        verticalLayout {
            textField("Write into me")
            button("Click me")

            button(i18n("user.login.logout.label")) {
                onClick {
                    authenticationContext.logout()
                }
            }
        }
    }

}