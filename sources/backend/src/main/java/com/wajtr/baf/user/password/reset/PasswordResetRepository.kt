package com.wajtr.baf.user.password.reset

import com.wajtr.baf.user.UserRepository
import org.jooq.DSLContext
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author Bretislav Wajtr
 */
@Service
@Transactional
class PasswordResetRepository(
    private val create: DSLContext,
    private val userRepository: UserRepository
) {

    fun updateUserPassword(accountEmail: String, password: String): UpdateUserPasswordResult {
        return try {
            val user = userRepository.loadUserByUsername(accountEmail)
            userRepository.updateUserPassword(user.id, password)
            UpdateUserPasswordResult.OK
        } catch (_: UsernameNotFoundException) {
            UpdateUserPasswordResult.NOT_FOUND
        }
    }
}


enum class UpdateUserPasswordResult {
    NOT_FOUND, // account with such email was not found
    OK // Password for given account was updated
}