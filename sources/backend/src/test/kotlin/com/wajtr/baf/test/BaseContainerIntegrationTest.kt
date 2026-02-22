package com.wajtr.baf.test

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.*

/**
 * Common base class for all integration tests (both backend and API).
 *
 * Provides a shared PostgreSQL Testcontainers container that is started once and reused
 * across all tests, along with the [Initializer] that wires the container's JDBC URL
 * into the Spring `Environment`.
 *
 * Subclasses must add their own `@SpringBootTest` annotation to select the test application
 * class and web environment. This class intentionally does **not** declare `@SpringBootTest`,
 * `@Transactional`, or `@Rollback` — those are concerns of the concrete base classes
 * ([BaseIntegrationTest] for transactional rollback tests, `BaseApiIntegrationTest` for
 * HTTP-level API tests).
 *
 * @author Bretislav Wajtr
 */
@ActiveProfiles("test")
@ContextConfiguration(initializers = [BaseContainerIntegrationTest.Initializer::class])
abstract class BaseContainerIntegrationTest {

    companion object {

        init {
            // Set timezone to UTC regardless of where this server runs. Note that developer should perform all
            // datetime related operations in UTC (database and additional services should run in UTC as well and data should
            // be stored in UTC timezone) and convert to zoned datetime only when presenting the data to the user in UI, based on
            // what TimeZone the current user is. Do not forget that users from multiple timezones can access one server at the same time.
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            System.setProperty("user.timezone", "UTC")
            Locale.setDefault(Locale.US)
        }

        /**
         * Shared PostgreSQL container for all integration tests.
         * This container is started once before all tests and reused,
         * significantly improving test performance.
         *
         * The container:
         * - Uses PostgreSQL 18
         * - Runs initialization script (testcontainers-init.sql) to create users and extensions
         * - Is automatically stopped after all tests complete
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
