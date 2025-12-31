package com.wajtr.baf.ui.views.administration

import com.github.mvysny.karibudsl.v10.h2
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.Route
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.base.ViewToolbar
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.user.UserRole.OWNER_ROLE
import jakarta.annotation.security.RolesAllowed

@RolesAllowed(OWNER_ROLE)
@Route("admin", layout = MainLayout::class)
@Menu(order = 1.0, icon = "vaadin:cog")
class AdminPage : MainLayoutPage() {

    init {
        add(ViewToolbar("Administration"))

        h2("This is admin view accessible only to owners")
    }

}