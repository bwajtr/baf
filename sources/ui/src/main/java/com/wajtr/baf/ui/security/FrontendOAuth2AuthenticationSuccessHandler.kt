package com.wajtr.baf.ui.security

import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler
import com.wajtr.baf.authentication.AuthenticationDetailsService
import com.wajtr.baf.authentication.oauth2.CoreOAuth2AuthenticationToken
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
    private val authenticationDetailsService: AuthenticationDetailsService
) : VaadinSavedRequestAwareAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        if (authentication is OAuth2AuthenticationToken) {
            val oauth2User = authentication.principal

            // Extract user identifier
            val email = oauth2User?.getAttribute<String>("email")

            if (email != null) {
                // Load user details from database
                val dbDetails = authenticationDetailsService.loadAuthenticationDetails(email)
                // Combine OAuth2 authorities with database roles
                val combinedAuthorities = mutableSetOf(
                    *authentication.authorities.toTypedArray(),
                    *dbDetails.roles.toTypedArray()
                )

                // Create our custom authentication token
                val customAuth = CoreOAuth2AuthenticationToken(
                    oauth2User,
                    combinedAuthorities,
                    authentication.authorizedClientRegistrationId,
                    dbDetails.user,
                    dbDetails.tenant
                )

                // Replace the authentication in the auth context
                SecurityContextHolder.getContext().authentication = customAuth
            }
        }

        // Continue with the default behavior
        super.onAuthenticationSuccess(request, response, authentication)
    }
}
