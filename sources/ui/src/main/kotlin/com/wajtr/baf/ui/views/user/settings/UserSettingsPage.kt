package com.wajtr.baf.ui.views.user.settings

import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.Route
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.base.MainLayout
import com.wajtr.baf.ui.base.ViewToolbar
import com.wajtr.baf.ui.components.MainLayoutPage
import com.wajtr.baf.ui.views.user.settings.parts.DeleteAccountComponent
import com.wajtr.baf.ui.views.user.settings.parts.PasswordChangeComponent
import com.wajtr.baf.ui.views.user.settings.parts.UserAccountComponent
import com.wajtr.baf.user.USER_SETTINGS_PAGE_URL
import jakarta.annotation.security.PermitAll

@PermitAll
@Route(USER_SETTINGS_PAGE_URL, layout = MainLayout::class)
class UserSettingsPage(
    userAccountComponent: UserAccountComponent,
    passwordChangeComponent: PasswordChangeComponent,
    deleteAccountComponent: DeleteAccountComponent
) : MainLayoutPage() {

    init {
        add(ViewToolbar(i18n("user.settings.page.header")))
        verticalLayout(false) {
            spacing = "3rem"
            maxWidth = "600px"
            addThemeVariants()

            add(userAccountComponent)
            add(passwordChangeComponent)
            add(deleteAccountComponent)
        }
    }

}