package com.wajtr.baf.core

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
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
class CoreConfiguration
