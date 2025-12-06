package com.wajtr.baf.features.product

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Bretislav Wajtr
 */
@RestController
class ProductsApiController(private val productRepository: ProductRepository) {

    @GetMapping("/api/myproducts")
    fun getMyProducts(): List<Product> {
        return productRepository.findAll()
    }
}


