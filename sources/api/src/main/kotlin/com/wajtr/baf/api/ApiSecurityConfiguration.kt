package com.wajtr.baf.api

import com.wajtr.baf.api.security.ApiKeyAuthenticationFilter
import com.wajtr.baf.user.emailverification.CONFIRM_EMAIL_OWNERSHIP_URL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Configuration of security chain for the API access (all paths matching /api/ endpoints)
 *
 * API endpoints require authentication via an API key provided in the `X-API-Key` header.
 * The email verification endpoint (/auth/confirm) is excluded and remains public.
 *
 * @author Bretislav Wajtr
 */
@Configuration
@EnableWebSecurity
class ApiSecurityConfiguration(
    private val apiKeyAuthenticationFilter: ApiKeyAuthenticationFilter,
    private val apiAuthenticationEntryPoint: ApiAuthenticationEntryPoint
) {

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
                it.authenticationEntryPoint(apiAuthenticationEntryPoint)
            }
            // Add API key authentication filter
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .securityMatcher(
                "/api/**", CONFIRM_EMAIL_OWNERSHIP_URL
            ).authorizeHttpRequests { auth ->
                // keep email ownership endpoint public
                auth.requestMatchers(CONFIRM_EMAIL_OWNERSHIP_URL).permitAll()
                // all other API endpoints require authentication (via API key)
                auth.requestMatchers("/api/**").authenticated()
            }

        return http.build()
    }

}
