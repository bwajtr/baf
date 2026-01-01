package com.wajtr.baf.ui.views.user.settings

import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.Route
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.base.ViewToolbar
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.ui.views.user.settings.parts.UserAccountComponent
import jakarta.annotation.security.PermitAll

const val USER_SETTINGS_PAGE = "user-settings"

@PermitAll
@Route(USER_SETTINGS_PAGE, layout = MainLayout::class)
class UserSettingsPage(
    userAccountComponent: UserAccountComponent,
) : MainLayoutPage() {

    init {
        add(ViewToolbar(i18n("user.settings.page.header")))

        verticalLayout(false) {
            setMaxWidth("400px")
            add(userAccountComponent)
        }
    }

}