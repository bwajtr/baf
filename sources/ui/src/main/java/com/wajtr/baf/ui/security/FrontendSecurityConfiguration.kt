package com.wajtr.baf.ui.security

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer
import com.wajtr.baf.authentication.db.LOGIN_PATH
import com.wajtr.baf.ui.views.user.login.LoginPage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain

@Configuration
@Order(2)
@EnableWebSecurity
class FrontendSecurityConfiguration(
    private val clientRegistrationRepository: ClientRegistrationRepository?,
    private val frontendOAuth2AuthenticationSuccessHandler: FrontendOAuth2AuthenticationSuccessHandler
) {

    @Bean
    fun uiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                // App specific static resources icons and images
                auth.requestMatchers("/static/**").permitAll()
                auth.requestMatchers("/images/**").permitAll()

                // Allow OAuth2 endpoints
                auth.requestMatchers("/oauth2/**").permitAll()
                auth.requestMatchers("/login/oauth2/**").permitAll()
            }

        http.with(VaadinSecurityConfigurer.vaadin()) { configurer ->
            configurer
                .loginView(LoginPage::class.java)
        }

        http.formLogin { customizer ->
            customizer
                .usernameParameter("email")
        }

        if (hasOAuth2Clients()) {
            // Add OIDC login support (does NOT replace form login, you have to redirect manually to one of OIDC login urls to initiate the flow)
            // See implementation in the LoginPage class
            http.oauth2Login { oauth2 ->
                oauth2
                    .loginPage("/$LOGIN_PATH") // redirect to normal login page if not authenticated
                    .successHandler(frontendOAuth2AuthenticationSuccessHandler)
            }
        }

        return http.build()
    }

    private fun hasOAuth2Clients(): Boolean {
        // will not be null if there are some spring.security.oauth2.client.* properties defined in
        // application properties. We have an example configuration defined in application.properties
        return clientRegistrationRepository != null
    }
}
