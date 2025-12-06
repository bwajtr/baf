package com.wajtr.baf.core

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.util.*

/**
 * @author Bretislav Wajtr
 */
@Configuration
class I18nConfiguration {
    @Bean
    fun localeResolver(): LocaleResolver {
        val sessionLocaleResolver = SessionLocaleResolver()
        sessionLocaleResolver.setDefaultTimeZone(TimeZone.getDefault()) // default is usually set in ApplicationMain.kt, just before Spring starts
        return sessionLocaleResolver
    }
}
