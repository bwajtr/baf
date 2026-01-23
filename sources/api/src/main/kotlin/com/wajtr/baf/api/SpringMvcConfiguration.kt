package com.wajtr.baf.api

import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.charset.StandardCharsets

/**
 * The configuration and customizations of Spring MVC framework. Spring MVC is mainly used for the API implementation through
 * @RestController endpoints.
 *
 * @author Bretislav Wajtr
 */
@Configuration
class SpringMvcConfiguration : WebMvcConfigurer {

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        // This is required so the response always explicitely states the UTF-8 charset. without it, the Android clients
        // incorrectly parse the response and use incorrect charset
        configurer.defaultContentType(
            MediaType("application", "json", StandardCharsets.UTF_8),
            MediaType.ALL
        )
    }
}
