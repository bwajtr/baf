package com.wajtr.baf.api.product

import com.wajtr.baf.api.API_V1_PREFIX
import com.wajtr.baf.product.Product
import com.wajtr.baf.product.ProductRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Bretislav Wajtr
 */
@RestController
@RequestMapping(API_V1_PREFIX)
class ProductsApiController(private val productRepository: ProductRepository) {

    companion object {
        const val PRODUCTS_ENDPOINT = "$API_V1_PREFIX/myproducts"
    }

    @GetMapping("/myproducts")
    fun getMyProducts(): List<Product> {
        return productRepository.findAll()
    }
}


