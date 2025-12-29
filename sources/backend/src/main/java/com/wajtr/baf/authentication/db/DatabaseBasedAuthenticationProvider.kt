package com.wajtr.baf.authentication.db

import com.wajtr.baf.authentication.AuthenticationDetailsService
import com.wajtr.baf.user.AccountStatusCheckResult
import com.wajtr.baf.user.AccountStatusCheckResult.NOT_VERIFIED
import com.wajtr.baf.user.UserRepository
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service

/**
 * @author Bretislav Wajtr
 */
@Service
class DatabaseBasedAuthenticationProvider(
    private val passwordVerificationService: PasswordVerificationService,
    private val authenticationDetailsService: AuthenticationDetailsService,
    private val userRepository: UserRepository,
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        if (authentication.credentials == null) {
            throw BadCredentialsException("Bad credentials; no Credentials supplied")
        }

        val email = if (authentication.principal == null)
            "NONE_PROVIDED"
        else
            authentication.name
        val password: String = authentication.credentials.toString()

        val passwordVerificationResult: PasswordVerificationResult = passwordVerificationService.verifyPassword(email, password)
        if (passwordVerificationResult == PasswordVerificationResult.OK) { // if email+password fits

            // check the status of the account first (verified, locked, disabled etc.)
            val accountStatus: AccountStatusCheckResult = userRepository.checkAccountStatus(email)
            if (accountStatus === NOT_VERIFIED) {
                throw EmailNotVerifiedException(email)
            }

            // everything is ok -> load all the details of the user and set it to the context
            val authDetails = authenticationDetailsService.loadAuthenticationDetails(email)

            val result = UsernamePasswordAuthenticationToken(
                authDetails.user, authentication.credentials, authDetails.roles
            )
            result.details = authDetails.tenant
            return result
        } else {
            throw BadCredentialsException("Bad credentials")
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return (UsernamePasswordAuthenticationToken::class.java
            .isAssignableFrom(authentication))
    }
}


class EmailNotVerifiedException(val email: String) : AuthenticationException("Email not verified")