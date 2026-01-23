package com.wajtr.baf.core

import com.wajtr.baf.core.datasource.ContextAwareTransactionManager
import com.wajtr.baf.user.Identity
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
    @Suppress("unused")
    fun afterApplicationStarted(event: ApplicationStartedEvent?) {
        transactionManager.setTransactionContextProperty("session.tenant.id") {
            identity.authenticatedTenant?.id.toString()
        }
    }
}
