package com.wajtr.baf.core.commons

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.info.GitProperties
import org.springframework.stereotype.Component
import java.util.*

/**
 * This implementation of CommandLineRunner will be executed once during startup of the application. Printing various information about the
 * running app.
 *
 * @author Bretislav Wajtr
 */
@Component
class PrintBuildInfoOnStartup(
    private val buildProperties: BuildProperties?,
    private val gitProperties: GitProperties?
) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(PrintBuildInfoOnStartup::class.java)

    override fun run(vararg args: String) {
        log.info("  → Spring boot application running in timezone: " + Date())

        if (buildProperties != null) {
            log.info("  → Running build ${buildProperties.version} built on ${buildProperties.time}")
        } else {
            log.warn("  → Build properties (META-INF/build-info.properties) were not found, build info cannot be provided")
        }

        if (gitProperties != null) {
            log.info("  → Build created out of commit ${gitProperties.commitId} from ${gitProperties.commitTime}")
        } else {
            log.warn("  → Git properties (/git.properties) were not found, git info cannot be provided")
        }
    }

}
