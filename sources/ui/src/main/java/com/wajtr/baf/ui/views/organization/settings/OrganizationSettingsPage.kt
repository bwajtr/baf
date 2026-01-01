package com.wajtr.baf.ui.views.organization.settings

import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.Route
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.base.ViewToolbar
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.ui.views.organization.settings.parts.DeleteOrganizationComponent
import com.wajtr.baf.ui.views.organization.settings.parts.OrganizationDetailsComponent
import com.wajtr.baf.user.UserRole
import jakarta.annotation.security.RolesAllowed

const val ORGANIZATION_SETTINGS_PAGE = "organization-settings"

@RolesAllowed(UserRole.OWNER_ROLE)
@Route(ORGANIZATION_SETTINGS_PAGE, layout = MainLayout::class)
@Menu(order = 2.0, icon = "vaadin:cog")
class OrganizationSettingsPage(
    organizationDetailsComponent: OrganizationDetailsComponent,
    deleteOrganizationComponent: DeleteOrganizationComponent
) : MainLayoutPage() {

    init {
        add(ViewToolbar(i18n("organization.settings.page.header")))
        verticalLayout(false) {
            spacing = "3rem"
            maxWidth = "600px"
            addThemeVariants()

            add(organizationDetailsComponent)
            add(deleteOrganizationComponent)
        }
    }

}