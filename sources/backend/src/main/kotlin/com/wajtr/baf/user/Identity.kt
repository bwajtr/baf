package com.wajtr.baf.user

import com.wajtr.baf.authentication.AuthenticatedTenant
import com.wajtr.baf.authentication.apikey.TenantApiKeyAuthenticationToken
import com.wajtr.baf.authentication.oauth2.OAuth2TenantAuthenticationToken
import org.springframework.security.authentication.AnonymousAuthenticationToken
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

    val grantedRoles: Collection<String>
        get() = SecurityContextHolder.getContext().authentication?.authorities?.filter {
            it.toString().startsWith("ROLE_")
        }?.map { it.toString().substring(5) } ?: listOf()

    val grantedAuthorities: Collection<GrantedAuthority>
        get() = SecurityContextHolder.getContext().authentication?.authorities ?: listOf()

    private fun resolveAuthenticatedUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null) {
            return when (authentication) {
                is AnonymousAuthenticationToken -> throw NoAuthenticatedUserException()
                is TenantApiKeyAuthenticationToken -> throw NoAuthenticatedUserException() // API key auth has no user
                is OAuth2TenantAuthenticationToken -> authentication.user // oauth2 login
                is OAuth2AuthenticationToken -> throw NoAuthenticatedUserException() // not yet fully authenticated oauth2 login
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
                is TenantApiKeyAuthenticationToken -> authentication.tenant // API key authentication
                is OAuth2TenantAuthenticationToken -> authentication.tenant // oauth2 login
                is UsernamePasswordAuthenticationToken -> authentication.details as AuthenticatedTenant // login page
                is OAuth2AuthenticationToken -> null // not yet fully authenticated oauth2 login
                is AnonymousAuthenticationToken -> null // no tenant available when accessing public pages
                else -> throw UnknownAuthenticationTokenException()
            }
        }
        return null // no tenant context available
    }

    fun hasRole(role: String): Boolean {
        return this.grantedRoles.contains(role)
    }


}

class NoAuthenticatedUserException : RuntimeException("No user context available")
class UnknownAuthenticationTokenException : RuntimeException("Unknown authentication token")