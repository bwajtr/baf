package com.wajtr.baf.api.test

import com.wajtr.baf.test.DatabaseTestHelper
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.client.RestTestClient
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.*

/**
 * Base class for API integration tests.
 *
 * Starts a real Spring Boot application on a random port with a PostgreSQL Testcontainers
 * database. [RestTestClient] is used to make HTTP requests against the running server.
 *
 * Database state is reset before each test by truncating all tables and reloading the
 * standard test data (via [DatabaseTestHelper]). This ensures every test starts from a
 * known, consistent database state — which is essential when tests modify data.
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
@SpringBootTest(classes = [BackendApiTestApplication::class], webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = [BaseApiIntegrationTest.Initializer::class])
abstract class BaseApiIntegrationTest {

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

        init {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            System.setProperty("user.timezone", "UTC")
            Locale.setDefault(Locale.US)
        }

        /**
         * Shared PostgreSQL container for all API integration tests.
         * Started once before the first test and reused for the entire test run.
         */
        val postgresContainer = PostgreSQLContainer("postgres:18")
            .withDatabaseName("testdb")
            .withInitScript("testcontainers-init.sql")
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(context: ConfigurableApplicationContext) {
            postgresContainer.start()
            TestPropertyValues.of(
                "spring.datasource.jdbcUrl=${postgresContainer.jdbcUrl}",
                "spring.migrations.datasource.jdbcUrl=${postgresContainer.jdbcUrl}"
            ).applyTo(context.environment)
        }
    }
}
