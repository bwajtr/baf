package com.wajtr.baf.core.email

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Configuration properties for company information displayed in email footers.
 * Required for CAN-SPAM and other email regulations compliance.
 *
 * @author Bretislav Wajtr
 */
@Component
@ConfigurationProperties(prefix = "company")
data class CompanyProperties(
    var name: String = "",
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var postalCode: String = "",
    var country: String = ""
) {
    /**
     * Returns formatted mailing address for email footers.
     */
    fun getFormattedAddress(): String {
        return "$address, $city, $state $postalCode, $country"
    }
}
