package com.wajtr.baf.organization.apikey

import com.wajtr.baf.core.commons.generateRandomAlphanumeric
import com.wajtr.baf.organization.member.UserRole
import com.wajtr.baf.user.Identity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom

private const val API_KEY_LENGTH = 48L

@Service
@Transactional
class TenantApiKeyService(
    private val tenantApiKeyRepository: TenantApiKeyRepository,
    private val identity: Identity
) {

    private val secureRandom = SecureRandom()

    /**
     * Returns the existing API key for the current tenant, or creates a new one if none exists.
     */
    @PreAuthorize("hasAnyRole('${UserRole.OWNER_ROLE}', '${UserRole.ADMIN_ROLE}')")
    fun getOrCreateApiKey(): TenantApiKey {
        val tenantId = identity.authenticatedTenant?.id
            ?: throw IllegalStateException("No authenticated tenant found")

        return tenantApiKeyRepository.findByTenantId(tenantId)
            ?: tenantApiKeyRepository.insert(
                apiKey = generateApiKey(),
                tenantId = tenantId
            )
    }

    /**
     * Deletes the existing API key and generates a new one. The old key stops working immediately.
     */
    @PreAuthorize("hasAnyRole('${UserRole.OWNER_ROLE}', '${UserRole.ADMIN_ROLE}')")
    fun resetApiKey(): TenantApiKey {
        val tenantId = identity.authenticatedTenant?.id
            ?: throw IllegalStateException("No authenticated tenant found")

        tenantApiKeyRepository.deleteByTenantId(tenantId)
        return tenantApiKeyRepository.insert(
            apiKey = generateApiKey(),
            tenantId = tenantId
        )
    }

    private fun generateApiKey(): String {
        return secureRandom.generateRandomAlphanumeric(API_KEY_LENGTH)
    }
}
