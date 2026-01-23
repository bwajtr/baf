package com.wajtr.baf.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Configuration of security change for the API access (all paths matching /api/ **)
 * 
 * @author Bretislav Wajtr
 */
@Configuration
class ApiSecurityConfiguration {

    @Bean
    @Order(1)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.securityMatcher(
            "/api/**",
        ).authorizeHttpRequests { auth ->
            // Rest api
            auth.requestMatchers("/api/**").permitAll()
        }
        http.csrf {
            it.ignoringRequestMatchers("/api/**")
        }
        return http.build()
    }

}
