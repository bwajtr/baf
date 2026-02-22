package com.wajtr.baf.api.test

import com.wajtr.baf.test.BaseContainerIntegrationTest
import com.wajtr.baf.test.BackendTestApplication
import com.wajtr.baf.test.DatabaseTestHelper
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.servlet.client.RestTestClient

/**
 * Base class for API integration tests.
 *
 * Extends [BaseContainerIntegrationTest] which provides the shared PostgreSQL container,
 * context initializer, and timezone/locale defaults.
 *
 * This class adds:
 * - A real Spring Boot application on a random port ([RANDOM_PORT])
 * - [RestTestClient] bound to the running server for making HTTP requests
 * - Database truncation and reload before each test (via [DatabaseTestHelper])
 * - API key authentication helpers ([withApiKey])
 *
 * Authentication is done exclusively via API key (`X-API-Key` header). Use [withApiKey]
 * to get a [RestTestClient] pre-configured with that header. Use the injected [restTestClient]
 * directly for unauthenticated requests.
 * Test API key constants are available as [API_KEY_TENANT_1] and [API_KEY_TENANT_2].
 *
 * Usage:
 * ```kotlin
 * class MyEndpointIT : BaseApiIntegrationTest() {
 *
 *     @Test
 *     fun `returns 200 with data for tenant 1`() {
 *         withApiKey(API_KEY_TENANT_1)
 *             .get()
 *             .uri("/api/v1/something")
 *             .exchange()
 *             .expectStatus().isOk
 *     }
 * }
 * ```
 *
 * @author Bretislav Wajtr
 */
@SpringBootTest(classes = [BackendTestApplication::class], webEnvironment = RANDOM_PORT)
abstract class BaseApiIntegrationTest : BaseContainerIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    protected lateinit var databaseTestHelper: DatabaseTestHelper

    protected lateinit var restTestClient: RestTestClient

    @BeforeEach
    fun setUp() {
        restTestClient = RestTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()

        databaseTestHelper.truncateAllTables()
        databaseTestHelper.loadBasicTestData()
    }

    /**
     * Returns a [RestTestClient] with the `X-API-Key` header pre-set for every request.
     */
    protected fun withApiKey(apiKey: String): RestTestClient =
        restTestClient.mutate().defaultHeader("X-API-Key", apiKey).build()

    companion object {
        /** API key for Tenant 1 (defined in integration-test-basic-database-content.sql) */
        const val API_KEY_TENANT_1 = "TEST_API_KEY_TENANT_1_ABCDEFGHIJKLMNOPRSTUVWXYZ12345"

        /** API key for Tenant 2 (defined in integration-test-basic-database-content.sql) */
        const val API_KEY_TENANT_2 = "TEST_API_KEY_TENANT_2_ABCDEFGHIJKLMNOPRSTUVWXYZ12345"
    }
}
