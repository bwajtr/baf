package com.wajtr.baf.user.password.reset

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.Assert

/**
 * A Service for sending "password reset" emails
 *
 * @author Bretislav Wajtr
 */
@Service
class PasswordResetMailSender {

    private val log = LoggerFactory.getLogger(PasswordResetMailSender::class.java)

    fun sendPasswordResetEmail(emailAddress: String, passwordResetUrl: String, resetToken: String): Boolean {
        Assert.hasText(passwordResetUrl, "Reset URL must not be empty, the user would get email with no URL otherwise!")
        Assert.hasText(resetToken, "There is no point sending a reset email with no token")

        log.info("Emulating sending email to $emailAddress with a password reset url $passwordResetUrl and reset token $resetToken")
        return true

    }

    fun sendInvitationReminderEmail(emailAddress: String): Boolean {
        log.info("Emulating sending invitation reminder email to $emailAddress")
        return true
    }

}