package com.wajtr.baf.ui.base

import com.github.mvysny.karibudsl.v10.avatar
import com.github.mvysny.karibudsl.v10.menuBar
import com.github.mvysny.karibudsl.v10.onClick
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.SubMenu
import com.vaadin.flow.component.menubar.MenuBarVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.spring.security.AuthenticationContext
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.views.legal.PUBLIC_PRIVACY_POLICY_VIEW
import com.wajtr.baf.ui.views.legal.PUBLIC_TERMS_OF_SERVICE_VIEW
import com.wajtr.baf.user.Identity
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class UserMenuBarComponent(
    identity: Identity,
    authenticationContext: AuthenticationContext
) : HorizontalLayout() {

    init {
        isPadding = true
        isSpacing = false
        alignItems = FlexComponent.Alignment.CENTER
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        setWidthFull()

        avatar(identity.authenticatedUser.name) {
            colorIndex = 4
        }

        menuBar {
            addThemeVariants(MenuBarVariant.AURA_TERTIARY)

            val item = addItem(identity.authenticatedUser.name)
            val subMenu: SubMenu = item.subMenu
            subMenu.addItem("Profile")
            subMenu.addSeparator()
            subMenu.addItem(i18n("legal.documents.termsOfService")).apply {
                onClick {
                    UI.getCurrent().page.open(PUBLIC_TERMS_OF_SERVICE_VIEW, "_blank")
                }
            }
            subMenu.addItem(i18n("legal.documents.privacyPolicy")).apply {
                onClick {
                    UI.getCurrent().page.open(PUBLIC_PRIVACY_POLICY_VIEW, "_blank")
                }
            }
            subMenu.addSeparator()
            subMenu.addItem(i18n("user.login.logout.label")).apply {
                onClick {
                    authenticationContext.logout()
                }
            }
        }

    }

}