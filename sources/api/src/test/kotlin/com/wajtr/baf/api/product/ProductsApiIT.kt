package com.wajtr.baf.api.product

import com.wajtr.baf.api.product.ProductsApiController.Companion.PRODUCTS_ENDPOINT
import com.wajtr.baf.api.test.BaseApiIntegrationTest
import org.junit.jupiter.api.Test

/**
 * Integration tests for GET /api/v1/myproducts.
 *
 * Verifies that products are correctly returned per tenant via HTTP requests
 * against the running server.
 *
 * @author Bretislav Wajtr
 */
class ProductsApiIT : BaseApiIntegrationTest() {

    @Test
    fun `returns 200 with products for tenant 1`() {
        withApiKey(API_KEY_TENANT_1).get()
            .uri(PRODUCTS_ENDPOINT)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `returns 200 with products for tenant 2`() {
        withApiKey(API_KEY_TENANT_2).get()
            .uri(PRODUCTS_ENDPOINT)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(2)
    }
}
