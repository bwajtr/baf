package com.wajtr.baf.ui.views.legal

import com.github.mvysny.karibudsl.v10.span
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.wajtr.baf.ui.components.ApplicationPage

/**
 * @author Bretislav Wajtr
 */
const val PUBLIC_PRIVACY_POLICY_VIEW = "accounts/public-privacy-policy"

@Route(PUBLIC_PRIVACY_POLICY_VIEW)
@AnonymousAllowed
class PublicPrivacyPolicyPage(
    //TODO fix this when legal documents are implemented
//    legalDocumentsDAO: LegalDocumentsDAO
) : ApplicationPage() {

    init {
        maxWidth = "900px"
        style.set("margin-left", "auto")
        style.set("margin-right", "auto")
        style.set("padding", "15px")

        span("Public Privacy Policy View - to be implemented")
    }
}

