package com.wajtr.baf.core.auth

import com.wajtr.baf.core.auth.token.CoreOAuth2AuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class Identity {

    val authenticatedUser: AuthenticatedUser
        get() = this.resolveAuthenticatedUser()

    val authenticatedTenant: AuthenticatedTenant
        get() = this.resolveAuthenticatedTenant()

    val grantedAuthorities: Collection<GrantedAuthority>?
        get() = SecurityContextHolder.getContext().authentication?.authorities

    private fun resolveAuthenticatedUser(): AuthenticatedUser {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null) {
            return when (authentication) {
                is CoreOAuth2AuthenticationToken -> authentication.user // frontend login
                else -> throw UnknownAuthenticationTokenException()
            }
        }
        throw NoAuthenticatedUserException()
    }

    private fun resolveAuthenticatedTenant(): AuthenticatedTenant {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null) {
            return when (authentication) {
                is CoreOAuth2AuthenticationToken -> authentication.tenant // frontend login
                else -> throw UnknownAuthenticationTokenException()
            }
        }
        throw NoAuthenticatedUserException()
    }


}

class NoAuthenticatedUserException : RuntimeException("No user context available")
class UnknownAuthenticationTokenException : RuntimeException("Unknown authentication token")