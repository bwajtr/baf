package com.wajtr.baf.authentication.db

import com.wajtr.baf.db.jooq.Public.AUTHENTICATE_USER
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author Bretislav Wajtr
 */
@Service
@Transactional
class PasswordVerificationService(
    private val dslContext: DSLContext
) {

    fun verifyPassword(email: String, password: String): PasswordVerificationResult {
        return dslContext
            .selectFrom(AUTHENTICATE_USER(email, password))
            .fetchOneInto(PasswordVerificationResult::class.java)
            ?: PasswordVerificationResult.NOT_OK
    }

}


enum class PasswordVerificationResult {
    OK, // User was successfully authenticated
    NOT_OK  // Given account either does not exist or the credentials were not correct
}