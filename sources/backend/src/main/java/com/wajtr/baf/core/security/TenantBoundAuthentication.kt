package com.wajtr.baf.core.security

import java.util.*

/**
 * @author Bretislav Wajtr
 */
interface TenantBoundAuthentication {
    val tenantId: UUID

    val usedAuthenticationMethod: AuthenticationMethod
}
