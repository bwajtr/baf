package com.wajtr.baf.user.emailverification

import com.wajtr.baf.db.jooq.Tables.APP_USER
import com.wajtr.baf.user.UserRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/**
 * @author Bretislav Wajtr
 */
@Repository
@Transactional
class UserEmailVerificationDAO(
    private val create: DSLContext,
    private val userRepository: UserRepository
) {

    @OptIn(ExperimentalUuidApi::class)
    fun createEmailVerificationToken(
        email: String,
        forceCreateNew: Boolean
    ): EmailVerificationTokenCreationResult {
        val user = userRepository.loadUserByUsername(email)

        if (user.emailIsVerified)
            return EmailVerificationTokenCreationResult(EmailVerificationTokenCreationStatus.ALREADY_VERIFIED, null)

        if (user.emailVerificationToken == null || forceCreateNew) {
            val newToken = Uuid.generateV7().toJavaUuid()
            userRepository.updateUserEmailVerificationToken(user.id, newToken)
            return EmailVerificationTokenCreationResult(
                EmailVerificationTokenCreationStatus.NEW_TOKEN_CREATED,
                newToken
            )
        } else {
            return EmailVerificationTokenCreationResult(
                EmailVerificationTokenCreationStatus.TOKEN_EXISTS,
                user.emailVerificationToken
            )
        }
    }

    fun confirmEmailVerificationToken(token: String): EmailVerificationConfirmationResult {
        val userId = create.select(APP_USER.ID).from(APP_USER)
            .where(APP_USER.EMAIL_VERIFICATION_TOKEN.eq(UUID.fromString(token)))
            .fetchOneInto(UUID::class.java)

        if (userId != null) {
            userRepository.updateUserEmailVerified(userId, true)
            return EmailVerificationConfirmationResult.TOKEN_VALID
        } else {
            return EmailVerificationConfirmationResult.TOKEN_INVALID
        }
    }

    fun clearEmailVerificationToken(email: String) {
        val user = userRepository.loadUserByUsername(email)
        userRepository.updateUserEmailVerificationToken(user.id, null)
    }
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