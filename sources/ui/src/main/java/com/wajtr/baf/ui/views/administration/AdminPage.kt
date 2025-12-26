package com.wajtr.baf.ui.views.administration

import com.github.mvysny.karibudsl.v10.h2
import com.vaadin.flow.router.Route
import com.wajtr.baf.core.auth.ADMIN_ROLE
import com.wajtr.baf.ui.components.ApplicationView
import jakarta.annotation.security.RolesAllowed

@RolesAllowed(ADMIN_ROLE)
@Route("admin")
class AdminPage : ApplicationView() {

    init {
        style.setPadding("3rem")

        h2("This is admin view")
    }

}