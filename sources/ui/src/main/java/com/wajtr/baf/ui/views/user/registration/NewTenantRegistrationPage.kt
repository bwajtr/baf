package com.wajtr.baf.ui.views.user.registration

import com.github.mvysny.karibudsl.v10.h1
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.kaributools.textAlign
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.wajtr.baf.authentication.db.DatabaseBasedAuthenticationProvider
import com.wajtr.baf.core.commons.HttpServletUtils
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.vaadin.extensions.getTimezone
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.ui.views.user.common.UserAccountRelatedBaseLayout
import com.wajtr.baf.user.registration.UserAndTenantRegistrationRequest
import com.wajtr.baf.user.registration.UserRegistrationResult
import com.wajtr.baf.user.registration.UserRegistrationResultStatus
import com.wajtr.baf.user.registration.UserRegistrationService
import java.net.InetAddress

/**
 *
 * @author Bretislav Wajtr
 */
const val REGISTRATION_FORM_VIEW = "accounts/register"

@Route(REGISTRATION_FORM_VIEW, layout = UserAccountRelatedBaseLayout::class)
@AnonymousAllowed
class RegistrationView(
    databaseBasedAuthenticationProvider: DatabaseBasedAuthenticationProvider,
    userRegistrationService: UserRegistrationService
) : AbstractRegistrationView(
    databaseBasedAuthenticationProvider,
    userRegistrationService
) {

    override fun createUI() {
        formLayout = verticalLayout {
            maxWidth = "300px"
            isPadding = false

            h1(i18n("users.registration.header")) {
                textAlign = "center"
                width = "100%"
                style.set("margin-bottom", "1rem")
            }

            form = createRegistrationFormComponent()
            add(form)
        }

        bindModel()
    }

    override fun processRegistration(regData: RegistrationFormData) { // attempt to register user to a new tenant
        val registrationRequest = UserAndTenantRegistrationRequest(
            regData.fullName,
            regData.email,
            regData.password,
            InetAddress.getByName(HttpServletUtils.getClientIp()),
            VaadinSession.getCurrent().locale,
            VaadinSession.getCurrent().getTimezone(),
        )
        val userRegistrationResult: UserRegistrationResult =
            userRegistrationService.registerUserOfNewTenant(registrationRequest)
        when (userRegistrationResult.status) {
            UserRegistrationResultStatus.OK -> performLogin(regData.email, regData.password)
            UserRegistrationResultStatus.ERROR_DUPLICATE -> {
                emailTextField.isInvalid = true
                emailTextField.errorMessage = i18n("users.registration.email.duplicate")
            }

            UserRegistrationResultStatus.ERROR_INVITATION_EXISTS -> {
                emailTextField.isInvalid = true
                emailTextField.errorMessage = i18n("users.registration.email.invitation.exists")
            }

            UserRegistrationResultStatus.ERROR_INVALID_INVITATION_ID, UserRegistrationResultStatus.ERROR_INVALID_EMAIL_TOKEN -> {
                showErrorNotification(i18n("users.registration.failed"))
            }
        }
    }
}
