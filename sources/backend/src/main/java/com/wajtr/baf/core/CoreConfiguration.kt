package com.wajtr.baf.core

import com.wajtr.baf.core.auth.Identity
import com.wajtr.baf.core.datasource.ContextAwareTransactionManager
import org.springframework.boot.context.event.ApplicationStartedEvent
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
    value = [DataSourceConfiguration::class, CacheConfiguration::class, I18nConfiguration::class, SpringMvcConfiguration::class, SchedulingConfiguration::class]
)
class CoreConfiguration(
    private val transactionManager: ContextAwareTransactionManager,
    private val identity: Identity
) {

    @EventListener
    @Suppress("unused")
    fun afterApplicationStarted(event: ApplicationStartedEvent?) {
        transactionManager.setTransactionContextProperty("session.tenant.id") {
            identity.authenticatedTenant.id.toString()
        }
    }
}
