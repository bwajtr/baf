package com.wajtr.baf.api.product

import com.wajtr.baf.test.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

/**
 * Integration tests for [ProductsApiController] invoked directly as a Spring bean.
 *
 * Verifies tenant isolation and response content by calling the controller method
 * directly (no HTTP layer). Runs as a transactional test with automatic rollback.
 *
 * Default authentication: Josh Owner (Tenant 1) — provided by [BaseIntegrationTest].
 *
 * @author Bretislav Wajtr
 */
class ProductsControllerIT : BaseIntegrationTest() {

    @Autowired
    private lateinit var productsApiController: ProductsApiController

    @Test
    fun `returns only products belonging to the authenticated tenant`() {
        val products = productsApiController.getMyProducts()

        assertThat(products)
            .extracting("name")
            .containsExactlyInAnyOrder("Product 1", "Product 2")
    }

    @Test
    fun `each product has the expected fields`() {
        val products = productsApiController.getMyProducts()
        val product1 = products.first { it.name == "Product 1" }

        assertThat(product1.price).isEqualByComparingTo(BigDecimal("12.45"))
    }
}
