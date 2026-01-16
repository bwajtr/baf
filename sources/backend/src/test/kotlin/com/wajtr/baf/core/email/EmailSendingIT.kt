package com.wajtr.baf.core.email

import com.wajtr.baf.core.email.localpreview.LocalFilePreviewEmailSender
import com.wajtr.baf.organization.delete.OrganizationDeletedMailSender
import com.wajtr.baf.organization.invitation.InvitationMailSender
import com.wajtr.baf.test.BaseIntegrationTest
import com.wajtr.baf.user.account.delete.AccountDeletedMailSender
import com.wajtr.baf.user.emailverification.EmailVerificationMailSender
import com.wajtr.baf.user.password.change.PasswordChangeMailSender
import com.wajtr.baf.user.password.reset.PasswordResetMailSender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.nio.file.Files
import java.time.ZoneId
import java.util.*

/**
 * Integration tests for the email sending system.
 * 
 * These tests verify that all email types (verification, password reset, password changed, 
 * account deleted, organization deleted, invitation) are properly generated and saved to the 
 * filesystem when using LocalFilePreviewEmailSender.
 * 
 * The tests check:
 * - Email is saved as an HTML file
 * - Email content contains expected elements (URLs, names, etc.)
 * - Email metadata is included in the file
 */
class EmailSendingIT : BaseIntegrationTest() {

    @Autowired
    private lateinit var emailVerificationMailSender: EmailVerificationMailSender

    @Autowired
    private lateinit var passwordResetMailSender: PasswordResetMailSender

    @Autowired
    private lateinit var invitationMailSender: InvitationMailSender

    @Autowired
    private lateinit var passwordChangeMailSender: PasswordChangeMailSender

    @Autowired
    private lateinit var accountDeletedMailSender: AccountDeletedMailSender

    @Autowired
    private lateinit var organizationDeletedMailSender: OrganizationDeletedMailSender

    @Autowired
    private lateinit var localFilePreviewEmailSender: LocalFilePreviewEmailSender

    @BeforeEach
    fun cleanupEmails() {
        localFilePreviewEmailSender.clearSentEmails()
    }

    @Test
    fun `sendVerificationMail should save email with verification URL`() {
        // Given
        val emailAddress = "test-verify@example.com"
        val verificationUrl = "https://app.example.com/verify?token=abc123"

        // When
        val result = emailVerificationMailSender.sendVerificationMail(
            emailAddress = emailAddress,
            verificationUrl = verificationUrl,
            locale = Locale.ENGLISH
        )

        // Then
        assertThat(result)
            .describedAs("Email should be sent successfully")
            .isTrue()

        val savedFile = localFilePreviewEmailSender.getLastSentEmailFile()
        assertThat(savedFile)
            .describedAs("Email file should be saved")
            .isNotNull()

        val content = Files.readString(savedFile!!)
        
        assertThat(content)
            .describedAs("Email should contain recipient address in metadata")
            .contains("To: $emailAddress")
        
        assertThat(content)
            .describedAs("Email should contain subject in metadata")
            .contains("Subject: Verify your email address")
        
        assertThat(content)
            .describedAs("Email should contain verification URL")
            .contains(verificationUrl)
        
        assertThat(content)
            .describedAs("Email should contain application name")
            .contains("TestApp")
    }

    @Test
    fun `sendPasswordResetEmail should save email with reset URL`() {
        // Given
        val emailAddress = "test-reset@example.com"
        val resetUrl = "https://app.example.com/reset-password?token=xyz789"
        val resetToken = "xyz789"

        // When
        val result = passwordResetMailSender.sendPasswordResetEmail(
            emailAddress = emailAddress,
            passwordResetUrl = resetUrl,
            resetToken = resetToken,
            locale = Locale.ENGLISH
        )

        // Then
        assertThat(result)
            .describedAs("Email should be sent successfully")
            .isTrue()

        val savedFile = localFilePreviewEmailSender.getLastSentEmailFile()
        assertThat(savedFile)
            .describedAs("Email file should be saved")
            .isNotNull()

        val content = Files.readString(savedFile!!)
        
        assertThat(content)
            .describedAs("Email should contain recipient address in metadata")
            .contains("To: $emailAddress")
        
        assertThat(content)
            .describedAs("Email should contain subject in metadata")
            .contains("Subject: Reset your password")
        
        assertThat(content)
            .describedAs("Email should contain reset URL")
            .contains(resetUrl)
        
        assertThat(content)
            .describedAs("Email should contain application name")
            .contains("TestApp")
    }

    @Test
    fun `sendPasswordChangedNotification should save email with security warning`() {
        // Given
        val emailAddress = "test-changed@example.com"
        val locale = Locale.ENGLISH
        val zoneId = ZoneId.of("Europe/Prague")

        // When
        val result = passwordChangeMailSender.sendPasswordChangedNotification(
            emailAddress = emailAddress,
            locale = locale,
            zoneId = zoneId
        )

        // Then
        assertThat(result)
            .describedAs("Email should be sent successfully")
            .isTrue()

        val savedFile = localFilePreviewEmailSender.getLastSentEmailFile()
        assertThat(savedFile)
            .describedAs("Email file should be saved")
            .isNotNull()

        val content = Files.readString(savedFile!!)

        assertThat(content)
            .describedAs("Email should contain recipient address in metadata")
            .contains("To: $emailAddress")

        assertThat(content)
            .describedAs("Email should contain subject in metadata")
            .contains("Subject: Your password has been changed")

        assertThat(content)
            .describedAs("Email should contain the email address in the body")
            .contains(emailAddress)

        assertThat(content)
            .describedAs("Email should contain security warning")
            .contains("Wasn't you?")

        assertThat(content)
            .describedAs("Email should contain action items for compromised account")
            .contains("Reset your password immediately")

        assertThat(content)
            .describedAs("Email should contain application name")
            .contains("TestApp")
    }

    @Test
    fun `sendAccountDeletedNotification should save email with deletion confirmation`() {
        // Given
        val emailAddress = "test-deleted@example.com"
        val locale = Locale.ENGLISH
        val zoneId = ZoneId.of("Europe/Prague")

        // When
        val result = accountDeletedMailSender.sendAccountDeletedNotification(
            emailAddress = emailAddress,
            locale = locale,
            zoneId = zoneId
        )

        // Then
        assertThat(result)
            .describedAs("Email should be sent successfully")
            .isTrue()

        val savedFile = localFilePreviewEmailSender.getLastSentEmailFile()
        assertThat(savedFile)
            .describedAs("Email file should be saved")
            .isNotNull()

        val content = Files.readString(savedFile!!)

        assertThat(content)
            .describedAs("Email should contain recipient address in metadata")
            .contains("To: $emailAddress")

        assertThat(content)
            .describedAs("Email should contain subject in metadata")
            .contains("Subject: Your account has been deleted")

        assertThat(content)
            .describedAs("Email should contain the email address in the body")
            .contains(emailAddress)

        assertThat(content)
            .describedAs("Email should contain security warning")
            .contains("Wasn't you?")

        assertThat(content)
            .describedAs("Email should mention permanent deletion")
            .contains("permanently deleted")

        assertThat(content)
            .describedAs("Email should contain application name")
            .contains("TestApp")
    }

    @Test
    fun `sendOrganizationDeletedNotification should save email with organization deletion confirmation`() {
        // Given
        val emailAddress = "test-org-deleted@example.com"
        val organizationName = "Acme Corporation"
        val locale = Locale.ENGLISH
        val zoneId = ZoneId.of("Europe/Prague")

        // When
        val result = organizationDeletedMailSender.sendOrganizationDeletedNotification(
            emailAddress = emailAddress,
            organizationName = organizationName,
            locale = locale,
            zoneId = zoneId
        )

        // Then
        assertThat(result)
            .describedAs("Email should be sent successfully")
            .isTrue()

        val savedFile = localFilePreviewEmailSender.getLastSentEmailFile()
        assertThat(savedFile)
            .describedAs("Email file should be saved")
            .isNotNull()

        val content = Files.readString(savedFile!!)

        assertThat(content)
            .describedAs("Email should contain recipient address in metadata")
            .contains("To: $emailAddress")

        assertThat(content)
            .describedAs("Email should contain subject in metadata")
            .contains("Subject: Your organization has been deleted")

        assertThat(content)
            .describedAs("Email should contain the organization name in the body")
            .contains(organizationName)

        assertThat(content)
            .describedAs("Email should mention permanent deletion")
            .contains("permanently deleted")

        assertThat(content)
            .describedAs("Email should mention organization data")
            .contains("organization data")

        assertThat(content)
            .describedAs("Email should contain application name")
            .contains("TestApp")
    }

    @Test
    fun `sendInvitationEmail should save email with invitation details`() {
        // Given
        val emailAddress = "new-member@example.com"
        val acceptanceUrl = "https://app.example.com/accept-invitation/invitation-id-123"
        val inviterName = "John Smith"
        val organizationName = "Acme Corporation"
        val role = "ROLE_ADMIN"

        // When
        val result = invitationMailSender.sendInvitationEmail(
            emailAddress = emailAddress,
            acceptanceUrl = acceptanceUrl,
            inviterName = inviterName,
            organizationName = organizationName,
            role = role,
            locale = Locale.ENGLISH
        )

        // Then
        assertThat(result)
            .describedAs("Email should be sent successfully")
            .isTrue()

        val savedFile = localFilePreviewEmailSender.getLastSentEmailFile()
        assertThat(savedFile)
            .describedAs("Email file should be saved")
            .isNotNull()

        val content = Files.readString(savedFile!!)
        
        assertThat(content)
            .describedAs("Email should contain recipient address in metadata")
            .contains("To: $emailAddress")
        
        assertThat(content)
            .describedAs("Email should contain subject with organization name in metadata")
            .contains("Subject: You've been invited to join $organizationName")
        
        assertThat(content)
            .describedAs("Email should contain acceptance URL")
            .contains(acceptanceUrl)
        
        assertThat(content)
            .describedAs("Email should contain inviter name")
            .contains(inviterName)
        
        assertThat(content)
            .describedAs("Email should contain organization name")
            .contains(organizationName)
        
        // Role should be localized via i18n("role.ADMIN")
        assertThat(content)
            .describedAs("Email should contain localized role name")
            .contains("Admin")
        
        assertThat(content)
            .describedAs("Email should contain application name")
            .contains("TestApp")
    }
}
