package com.wajtr.baf.authentication

import com.wajtr.baf.authentication.db.DatabaseBasedAuthenticationProvider
import com.wajtr.baf.authentication.oauth2.OAuth2AuthenticationProvider
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.UnknownAuthenticationTokenException
import com.wajtr.baf.user.UserRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class ChangeAuthenticatedTenantService(
    private val identity: Identity,
    private val userRepository: UserRepository,
    private val databaseBasedAuthenticationProvider: DatabaseBasedAuthenticationProvider,
    private val oAuth2AuthenticationProvider: OAuth2AuthenticationProvider
) {

    fun switchToTenant(desiredTenantId: UUID): TenantSwitchResult {
        // verify user has access to given tenant first
        val tenantIdsOfUser = userRepository.resolveTenantIdsOfUser(identity.authenticatedUser.id)
        if (!tenantIdsOfUser.contains(desiredTenantId)) {
            return TenantSwitchResult.NOT_ALLOWED
        }

        val currentAuthentication = SecurityContextHolder.getContext().authentication
        when (currentAuthentication) {
            is OAuth2AuthenticationToken -> reauthenticateOauth2Authentication(
                currentAuthentication,
                desiredTenantId
            )

            is UsernamePasswordAuthenticationToken -> reauthenticateUsernamePasswordAuthentication(
                currentAuthentication,
                desiredTenantId
            )

            else -> throw UnknownAuthenticationTokenException()
        }

        return TenantSwitchResult.TENANT_CHANGED
    }

    private fun reauthenticateUsernamePasswordAuthentication(
        currentAuthentication: UsernamePasswordAuthenticationToken,
        desiredTenantId: UUID
    ) {
        val newAuthentication = databaseBasedAuthenticationProvider.buildUsernamePasswordAuthentication(
            currentAuthentication.name,
            currentAuthentication.credentials,
            desiredTenantId
        )
        // Replace the authentication in the auth context
        SecurityContextHolder.getContext().authentication = newAuthentication
    }

    private fun reauthenticateOauth2Authentication(
        currentAuthentication: OAuth2AuthenticationToken,
        desiredTenantId: UUID
    ) {
        val newAuthentication = oAuth2AuthenticationProvider.buildOAuth2TenantAuthentication(
            currentAuthentication,
            desiredTenantId
        )
        // Replace the authentication in the auth context
        SecurityContextHolder.getContext().authentication = newAuthentication
    }

}


enum class TenantSwitchResult {
    TENANT_CHANGED,
    NOT_ALLOWED
}
