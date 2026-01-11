package com.wajtr.baf.test

import com.wajtr.baf.core.CoreConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * Test-only Spring Boot application for backend integration tests.
 * This is needed because the main ApplicationMain class is in the UI module.
 * 
 * This application scans the backend packages and imports the CoreConfiguration
 * to set up the necessary beans for testing (datasource, jOOQ, security, etc.).
 * 
 * OAuth2 is disabled in tests (excluded via @SpringBootApplication annotation) - we use
 * only a direct authentication in tests.
 */
@SpringBootApplication(
    scanBasePackages = ["com.wajtr.baf"],
    exclude = [OAuth2ClientAutoConfiguration::class]  // disable OAuth2 auto-configuration otherwise Spring Security fails to start for backend-only project
)
@Import(CoreConfiguration::class)
class BackendTestApplication

