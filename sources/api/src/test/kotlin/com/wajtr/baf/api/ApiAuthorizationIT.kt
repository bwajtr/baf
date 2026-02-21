package com.wajtr.baf.api

import com.wajtr.baf.api.test.BaseApiIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

/**
 * Integration tests for API key authorization.
 *
 * Verifies that unauthenticated requests to API endpoints are rejected with a proper
 * JSON error response (not an HTML page) and the correct HTTP 401 status.
 *
 * @author Bretislav Wajtr
 */
class ApiAuthorizationIT : BaseApiIntegrationTest() {

    // Any authenticated endpoint can be used here; /api/v1/myproducts is the simplest available.
    private val anyProtectedEndpoint = "/api/v1/myproducts"

    @Test
    fun `returns 401 when no API key header is provided`() {
        restTestClient.get()
            .uri(anyProtectedEndpoint)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 401 when API key header is empty`() {
        withApiKey("").get()
            .uri(anyProtectedEndpoint)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 401 when API key is invalid`() {
        withApiKey("invalid-key").get()
            .uri(anyProtectedEndpoint)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `returns JSON error body with status 401 and Unauthorized message`() {
        restTestClient.get()
            .uri(anyProtectedEndpoint)
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.status").isEqualTo(401)
            .jsonPath("$.error").isEqualTo("Unauthorized")
    }
}
