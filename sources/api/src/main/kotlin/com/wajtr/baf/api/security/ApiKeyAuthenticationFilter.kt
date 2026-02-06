package com.wajtr.baf.api.security

import com.wajtr.baf.authentication.AuthenticatedTenant
import com.wajtr.baf.authentication.apikey.TenantApiKeyAuthenticationToken
import com.wajtr.baf.organization.apikey.TenantApiKeyRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that authenticates API requests using the `X-API-Key` header.
 *
 * When a valid API key is found in the header, a [TenantApiKeyAuthenticationToken]
 * is placed into the [SecurityContextHolder], identifying the tenant that owns the key.
 *
 * If the header is missing or the key is invalid, the filter does nothing and lets
 * Spring Security's authorization rules handle the response (401 Unauthorized).
 *
 * @author Bretislav Wajtr
 */
@Component
class ApiKeyAuthenticationFilter(
    private val tenantApiKeyRepository: TenantApiKeyRepository
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter::class.java)

    companion object {
        const val API_KEY_HEADER = "X-API-Key"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader(API_KEY_HEADER)

        if (!apiKey.isNullOrBlank()) {
            val tenantId = tenantApiKeyRepository.findTenantIdByApiKey(apiKey)
            if (tenantId != null) {
                val authentication = TenantApiKeyAuthenticationToken(
                    tenant = AuthenticatedTenant(id = tenantId),
                    apiKey = apiKey
                )
                SecurityContextHolder.getContext().authentication = authentication
                log.debug("Authenticated API request for tenant {}", tenantId)
            } else {
                log.debug("Invalid API key provided in request to {}", request.requestURI)
            }
        }

        filterChain.doFilter(request, response)
    }
}
