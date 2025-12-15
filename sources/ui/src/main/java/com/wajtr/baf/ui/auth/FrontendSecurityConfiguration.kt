package com.wajtr.baf.ui.auth

import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint

@Configuration
@EnableWebSecurity
class FrontendSecurityConfiguration {

    @Bean
    fun vaadinSecurityFilterChain(
        http: HttpSecurity,
        clientRegistrationRepository: ClientRegistrationRepository,
        authSuccessHandler: FrontendOAuth2AuthenticationSuccessHandler
    ): SecurityFilterChain {
        val loginRoute = "/oauth2/authorization/keycloak"
        val logoutSuccessHandler = OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository)
        logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}")

        http.setSharedObject(VaadinSavedRequestAwareAuthenticationSuccessHandler::class.java, authSuccessHandler)
        http.with(vaadin()) { vaadin: VaadinSecurityConfigurer ->
            vaadin
                .oauth2LoginPage(loginRoute)
                .logoutSuccessHandler(logoutSuccessHandler)
                .enableExceptionHandlingConfiguration(false)
        }

        // Configure OAuth2 login with custom user service and success handler
        http.oauth2Login { oauth2 ->
            oauth2
                .successHandler(authSuccessHandler)
        }

        http.exceptionHandling { exceptionHandling: ExceptionHandlingConfigurer<HttpSecurity> ->
            val entryPoint = LoginUrlAuthenticationEntryPoint(loginRoute)
            exceptionHandling.authenticationEntryPoint(entryPoint)
        }

        return http.build()
    }
}
