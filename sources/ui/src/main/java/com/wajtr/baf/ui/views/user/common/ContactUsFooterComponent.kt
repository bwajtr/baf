package com.wajtr.baf.ui.views.user.common

import com.wajtr.baf.ui.views.legal.PublicTermsOfServiceView
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.wajtr.baf.core.ApplicationProperties
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.views.legal.PublicPrivacyPolicyView
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


/**
 * Footer of the /account/... pages containing support email and links to terms of service and privacy policy
 *
 * @author Bretislav Wajtr
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class ContactUsFooterComponent(
    applicationProperties: ApplicationProperties
) : Div() {

    init {
        style.set("padding-top", "45px")
        width = "100%"

        horizontalLayout {
            isMargin = false
            isPadding = false

            justifyContentMode = FlexComponent.JustifyContentMode.CENTER

            div {
                span(i18n("user.contact.us.at")) {
                    style.set("font-size", "small")
                    style.set("padding-right", "4px")
                }

                anchor(
                    href = "mailto: ${applicationProperties.appSupportEmailAddress}",
                    text = applicationProperties.appSupportEmailAddress
                ) {
                    style.set("font-size", "small")
                }
            }
        }

        horizontalLayout {
            isMargin = false
            isPadding = false

            justifyContentMode = FlexComponent.JustifyContentMode.CENTER

            routerLink(
                text = i18n("legal.documents.termsOfService"),
                viewType = PublicTermsOfServiceView::class
            ) {
                style.set("font-size", "small")
            }
            span("•") {
                style.set("margin", "0 5px 0 5px")
            }
            routerLink(
                text = i18n("legal.documents.privacyPolicy"),
                viewType = PublicPrivacyPolicyView::class
            ) {
                style.set("margin", "0")
                style.set("font-size", "small")
            }
        }

    }
}
