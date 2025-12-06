package com.wajtr.baf

import com.vaadin.flow.spring.annotation.EnableVaadin
import com.wajtr.baf.core.CoreConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration
import org.springframework.context.annotation.Import
import java.util.*

@SpringBootApplication(
    exclude = [
        // we are not using direct JdbcTemplate operations at all
        JdbcTemplateAutoConfiguration::class,

        // this prevents automatically redirecting to "/error" when exception is thrown. This autoconfiguration
        // is causing issues when calling REST API ("/api/**" endpoints) with invalid token -
        // instead of returning 401, it redirects to "/error" which in turn redirects to "/accounts/login" which is not what we want.
        ErrorMvcAutoConfiguration::class
    ]
)
@Import(
    CoreConfiguration::class,
)
@EnableVaadin("com.wajtr")
class ApplicationMain

fun main(args: Array<String>) {
    // Set timezone to UTC regardless of where this server runs. Note that developer should perform all
    // datetime related operations in UTC (database and additional services should run in UTC as well and data should
    // be stored in UTC timezone) and convert to zoned datetime only when presenting the data to the user in UI, base on
    // what TimeZone the current user is. Do not forget that users from multiple timezones can access one server at the same time.
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    System.setProperty("user.timezone", "UTC")

    // Run spring boot
    SpringApplication.run(ApplicationMain::class.java, *args)
}
