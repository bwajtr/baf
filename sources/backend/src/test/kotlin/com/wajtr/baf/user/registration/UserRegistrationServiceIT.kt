package com.wajtr.baf.user.registration

import com.wajtr.baf.test.BaseIntegrationTest
import com.wajtr.baf.user.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.net.InetAddress
import java.time.ZoneId
import java.util.*

/**
 * Integration test for UserRegistrationService.
 * Tests user registration functionality including storage of user preferences.
 */
class UserRegistrationServiceIT : BaseIntegrationTest() {

    @Autowired
    private lateinit var userRegistrationService: UserRegistrationService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `registerUserOfNewTenant should store preferred locale and timezone`() {
        // Given: A registration request with specific locale and timezone
        val preferredLocale = Locale.forLanguageTag("de-DE")
        val preferredTimezone = ZoneId.of("Europe/Berlin")
        val email = "test.user.${System.currentTimeMillis()}@example.com"

        val request = UserAndTenantRegistrationRequest(
            name = "Test User",
            email = email,
            password = "securePassword123",
            ipAddress = InetAddress.getByName("127.0.0.1"),
            preferredLocale = preferredLocale,
            preferredTimezoneId = preferredTimezone
        )

        // When: User registers
        val result = userRegistrationService.registerUserOfNewTenant(request)

        // Then: Registration should succeed
        assertThat(result)
            .describedAs("Registration should succeed")
            .isInstanceOf(UserRegistrationSuccess::class.java)

        // And: The user should be loadable by username with correct preferences
        val registeredUser = userRepository.loadUserByUsername(email)

        assertThat(registeredUser.preferredLocale)
            .describedAs("Preferred locale should be stored and retrieved correctly")
            .isEqualTo(preferredLocale)

        assertThat(registeredUser.preferredTimezoneId)
            .describedAs("Preferred timezone should be stored and retrieved correctly")
            .isEqualTo(preferredTimezone)
    }

    @Test
    fun `registerUserOfNewTenant should store US locale and New York timezone`() {
        // Given: A registration request with US locale and America/New_York timezone
        val preferredLocale = Locale.forLanguageTag("en-US")
        val preferredTimezone = ZoneId.of("America/New_York")
        val email = "american.user.${System.currentTimeMillis()}@example.com"

        val request = UserAndTenantRegistrationRequest(
            name = "American User",
            email = email,
            password = "securePassword456",
            ipAddress = InetAddress.getByName("192.168.1.1"),
            preferredLocale = preferredLocale,
            preferredTimezoneId = preferredTimezone
        )

        // When: User registers
        val result = userRegistrationService.registerUserOfNewTenant(request)

        // Then: Registration should succeed
        assertThat(result)
            .describedAs("Registration should succeed")
            .isInstanceOf(UserRegistrationSuccess::class.java)

        // And: The user should be loadable by username with correct preferences
        val registeredUser = userRepository.loadUserByUsername(email)

        assertThat(registeredUser.preferredLocale)
            .describedAs("US locale should be stored and retrieved correctly")
            .isEqualTo(preferredLocale)

        assertThat(registeredUser.preferredTimezoneId)
            .describedAs("America/New_York timezone should be stored and retrieved correctly")
            .isEqualTo(preferredTimezone)
    }

    @Test
    fun `loadUserByUsername should correctly read locale and timezone from test data`() {
        // Given: Jane Admin from test data has cs-CZ locale and Europe/Prague timezone

        // When: Loading Jane Admin by email
        val janeAdmin = userRepository.loadUserByUsername("jane.admin@acme.com")

        // Then: Preferences should match what was set in test data
        assertThat(janeAdmin.preferredLocale)
            .describedAs("Jane Admin should have Czech locale from test data")
            .isEqualTo(Locale.forLanguageTag("cs-CZ"))

        assertThat(janeAdmin.preferredTimezoneId)
            .describedAs("Jane Admin should have Europe/Prague timezone from test data")
            .isEqualTo(ZoneId.of("Europe/Prague"))
    }

    @Test
    fun `loadUserByUsername should correctly read locale and timezone as null from test data`() {
        // Given: Josh Owner from test data has NULL locale and timezone

        // When: Loading Josh Owner by email
        val joshOwner = userRepository.loadUserByUsername("josh.owner@acme.com")

        // Then: Preferences should be null as set in test data
        assertThat(joshOwner.preferredLocale)
            .describedAs("Josh Owner should have null locale from test data")
            .isNull()

        assertThat(joshOwner.preferredTimezoneId)
            .describedAs("Josh Owner should have null timezone from test data")
            .isNull()
    }
}
