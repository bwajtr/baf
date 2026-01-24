package com.wajtr.baf.core

import com.wajtr.baf.core.datasource.ContextAwareTransactionManager
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.NoAuthenticatedUserException
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.SchedulingConfiguration

/**
 * @author Bretislav Wajtr
 */
@Configuration
@ComponentScan
@Import(
    value = [DataSourceConfiguration::class, CacheConfiguration::class, I18nConfiguration::class, SchedulingConfiguration::class]
)
@EnableConfigurationProperties(ApplicationProperties::class)
class CoreConfiguration(
    private val transactionManager: ContextAwareTransactionManager,
    private val identity: Identity
) {

    @EventListener
    @Suppress("unused", "IfThenToSafeAccess")
    fun afterApplicationStarted(event: ApplicationStartedEvent?) {
        transactionManager.setTransactionContextProperty("session.tenant.id") {
            val authenticatedTenant = identity.authenticatedTenant

            if (authenticatedTenant != null) {
                authenticatedTenant.id.toString()
            } else {
                // this might happen in cases when there is no authenticated user. Note that only very limited
                // set of DB operations are permitted without tenant set (like registering new user, authenticating user etc).
                null
            }
        }

        transactionManager.setTransactionContextProperty("session.user.id", {
            try {
                val currentUser = identity.authenticatedUser
                currentUser.id.toString()
            } catch (e: NoAuthenticatedUserException) {
                // this might happen in cases when there is no authenticated user. Note that only very limited
                // set of DB operations are permitted without tenant set (like registering new user, authenticating user etc).
                null
            }
        })
    }
}
