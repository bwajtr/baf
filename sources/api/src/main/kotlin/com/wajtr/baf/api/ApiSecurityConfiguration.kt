package com.wajtr.baf.api

import com.wajtr.baf.user.emailverification.CONFIRM_EMAIL_OWNERSHIP_URL
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.SecurityFilterChain

/**
 * Configuration of security change for the API access (all paths matching /api/ **)
 * 
 * @author Bretislav Wajtr
 */
@Configuration
@EnableWebSecurity
class ApiSecurityConfiguration {

    @Bean
    @Order(1)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Enable CORS...
            .cors(withDefaults())
            // ...and disable CSRF and other things
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .requestCache { it.disable() }
            // Set session management to stateless
            .sessionManagement {
                it.sessionCreationPolicy(STATELESS)
            }
            // Set unauthorized requests exception handler
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, exception ->
                    response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        exception.message
                    )
                }
            }
            .securityMatcher(
                "/api/**", CONFIRM_EMAIL_OWNERSHIP_URL
            ).authorizeHttpRequests { auth ->
                // keep email ownership api public
                auth.requestMatchers(CONFIRM_EMAIL_OWNERSHIP_URL).permitAll()
                // but rest of the /api/ should be authenticated
                auth.requestMatchers("/api/**").permitAll()
            }

        return http.build()
    }

}
