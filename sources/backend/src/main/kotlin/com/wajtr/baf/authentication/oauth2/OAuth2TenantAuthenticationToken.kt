package com.wajtr.baf.authentication.oauth2

import com.wajtr.baf.authentication.AuthenticatedTenant
import com.wajtr.baf.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User

class OAuth2TenantAuthenticationToken(
    principal: OAuth2User,
    authorities: Collection<GrantedAuthority>?,
    authorizedClientRegistrationId: String,
    val user: User,
    val tenant: AuthenticatedTenant
) : OAuth2AuthenticationToken(principal, authorities, authorizedClientRegistrationId)
