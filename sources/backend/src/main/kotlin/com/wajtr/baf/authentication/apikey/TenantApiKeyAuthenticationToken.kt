package com.wajtr.baf.authentication.apikey

import com.wajtr.baf.authentication.AuthenticatedTenant
import org.springframework.security.authentication.AbstractAuthenticationToken
import java.util.*

/**
 * Authentication token representing a request authenticated via a tenant API key.
 *
 * API key authentication identifies a *tenant*, not a specific user.
 * The principal is the tenant ID, and the credentials are the API key string.
 *
 * @author Bretislav Wajtr
 */
class TenantApiKeyAuthenticationToken(
    val tenant: AuthenticatedTenant,
    private val apiKey: String
) : AbstractAuthenticationToken(emptyList()) {

    init {
        isAuthenticated = true
    }

    override fun getCredentials(): String = apiKey

    override fun getPrincipal(): UUID = tenant.id
}
