package com.wajtr.baf.user.emailverification

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * A Service for sending "verification URL" emails: A mail is sent to user is sent, which contains URL (or clickable button) ->
 * visiting such URL is enough to verify the users ownershipt of the email address.
 *
 *
 * @author Bretislav Wajtr
 */
@Service
class EmailVerificationMailSender {
    private val log = LoggerFactory.getLogger(EmailVerificationMailSender::class.java)

    /**
     * @return Retuns true if the email was successfully sent, returns false if there was a problem sending the email (email will not be delivered)
     */
    fun sendVerificationMail(emailAddress: String, verificationUrl: String): Boolean {
        log.info("Emulating sending email to $emailAddress with a verificadtion url $verificationUrl")
        return true
    }

}