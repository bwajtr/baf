package com.wajtr.baf.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.transaction.BeforeTransaction
import org.springframework.transaction.annotation.Transactional

/**
 * Base class for "normal" (non-HTTP) integration tests using Spring Boot and Testcontainers.
 *
 * Extends [BaseContainerIntegrationTest] which provides the shared PostgreSQL container,
 * context initializer, and timezone/locale defaults.
 *
 * This class adds:
 * - Transactional rollback after each test (ensures test isolation without truncation)
 * - Authentication test helpers ([AuthenticationTestHelper]) — defaults to Owner (Josh, Tenant 1)
 * - Database test helpers ([DatabaseTestHelper])
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
 *
 * @author Bretislav Wajtr
 */
@SpringBootTest(classes = [BackendTestApplication::class])
@Rollback
@Transactional
abstract class BaseIntegrationTest : BaseContainerIntegrationTest() {

    @Autowired
    protected lateinit var databaseTestHelper: DatabaseTestHelper

    @Autowired
    protected lateinit var authenticationTestHelper: AuthenticationTestHelper

    @BeforeTransaction
    fun setupTransactionContext() {
        // by default, login as Owner, but you can override this in the test method
        authenticationTestHelper.loginAsOwner()
    }
}
