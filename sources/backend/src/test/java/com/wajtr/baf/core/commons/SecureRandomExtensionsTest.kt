package com.wajtr.baf.core.commons

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.security.SecureRandom

/**
 * Unit test for SecureRandomExtensions.
 * This is a pure JUnit test without Spring context, demonstrating how to write
 * fast unit tests for utility functions.
 * 
 * Unit tests should:
 * - Not use @SpringBootTest (no Spring context overhead)
 * - Have suffix *Test.kt (e.g., SecureRandomExtensionsTest.kt)
 * - Test isolated functions/classes without external dependencies
 * - Run quickly (typically < 100ms per test)
 * - Use AssertJ assertions (assertThat) for fluent, readable assertions
 */
class SecureRandomExtensionsTest {

    private val secureRandom = SecureRandom()

    @Test
    fun `generateRandomAlphanumeric should return string of correct length`() {
        val result = secureRandom.generateRandomAlphanumeric(10)
        
        assertThat(result)
            .describedAs("Generated string should have exactly 10 characters")
            .hasSize(10)
    }

    @Test
    fun `generateRandomAlphanumeric should return only alphanumeric characters`() {
        val result = secureRandom.generateRandomAlphanumeric(100)
        
        assertThat(result.all { it.isLetterOrDigit() })
            .describedAs("Generated string should contain only letters and digits")
            .isTrue()
    }

    @Test
    fun `generateRandomAlphanumeric should return different values on subsequent calls`() {
        val result1 = secureRandom.generateRandomAlphanumeric(20)
        val result2 = secureRandom.generateRandomAlphanumeric(20)
        
        assertThat(result1)
            .describedAs("Two consecutive calls should produce different random strings")
            .isNotEqualTo(result2)
    }

    @Test
    fun `generateRandomAlphanumeric should handle zero length`() {
        val result = secureRandom.generateRandomAlphanumeric(0)
        
        assertThat(result)
            .describedAs("Zero length should produce empty string")
            .isEmpty()
    }

    @Test
    fun `generateRandomAlphanumeric should handle length of 1`() {
        val result = secureRandom.generateRandomAlphanumeric(1)
        
        assertThat(result)
            .describedAs("Length of 1 should produce single alphanumeric character")
            .hasSize(1)
            .matches { it[0].isLetterOrDigit() }
    }

    @Test
    fun `generateRandomAlphanumeric should handle large length`() {
        val length = 1000L
        val result = secureRandom.generateRandomAlphanumeric(length)
        
        assertThat(result)
            .describedAs("Should handle large lengths with all alphanumeric characters")
            .hasSize(length.toInt())
            .matches { it.all { char -> char.isLetterOrDigit() } }
    }
}
