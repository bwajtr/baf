package com.wajtr.baf.api

/**
 * Constants for API endpoints.
 *
 * @author Bretislav Wajtr
 */

/**
 * The base path prefix for version 1 of the REST API.
 * All REST API controllers should use this prefix via @RequestMapping annotation.
 *
 * Example:
 * ```
 * @RestController
 * @RequestMapping(API_V1_PREFIX)
 * class MyController {
 *     @GetMapping("/myendpoint")  // Results in: GET /api/v1/myendpoint
 *     fun myEndpoint() { ... }
 * }
 * ```
 */
const val API_V1_PREFIX = "/api/v1"
