package com.wajtr.baf.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Global exception handler for all REST API controllers.
 *
 * Ensures that unhandled exceptions are returned as JSON responses instead of
 * the default HTML error pages produced by the servlet container.
 *
 * @author Bretislav Wajtr
 */
@RestControllerAdvice(basePackages = ["com.wajtr.baf.api"])
class ApiExceptionHandler {

    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ApiError> {
        logger.error("Unhandled API exception", exception)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError(status = 500, error = "Internal Server Error"))
    }
}

data class ApiError(
    val status: Int,
    val error: String
)
