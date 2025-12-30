package com.wajtr.baf.ui.views.administration

import com.github.mvysny.karibudsl.v10.h2
import com.vaadin.flow.router.Route
import com.wajtr.baf.ui.components.ApplicationPage
import com.wajtr.baf.user.UserRole.OWNER_ROLE
import jakarta.annotation.security.RolesAllowed

@RolesAllowed(OWNER_ROLE)
@Route("admin")
class AdminPage : ApplicationPage() {

    init {
        style.setPadding("3rem")

        h2("This is admin view accessible only to owners")
    }

}