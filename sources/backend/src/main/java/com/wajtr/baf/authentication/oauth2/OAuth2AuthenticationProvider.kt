package com.wajtr.baf.authentication.oauth2

import com.wajtr.baf.authentication.AuthenticationDetailsService
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class OAuth2AuthenticationProvider(
    private val authenticationDetailsService: AuthenticationDetailsService
) {

    fun buildOAuth2TenantAuthentication(
        authentication: OAuth2AuthenticationToken,
        desiredTenantId: UUID? = null
    ): OAuth2TenantAuthenticationToken {
        val oauth2User = authentication.principal

        // Extract user identifier
        val email = oauth2User?.getAttribute<String>("email")

        if (email != null) {
            // Load user details from database
            val dbDetails = authenticationDetailsService.loadAuthenticationDetails(email, desiredTenantId)
            // Combine OAuth2 authorities with database roles
            val combinedAuthorities = mutableSetOf(
                *oauth2User.authorities.toTypedArray(),
                *dbDetails.roles.toTypedArray()
            )

            // Create our custom authentication token
            return OAuth2TenantAuthenticationToken(
                oauth2User,
                combinedAuthorities,
                authentication.authorizedClientRegistrationId,
                dbDetails.user,
                dbDetails.tenant
            )
        } else throw OAuth2EmailNotProvidedException()
    }

}


class OAuth2EmailNotProvidedException() : AuthenticationException("Email attribute not found during OAuth2 Login")
