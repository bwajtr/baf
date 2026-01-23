package com.wajtr.baf.ui.views.product

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.router.Route
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.product.ProductRepository
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.base.ViewToolbar
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.user.Identity
import jakarta.annotation.security.PermitAll

@PermitAll
@Route("", layout = MainLayout::class)
class ProductsOverviewPage(
    productRepository: ProductRepository, identity: Identity
) : MainLayoutPage() {

    init {
        add(ViewToolbar(i18n("features.products.title")))

        h2("User info and granted authorities")
        ul {
            li("Current user id: " + identity.authenticatedUser.id)
            li("Current user email: " + identity.authenticatedUser.email)
            li("Current user name: " + identity.authenticatedUser.name)
            li("Tenant ID: " + identity.authenticatedTenant?.id)
            li("Granted authorities: " + identity.grantedAuthorities.joinToString(", "))
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
        }
    }

    override fun getPageTitle(): String {
        return i18n("features.products.title")
    }

}