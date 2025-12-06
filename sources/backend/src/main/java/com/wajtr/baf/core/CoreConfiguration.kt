package com.wajtr.baf.core

import com.wajtr.baf.core.datasource.ContextAwareTransactionManager
import com.wajtr.baf.core.shared.CoreContext.currentTenantId
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.SchedulingConfiguration
import java.util.function.Supplier

/**
 * @author Bretislav Wajtr
 */
@Configuration
@ComponentScan
@Import(
    value = [DataSourceConfiguration::class, CacheConfiguration::class, I18nConfiguration::class, SpringMvcConfiguration::class, SchedulingConfiguration::class]
)
class CoreConfiguration(private val transactionManager: ContextAwareTransactionManager) {

    @EventListener
    @Suppress("unused")
    fun afterApplicationStarted(event: ApplicationStartedEvent?) {
        transactionManager.setTransactionContextProperty("session.tenant.id", Supplier {
            return@Supplier currentTenantId?.toString()
        })
    }
}
