package com.wajtr.baf.user.emailverification

import com.wajtr.baf.user.emailverification.EmailVerificationConfirmationResult.TOKEN_VALID
import com.wajtr.baf.user.emailverification.EmailVerificationTokenCreationStatus.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

const val CONFIRM_EMAIL_OWNERSHIP_URL = "/api/confirm"

/**
 * Service for user email verification (==verification of ownership) process where a special token (stored to database) and
 * unique URL (generated from this token) is created for the user. This URL is then sent to the user via email and when user
 * enters this URL to browser (or clicks on a button with this URL in the email) his ownership of the email address is proven.
 * 
 * 
 * This "persisted" email verification process differs from the "inmemory" email verification process (ChangeUserEmailService) in storage of the token:
 * "persisted" email verification can survive server restart and verification of the email can be completed any time,
 * the "inmemory" will not survive server restart and validity and usability of the token is limited in time.
 * 
 * @author Bretislav Wajtr
 */
@Service
class EmailVerificationService(
    private val mailSender: EmailVerificationMailSender,
    userEmailVerificationDAO: UserEmailVerificationDAO
) {
    private val userEmailVerificationDAO: UserEmailVerificationDAO

    init {
        this.userEmailVerificationDAO = userEmailVerificationDAO
    }

    /**
     * Starts (or continues) the process of verifying the ownership of email address. If user's email is not verified (stored as a flag in the users record), then a new
     * 'email verification token' is created (UUID) and stored to the record. This token does not expire. A mail is sent
     * to the user with URL containing this token. User is expected to click on the URL in the email, which would
     * trigger confirmation of the email address (see confirmEmailVerificationToken(String) method).
     * 
     * @param emailAddressToVerify    Email which should be verified
     * @param baseServerUrl    Base url of the server, will be used for generation of the confirmation URL link
     * @param forceSendNewMail If set to false and the system detects that email was already sent to the user, then no
     * new email is sent unless you call this method with this parameter set to true.
     * @return Returns status - see documentation in UserEmailVerificationStatus enum.
     */
    fun startEmailVerificationProcess(
        emailAddressToVerify: String,
        baseServerUrl: String?,
        forceSendNewMail: Boolean
    ): UserEmailVerificationStatus {

        val emailVerificationTokenCreationResult =
            userEmailVerificationDAO.createEmailVerificationToken(emailAddressToVerify, forceSendNewMail)

        return when (emailVerificationTokenCreationResult.status) {
            ALREADY_VERIFIED -> {
                LOG.info(
                    "An attempt was made to initiate an email verification process, but email {} is already verified.",
                    emailAddressToVerify
                )
                UserEmailVerificationStatus.ALREADY_VERIFIED // user's email address was already verified, no action needed
            }

            TOKEN_EXISTS -> {
                LOG.info(
                    "An attempt was made to initiate an email verification process for {}, but process is already running. Waiting for user action.",
                    emailAddressToVerify
                )
                UserEmailVerificationStatus.WAITING_FOR_VERIFICATION // no new token create, just wait for user to click on URL
            }

            NEW_TOKEN_CREATED -> {
                val token: UUID? = emailVerificationTokenCreationResult.token
                requireNotNull(token) { "We assume that the DB function created new token" }
                val verificationUrl = createEmailVerificationUrl(baseServerUrl, token)
                val mailSuccessfullySent = mailSender.sendVerificationMail(emailAddressToVerify, verificationUrl)
                if (mailSuccessfullySent) {
                    LOG.info("Email verification process initiated for {}", emailAddressToVerify)
                    UserEmailVerificationStatus.NEW_EMAIL_SENT
                } else {
                    // mail sending was not successful -> reset email verification token so the user can start over next time
                    LOG.error(
                        "Sending email to {} to verify email ownership FAILED, rolling back the process",
                        emailAddressToVerify
                    )
                    userEmailVerificationDAO.clearEmailVerificationToken(emailAddressToVerify)
                    UserEmailVerificationStatus.EMAIL_SENDING_FAILED
                }
            }
        }
    }

    /**
     * This is a logic completion of the email verification process. The process starts with call to startEmailVerificationProcess(String, String, boolean).
     * This method is called eventually when user clicks on the link in the verification email. If the token exists, then
     * the email is verified (appropriate flag is set on user record) and token is cleared.
     * 
     * @param token Email verification token created by startEmailVerificationProcess() method to verify
     * @return Returns success or failure - see documentation in EmailVerificationConfirmationResult.
     */
    fun confirmEmailVerificationToken(token: String): EmailVerificationConfirmationResult {
        val result: EmailVerificationConfirmationResult = userEmailVerificationDAO.confirmEmailVerificationToken(token)
        if (result === TOKEN_VALID) {
            LOG.info("Email verification process confirmed for token {}", token)
        } else {
            LOG.warn("Failed to verify email ownership based on token {}", token)
        }
        return result
    }


    private fun createEmailVerificationUrl(baseServerUrl: String?, token: UUID): String {
        return "$baseServerUrl$CONFIRM_EMAIL_OWNERSHIP_URL?$KEY_PARAM=$token"
    }

    companion object {
        const val KEY_PARAM: String = "key"
        private val LOG: Logger = LoggerFactory.getLogger(EmailVerificationService::class.java)
    }
}


enum class UserEmailVerificationStatus {
    NEW_EMAIL_SENT, // a new verification email (with a new token) was sent to the user and we'll wait for response
    WAITING_FOR_VERIFICATION, // the verification email was sent to the user earlier, so we don't have to do anything, just wait for response
    ALREADY_VERIFIED, // the user's email is already verified, no further verification action is necessary
    EMAIL_SENDING_FAILED // attempt to send new verification email failed, the verification process was interrupted
}
