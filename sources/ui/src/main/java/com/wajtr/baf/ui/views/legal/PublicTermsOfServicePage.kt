package com.wajtr.baf.ui.views.legal

import com.github.mvysny.karibudsl.v10.span
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.wajtr.baf.ui.components.ApplicationPage

/**
 * The terms of service view which is provided by the "legal" module is not available for unauthenticated
 * users -> therefore, we provide a route to that view for unauthenticated users here
 *
 * @author Bretislav Wajtr
 */
const val PUBLIC_TERMS_OF_SERVICE_VIEW = "accounts/public-terms-of-service"

@Route(PUBLIC_TERMS_OF_SERVICE_VIEW)
@AnonymousAllowed
class PublicTermsOfServicePage(
    // TODO fix this when legal documents are implemented
//    legalDocumentsDAO: LegalDocumentsDAO
) : ApplicationPage() {

    init {
        maxWidth = "900px"
        style.set("margin-left", "auto")
        style.set("margin-right", "auto")
        style.set("padding", "15px")

        span("Public Terms of Service View - to be implemented")
    }

}
