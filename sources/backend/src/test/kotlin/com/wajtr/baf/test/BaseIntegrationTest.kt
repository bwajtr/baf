package com.wajtr.baf.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.BeforeTransaction
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.*


/**
 * Base class for integration tests using Spring Boot and Testcontainers.
 * 
 * Features:
 * - PostgreSQL database via Testcontainers (shared container for all tests)
 * - Automatic Flyway migrations on startup
 * - Transactional rollback after each test (ensures test isolation)
 * - Authentication test helpers (loginAsUser, loginAsAdmin, loginAsOwner, etc.)
 * - Database test helpers (loadBasicTestData, executeSqlScript, etc.)
 * - OAuth2 auto-configuration excluded (we use only direct authentication via Spring Security context)
 * 
 * The shared PostgreSQL container starts once for all tests and is reused,
 * making tests faster. Each test runs in a transaction that is rolled back
 * after the test completes, ensuring no data leaks between tests.
 * 
 * Usage:
 * ```kotlin
 * class MyServiceIT : BaseIntegrationTest() {
 * 
 *     @Autowired
 *     private lateinit var myService: MyService
 *
 *     @Test
 *     fun `test something as owner`() {
 *         val result = myService.doSomething()
 *         assertThat(result).isEqualTo(...)
 *     }
 *
 *     @Test
 *     fun `test something as admin`() {
 *          authenticationTestHelper.loginAsAdmin()
 *          val result = myService.doSomething()
 *          assertThat(result).isEqualTo(...)
 *     }
 * }
 * ```
 */
@SpringBootTest(classes = [BackendTestApplication::class])
@ActiveProfiles("test")
@ContextConfiguration(initializers = [BaseIntegrationTest.Initializer::class])
@Rollback
@Transactional
abstract class BaseIntegrationTest {

    @Autowired
    protected lateinit var databaseTestHelper: DatabaseTestHelper

    @Autowired
    protected lateinit var authenticationTestHelper: AuthenticationTestHelper

    @BeforeTransaction
    fun setupTransactionContext() {
        // by default, login as Owner, but you can override this in the test method
        authenticationTestHelper.loginAsOwner()
    }

    companion object {

        init {
            // Set timezone to UTC regardless of where this server runs. Note that developer should perform all
            // datetime related operations in UTC (database and additional services should run in UTC as well and data should
            // be stored in UTC timezone) and convert to zoned datetime only when presenting the data to the user in UI, base on
            // what TimeZone the current user is. Do not forget that users from multiple timezones can access one server at the same time.
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            System.setProperty("user.timezone", "UTC")
            println("Spring boot application running in UTC timezone: " + Date())
            Locale.setDefault(Locale.US)
        }

        /**
         * Shared PostgreSQL container for all integration tests.
         * This container is started once before all tests and reused,
         * significantly improving test performance.
         * 
         * The container:
         * - Uses PostgreSQL
         * - Runs initialization script (testcontainers-init.sql) to create users and extensions
         * - Is automatically stopped after all tests complete
         */
        val postgresContainer = PostgreSQLContainer("postgres:18")
            .withDatabaseName("testdb")
            .withInitScript("testcontainers-init.sql")
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            postgresContainer.start()

            TestPropertyValues.of(
                "spring.datasource.jdbcUrl=${postgresContainer.jdbcUrl}",
                "spring.migrations.datasource.jdbcUrl=${postgresContainer.jdbcUrl}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}
