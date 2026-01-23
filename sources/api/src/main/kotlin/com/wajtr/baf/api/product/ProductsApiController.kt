package com.wajtr.baf.api.product

import com.wajtr.baf.product.Product
import com.wajtr.baf.product.ProductRepository
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


