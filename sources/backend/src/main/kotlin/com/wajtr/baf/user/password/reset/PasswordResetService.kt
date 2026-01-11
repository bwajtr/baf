package com.wajtr.baf.user.password.reset

import com.wajtr.baf.core.commons.generateRandomAlphanumeric
import com.wajtr.baf.user.AccountStatusCheckResult.*
import com.wajtr.baf.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.SecureRandom

const val PASSWORD_RESET_PAGE = "accounts/reset"
const val PASSWORD_RESET_URL = "/" + PASSWORD_RESET_PAGE
const val PASSWORD_RESET_PREPARATION_PAGE = "accounts/resetprep"


/**
 * Services related to the users and their accounts.
 *
 * @author Bretislav Wajtr
 */
interface PasswordResetService {

    /**
     * This method initiates the password reset process, which tries to comply with OWASP recommendations as close as possible
     * -> checks if an account with given email exits, is verified and if so then generates password reset token (if the token
     * isn't already existing). The password reset token expires after some limited amount of time (i.e. 20 minutes), so this
     * gives the user short time window to reset his/her password. Note that this token is later used for validation of the
     * password reset form -> only non-expired tokens assigned to some email are considered valid.
     *
     * @param email         Non-null email assigned to an account for which the password process should start
     * @param baseServerUrl Base url of the server, will be used for generation of the email for the user
     * @return Result of the operation, see PasswordResetInitiationResult enum for more detail.
     */
    fun initiatePasswordResetProcess(email: String, baseServerUrl: String): PasswordResetInitiationResult

    /**
     * The second part of the password reset process - this method should be called to update the users password based
     * on the password reset token, which was created during method initiatePasswordResetProcess(). If the provided token
     * is valid, assigned to some email and not expired, then the newPassword is set to the account represented by the email
     * which is tied to the token.
     *
     * @param token       Password reset token created during execution of initiatePasswordResetProcess() method
     * @param newPassword New password to be set to the account
     * @return Result of the operation, see PasswordResetResult enum for more detail.
     */
    fun performPasswordReset(token: String, newPassword: String): PasswordResetResult
}


enum class PasswordResetInitiationResult {
    ACCOUNT_NOT_FOUND, // No account with provided email has been found
    EMAIL_NOT_VERIFIED, // An account with provided email exists, but the email has not been verified yet
    PROCESS_ALREADY_STARTED, // An account with such email exists and is verified, but the password reset process was already initiated and email sent; no new token was generated
    RESET_PROCESS_INITIATED, // An account with such email exists and is ready for password reset; password token was generated and email sent
    EMAIL_SENDING_FAILED // An attempt to send email with password reset instructions failed, the whole process was interrupted and cancelled
}

enum class PasswordResetResult {
    INVALID_TOKEN, // password reset token was either null, invalid or expired; no password was therefore updated
    PASSWORD_CHANGED // token was valid -> password of the user assigned to the token was therefore updated
}

/**
 * @author Bretislav Wajtr
 */
@Service
class PasswordResetServiceImpl(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val mailSender: PasswordResetMailSender,
    private val passwordResetRepository: PasswordResetRepository
) : PasswordResetService {

    private val log = LoggerFactory.getLogger(PasswordResetService::class.java)

    override fun initiatePasswordResetProcess(email: String, baseServerUrl: String): PasswordResetInitiationResult {
        val accountStatusCheckResult = userRepository.checkAccountStatus(email)

        return when (accountStatusCheckResult) {
            NOT_FOUND -> handleAccountNotFoundResult(email)
            NOT_VERIFIED -> handleAccountNotVerifiedResult(email)
            OK -> handleAccountCheckOkResult(email, baseServerUrl)
        }
    }

    private fun handleAccountCheckOkResult(email: String, baseServerUrl: String): PasswordResetInitiationResult {

        return if (passwordResetTokenRepository.tokenExistsForEmail(email)) {
            handleProcessAlreadyRunningForEmail(email)
        } else {
            // all ok, start the password reset process
            val resetToken = SecureRandom()
                .generateRandomAlphanumeric(16)
                .chunked(4)
                .joinToString("-")
            val passwordResetUrl = createPasswordResetUrl(baseServerUrl)
            val success = mailSender.sendPasswordResetEmail(email, passwordResetUrl, resetToken)
            if (success) {
                passwordResetTokenRepository.storeToken(resetToken, email)
                log.info("Initiated password reset process for email {}", email)
                PasswordResetInitiationResult.RESET_PROCESS_INITIATED
            } else {
                log.error("Sending email to $email as part of password reset process FAILED; rolling back the process")
                PasswordResetInitiationResult.EMAIL_SENDING_FAILED
            }
        }
    }

    private fun handleProcessAlreadyRunningForEmail(email: String): PasswordResetInitiationResult {
        log.info("Attempt to initiate password reset process failed: process already running for email $email")
        return PasswordResetInitiationResult.PROCESS_ALREADY_STARTED
    }

    private fun handleAccountNotVerifiedResult(email: String): PasswordResetInitiationResult {
        log.info("Attempt to initiate password reset process failed: account with email $email is not verified")
        return PasswordResetInitiationResult.EMAIL_NOT_VERIFIED
    }

    fun handleAccountNotFoundResult(email: String): PasswordResetInitiationResult {
        log.info("Attempt to initiate password reset process failed: account with email $email was not found")
        return PasswordResetInitiationResult.ACCOUNT_NOT_FOUND
    }

    override fun performPasswordReset(token: String, newPassword: String): PasswordResetResult {
        val ret: PasswordResetResult

        val accountEmail = passwordResetTokenRepository.retrieveEmailForToken(token)
        if (accountEmail == null) {
            log.warn("Attempt was made to perform account password reset, but token was either invalid or expired: $token")
            ret = PasswordResetResult.INVALID_TOKEN
        } else {
            passwordResetTokenRepository.removeToken(token)
            val updateUserPasswordResult = passwordResetRepository.updateUserPassword(accountEmail, newPassword)
            if (updateUserPasswordResult === UpdateUserPasswordResult.OK) {
                log.info("Account password reset performed for account with email $accountEmail")
                ret = PasswordResetResult.PASSWORD_CHANGED
            } else {
                // In case of any error during the password update simply return "invalid token" result.
                // The token is already invalid at this point. This all will force the user to restart whole process.
                // Maybe it's annoying, but definitely safer
                ret = PasswordResetResult.INVALID_TOKEN
                log.warn("Account password update failed for account with email $accountEmail with result $updateUserPasswordResult. User will be forced to restart the process.")
            }
        }

        return ret
    }

    private fun createPasswordResetUrl(baseServerUrl: String): String {
        return baseServerUrl + PASSWORD_RESET_URL
    }

}
