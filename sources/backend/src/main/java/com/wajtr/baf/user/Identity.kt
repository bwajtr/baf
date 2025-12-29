package com.wajtr.baf.user

import com.wajtr.baf.authentication.AuthenticatedTenant
import com.wajtr.baf.authentication.oauth2.CoreOAuth2AuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component

@Component
class Identity {

    val authenticatedUser: User
        get() = this.resolveAuthenticatedUser()

    val authenticatedTenant: AuthenticatedTenant? // can be null during e.g. during oauth2 authentication process
        get() = this.resolveAuthenticatedTenant()

    val grantedAuthorities: Collection<GrantedAuthority>?
        get() = SecurityContextHolder.getContext().authentication?.authorities

    private fun resolveAuthenticatedUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null) {
            return when (authentication) {
                is CoreOAuth2AuthenticationToken -> authentication.user // oauth2 login
                is UsernamePasswordAuthenticationToken -> authentication.principal as User // login page
                else -> throw UnknownAuthenticationTokenException()
            }
        }
        throw NoAuthenticatedUserException()
    }

    private fun resolveAuthenticatedTenant(): AuthenticatedTenant? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null) {
            return when (authentication) {
                is CoreOAuth2AuthenticationToken -> authentication.tenant // oauth2 login
                is UsernamePasswordAuthenticationToken -> authentication.details as AuthenticatedTenant // login page
                is OAuth2AuthenticationToken -> null // not yet fully authenticated oauth2 login
                else -> throw UnknownAuthenticationTokenException()
            }
        }
        return null // no tenant context available
    }


}

class NoAuthenticatedUserException : RuntimeException("No user context available")
class UnknownAuthenticationTokenException : RuntimeException("Unknown authentication token")