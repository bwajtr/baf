package com.wajtr.baf.user.emailverification

import com.wajtr.baf.db.jooq.Tables.APP_USER
import com.wajtr.baf.user.UserRepository
import com.wajtr.baf.user.emailverification.EmailVerificationConfirmationResult.TOKEN_VALID
import com.wajtr.baf.user.emailverification.EmailVerificationTokenCreationStatus.*
import org.jooq.DSLContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

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
@Transactional
class EmailVerificationService(
    private val mailSender: EmailVerificationMailSender,
    private val create: DSLContext,
    private val userRepository: UserRepository
) {

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
            createEmailVerificationToken(emailAddressToVerify, forceSendNewMail)

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
                    clearEmailVerificationToken(emailAddressToVerify)
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
        val userId = create.select(APP_USER.ID).from(APP_USER)
            .where(APP_USER.EMAIL_VERIFICATION_TOKEN.eq(UUID.fromString(token)))
            .fetchOneInto(UUID::class.java)

        return if (userId != null) {
            userRepository.updateUserEmailVerified(userId, true)
            LOG.info("Email verification process confirmed for token {}", token)
            TOKEN_VALID
        } else {
            LOG.warn("Failed to verify email ownership based on token {}", token)
            EmailVerificationConfirmationResult.TOKEN_INVALID
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun createEmailVerificationToken(
        email: String,
        forceCreateNew: Boolean
    ): EmailVerificationTokenCreationResult {
        val user = userRepository.loadUserByUsername(email)

        if (user.emailIsVerified)
            return EmailVerificationTokenCreationResult(ALREADY_VERIFIED, null)

        if (user.emailVerificationToken == null || forceCreateNew) {
            val newToken = Uuid.generateV7().toJavaUuid()
            userRepository.updateUserEmailVerificationToken(user.id, newToken)
            return EmailVerificationTokenCreationResult(
                NEW_TOKEN_CREATED,
                newToken
            )
        } else {
            return EmailVerificationTokenCreationResult(
                TOKEN_EXISTS,
                user.emailVerificationToken
            )
        }
    }


    fun clearEmailVerificationToken(email: String) {
        val user = userRepository.loadUserByUsername(email)
        userRepository.updateUserEmailVerificationToken(user.id, null)
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

enum class EmailVerificationConfirmationResult {
    TOKEN_VALID, // token matched record in database, email assigned to the token was therefore verified
    TOKEN_INVALID // no record matched this token -> no email was verified
}

enum class EmailVerificationTokenCreationStatus {
    TOKEN_EXISTS, // verification token already exists, so no new token was created
    NEW_TOKEN_CREATED, // a new token was created
    ALREADY_VERIFIED // no token is actually needed, because the user is already verified
}

data class EmailVerificationTokenCreationResult(
    val status: EmailVerificationTokenCreationStatus,
    val token: UUID? // may be null if status is ALREADY_VERIFIED
)