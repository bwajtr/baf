package com.wajtr.baf.api.test

import com.wajtr.baf.core.CoreConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * Test-only Spring Boot application for API integration tests.
 *
 * Scans all of `com.wajtr.baf` to load both the backend beans (datasource, jOOQ, services,
 * repositories) and the API layer (controllers, security filter chain, exception handlers).
 *
 * OAuth2 is disabled — API tests authenticate exclusively via API key (`X-API-Key` header).
 *
 * @author Bretislav Wajtr
 */
@SpringBootApplication(
    scanBasePackages = ["com.wajtr.baf"],
    exclude = [OAuth2ClientAutoConfiguration::class]
)
@Import(CoreConfiguration::class)
class BackendApiTestApplication
