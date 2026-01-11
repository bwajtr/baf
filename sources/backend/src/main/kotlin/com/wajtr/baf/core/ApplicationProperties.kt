package com.wajtr.baf.core

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "application")
class ApplicationProperties {

    /**
     * Email address of the sender (aka "From" in the email) which will be used when sending emails related to the technical operations related to the application
     * (for example account verification emails, password reset emails etc.). It'll be also used as email address when advising the user to "contact support"
     */
    val appSupportEmailAddress: String = "support@baf.com"
}