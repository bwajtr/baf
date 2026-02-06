package com.wajtr.baf.user

import com.wajtr.baf.db.jooq.tables.references.USER_ACCOUNT
import com.wajtr.baf.db.jooq.tables.references.TENANT_MEMBER
import com.wajtr.baf.db.jooq.routines.EncryptPassword
import com.wajtr.baf.db.jooq.tables.records.UserAccountRecord
import org.jooq.DSLContext
import org.jooq.RecordMapper
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.net.UnknownHostException
import java.time.ZoneId
import java.util.*

/**
 * @author Bretislav Wajtr
 */
@Repository
@Transactional
class UserRepository(private val create: DSLContext) {

    private val mapIntoUser: RecordMapper<UserAccountRecord, User> = RecordMapper { record: UserAccountRecord ->
        try {
            return@RecordMapper User(
                record.id!!,
                record.name!!,
                record.email!!,
                record.emailVerified!!,
                record.createdAt!!.toInstant(),
                record.emailVerificationToken,
                record.preferredLocale?.let { Locale.forLanguageTag(it) },
                record.preferredTimezoneId?.let { ZoneId.of(it) }
            )
        } catch (e: UnknownHostException) {
            throw RuntimeException(e)
        }
    }

    private val loadUserFunctionMapIntoUser: RecordMapper<UserAccountRecord, User> =
        RecordMapper { record: UserAccountRecord ->
            try {
                return@RecordMapper User(
                    record.id!!,
                    record.name!!,
                    record.email!!,
                    record.emailVerified!!,
                    record.createdAt!!.toInstant(),
                    record.emailVerificationToken,
                    record.preferredLocale?.let { Locale.forLanguageTag(it) },
                    record.preferredTimezoneId?.let { ZoneId.of(it) }
                )
            } catch (e: UnknownHostException) {
                throw RuntimeException(e)
            }
        }


    fun findById(id: UUID): User? {
        return create.selectFrom(USER_ACCOUNT)
            .where(USER_ACCOUNT.ID.eq(id))
            .fetchOne(mapIntoUser)
    }

    fun update(input: User): Int {
        return create.update(USER_ACCOUNT)
            .set(USER_ACCOUNT.NAME, input.name)
            .set(USER_ACCOUNT.EMAIL, input.email)
            .set(USER_ACCOUNT.EMAIL_VERIFIED, input.emailIsVerified)
            .where(USER_ACCOUNT.ID.eq(input.id))
            .execute()
    }

    fun resolveTenantIdsOfUser(userId: UUID): List<UUID> {
        return create.selectDistinct(TENANT_MEMBER.TENANT_ID)
            .from(TENANT_MEMBER)
            .where(TENANT_MEMBER.USER_ID.eq(userId))
            .fetchInto<UUID>(UUID::class.java)
    }

    fun updateUserName(id: UUID, name: String): Boolean {
        return create.update(USER_ACCOUNT)
            .set(USER_ACCOUNT.NAME, name)
            .where(USER_ACCOUNT.ID.eq(id))
            .execute() > 0
    }

    fun updateUserEmailVerificationToken(id: UUID, token: UUID?) {
        create.update(USER_ACCOUNT)
            .set(USER_ACCOUNT.EMAIL_VERIFICATION_TOKEN, token)
            .where(USER_ACCOUNT.ID.eq(id))
            .execute()
    }

    fun updateUserEmailVerified(id: UUID, verified: Boolean) {
        create.update(USER_ACCOUNT)
            .set(USER_ACCOUNT.EMAIL_VERIFIED, verified)
            .where(USER_ACCOUNT.ID.eq(id))
            .execute()
    }

    fun updateUserPassword(id: UUID, password: String) {
        val encryptedPassword = encryptPassword(password)

        create.update(USER_ACCOUNT)
            .set(USER_ACCOUNT.PASSWORD, encryptedPassword)
            .where(USER_ACCOUNT.ID.eq(id))
            .execute()
    }

    fun remove(userId: UUID): Boolean {
        // note that additional data related to this user will be removed from database as well thanks to ON DELETE CASCADE
        return create.deleteFrom(USER_ACCOUNT).where(USER_ACCOUNT.ID.eq(userId)).execute() > 0
    }

    fun loadUserByUsername(email: String): User {
        val user: User? = create.selectFrom(USER_ACCOUNT)
            .where(USER_ACCOUNT.EMAIL.eq(email))
            .fetchOne(loadUserFunctionMapIntoUser)

        if (user == null) {
            throw UsernameNotFoundException("User with email $email not found")
        }
        return user
    }

    fun checkAccountStatus(email: String): AccountStatusCheckResult {
        val userRecord = create.select()
            .from(USER_ACCOUNT)
            .where(USER_ACCOUNT.EMAIL.eq(email))
            .fetchOne()

        return if (userRecord == null) {
            AccountStatusCheckResult.NOT_FOUND
        } else {
            val emailVerified: Boolean = userRecord.get(USER_ACCOUNT.EMAIL_VERIFIED)!!
            if (!emailVerified) {
                AccountStatusCheckResult.NOT_VERIFIED
            } else {
                AccountStatusCheckResult.OK
            }
        }
    }

    fun encryptPassword(password: String): String {
        val encryptPassword = EncryptPassword()
        encryptPassword.setPassword(password)
        encryptPassword.execute(create.configuration())
        return encryptPassword.returnValue
            ?: throw IllegalStateException("Failed to encrypt password")
    }
}

enum class AccountStatusCheckResult {
    NOT_FOUND, // an account with such email was not found
    NOT_VERIFIED, // an account with such email exists, but it's not verified
    OK // an account is OK and active
}
