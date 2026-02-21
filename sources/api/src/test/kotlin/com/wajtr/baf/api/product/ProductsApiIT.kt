package com.wajtr.baf.api.product

import com.wajtr.baf.api.test.BaseApiIntegrationTest
import org.junit.jupiter.api.Test

/**
 * Integration tests for GET /api/v1/myproducts.
 *
 * Verifies that products are correctly returned per tenant and that tenant isolation
 * is enforced (each tenant sees only their own products).
 *
 * @author Bretislav Wajtr
 */
class ProductsApiIT : BaseApiIntegrationTest() {

    @Test
    fun `returns 200 with products for tenant 1`() {
        withApiKey(API_KEY_TENANT_1).get()
            .uri("/api/v1/myproducts")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `returns 200 with products for tenant 2`() {
        withApiKey(API_KEY_TENANT_2).get()
            .uri("/api/v1/myproducts")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `returns only products belonging to the authenticated tenant`() {
        withApiKey(API_KEY_TENANT_1).get()
            .uri("/api/v1/myproducts")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[*].name").value<List<String>> { names ->
                assert(names.containsAll(listOf("Product 1", "Product 2")))
                assert(!names.contains("Product 3"))
                assert(!names.contains("Product 4"))
            }
    }

    @Test
    fun `each product has the expected fields`() {
        withApiKey(API_KEY_TENANT_1).get()
            .uri("/api/v1/myproducts")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[?(@.name == 'Product 1')].price").isEqualTo(12.45)
    }
}
