package com.wajtr.baf.ui.security

import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler
import com.wajtr.baf.authentication.oauth2.OAuth2AuthenticationProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component

/**
 * Custom authentication success handler that replaces the OAuth2AuthenticationToken
 * with our custom CoreOAuth2AuthenticationToken containing database-loaded user details, user roles
 * and tenant information.
 *
 * @author Bretislav Wajtr
 */
@Component
class FrontendOAuth2AuthenticationSuccessHandler(
    private val oAuth2AuthenticationProvider: OAuth2AuthenticationProvider
) : VaadinSavedRequestAwareAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        if (authentication is OAuth2AuthenticationToken) {
            // this adds the resolved tenant information to the authentication
            val authenticationWithTenant = oAuth2AuthenticationProvider.buildOAuth2TenantAuthentication(authentication)
            SecurityContextHolder.getContext().authentication = authenticationWithTenant
        }

        // Continue with the default behavior
        super.onAuthenticationSuccess(request, response, authentication)
    }
}
