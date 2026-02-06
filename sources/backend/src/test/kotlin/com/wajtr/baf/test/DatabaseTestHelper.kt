package com.wajtr.baf.test

import jakarta.annotation.PostConstruct
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.sql.DataSource
import kotlin.io.bufferedReader
import kotlin.io.readText
import kotlin.use

/**
 * Helper class for database operations in integration tests.
 * 
 * Provides utilities for:
 * - Loading test data from SQL scripts
 * - Executing arbitrary SQL scripts
 * - Cleaning up test data (though @Transactional rollback handles most cleanup)
 */
@Component
class DatabaseTestHelper(
    private val dslContext: DSLContext,
    @param:Qualifier("migrationsDataSource") // this datasource uses "dbadmin" user and has therefore higher privileges to perform operations on DB than the usual "dbuser" role
    private val migrationsDataSource: DataSource
) {

    @PostConstruct
    private fun initAfterSpringInitialized() {
        // we typically want to load the initial data only once after the test suite is spawn
        loadBasicTestData()
    }

    /**
     * Executes an SQL script from classpath.
     * Typically used to load test data.
     * 
     * @param scriptPath Path to SQL script in classpath (e.g., "integration-test-basic-database-content.sql")
     */
    @Suppress("SqlSourceToSinkFlow")
    fun executeSqlScript(scriptPath: String) {
        val sql = ClassPathResource(scriptPath).inputStream.bufferedReader().use { it.readText() }
        migrationsDataSource.connection.use { connection ->
            DSL.using(connection, SQLDialect.POSTGRES).execute(sql)
        }
    }

    /**
     * Loads the basic integration test data.
     * This includes test users (Joe User, Jane Admin, Josh Owner, William Owner), 
     * tenants (Tenant 1, Tenant 2), and sample data (products, invitations, login logs).
     * 
     * This should be called in @BeforeEach of the integration tests to ensure a consistent
     * starting state. The @Transactional annotation on tests will automatically roll back
     * all changes after each test completes.
     */
    fun loadBasicTestData() {
        executeSqlScript("integration-test-basic-database-content.sql")
    }

    /**
     * Truncates all tenant-specific tables.
     * Useful for cleaning up between tests if @Transactional rollback is not used.
     * 
     * Note: In most cases, you should rely on @Transactional rollback instead of
     * explicitly truncating tables, as it's faster and safer.
     */
    @Transactional
    fun truncateAllTables() {
        dslContext.execute(
            "TRUNCATE TABLE tenant_member_invitation, product, user_login_history, " +
                    "tenant_member, user_account, tenant CASCADE"
        )
    }
}
