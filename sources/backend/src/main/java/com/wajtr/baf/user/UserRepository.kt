package com.wajtr.baf.user

import com.wajtr.baf.db.jooq.Tables.APP_USER
import com.wajtr.baf.db.jooq.Tables.APP_USER_ROLE_TENANT
import com.wajtr.baf.db.jooq.routines.EncryptPassword
import com.wajtr.baf.db.jooq.tables.records.AppUserRecord
import org.jooq.DSLContext
import org.jooq.RecordMapper
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.net.UnknownHostException
import java.util.*

/**
 * @author Bretislav Wajtr
 */
@Repository
@Transactional
class UserRepository(private val create: DSLContext) {

    private val mapIntoUser: RecordMapper<AppUserRecord, User> = RecordMapper { record: AppUserRecord ->
        try {
            return@RecordMapper User(
                record.id,
                record.name,
                record.email,
                record.emailVerified,
                record.createdAt.toInstant(),
                record.emailVerificationToken
            )
        } catch (e: UnknownHostException) {
            throw RuntimeException(e)
        }
    }

    private val loadUserFunctionMapIntoUser: RecordMapper<AppUserRecord, User> =
        RecordMapper { record: AppUserRecord ->
            try {
                return@RecordMapper User(
                    record.id,
                    record.name,
                    record.email,
                    record.emailVerified,
                    record.createdAt.toInstant(),
                    record.emailVerificationToken
                )
            } catch (e: UnknownHostException) {
                throw RuntimeException(e)
            }
        }


    fun findById(id: UUID): User? {
        return create.selectFrom(APP_USER)
            .where(APP_USER.ID.eq(id))
            .fetchOne(mapIntoUser)
    }

    fun findAll(): MutableList<User> {
        return create
            .selectFrom(APP_USER)
            .fetch(mapIntoUser)
    }

    fun update(input: User): Int {
        return create.update(APP_USER)
            .set(APP_USER.NAME, input.name)
            .set(APP_USER.EMAIL, input.email)
            .set(APP_USER.EMAIL_VERIFIED, input.emailIsVerified)
            .where(APP_USER.ID.eq(input.id))
            .execute()
    }

    fun resolveTenantIdsOfUser(userId: UUID): List<UUID> {
        return create.selectDistinct(APP_USER_ROLE_TENANT.TENANT_ID)
            .from(APP_USER_ROLE_TENANT)
            .where(APP_USER_ROLE_TENANT.USER_ID.eq(userId))
            .fetchInto<UUID>(UUID::class.java)
    }

    fun updateUserName(id: UUID, name: String): Boolean {
        return create.update(APP_USER)
            .set(APP_USER.NAME, name)
            .where(APP_USER.ID.eq(id))
            .execute() > 0
    }

    fun updateUserEmailVerificationToken(id: UUID, token: UUID?) {
        create.update(APP_USER)
            .set(APP_USER.EMAIL_VERIFICATION_TOKEN, token)
            .where(APP_USER.ID.eq(id))
            .execute()
    }

    fun updateUserEmailVerified(id: UUID, verified: Boolean) {
        create.update(APP_USER)
            .set(APP_USER.EMAIL_VERIFIED, verified)
            .where(APP_USER.ID.eq(id))
            .execute()
    }

    fun updateUserPassword(id: UUID, password: String) {
        val encryptedPassword = encryptPassword(password)

        create.update(APP_USER)
            .set(APP_USER.PASSWORD, encryptedPassword)
            .where(APP_USER.ID.eq(id))
            .execute()
    }

    fun updateUserEmail(id: UUID, email: String): UpdateUserEmailResult {
        var result: UpdateUserEmailResult

        try {
            val updateCount: Int = create.update(APP_USER)
                .set(APP_USER.EMAIL, email)
                .where(APP_USER.ID.eq(id))
                .execute()

            result = if (updateCount > 0) UpdateUserEmailResult.OK else UpdateUserEmailResult.NOT_OK
        } catch (_: DuplicateKeyException) {
            result = UpdateUserEmailResult.DUPLICATE_VALUE
        }

        return result
    }

    fun remove(userId: UUID): Boolean {
        // note that additional data related to this user will be removed from database as well thanks to ON DELETE CASCADE
        return create.deleteFrom(APP_USER).where(APP_USER.ID.eq(userId)).execute() > 0
    }

    fun loadUserByUsername(email: String): User {
        val user: User? = create.selectFrom(APP_USER)
            .where(APP_USER.EMAIL.eq(email))
            .fetchOne(loadUserFunctionMapIntoUser)

        if (user == null) {
            throw UsernameNotFoundException("User with email " + email + " not found")
        }
        return user
    }

    fun checkAccountStatus(email: String): AccountStatusCheckResult {
        val userRecord = create.select()
            .from(APP_USER)
            .where(APP_USER.EMAIL.eq(email))
            .fetchOne()

        return if (userRecord == null) {
            // TODO fix this when invitations are implemented
//            val invitationRecord: Record? = create.select()
//                .from(USER_INVITATION)
//                .where(org.jooq.impl.DSL.field("email").eq(email))
//                .fetchOne()

            val invitationRecord = null
            if (invitationRecord != null) {
                AccountStatusCheckResult.INVITED
            } else {
                AccountStatusCheckResult.NOT_FOUND
            }
        } else {
            val emailVerified: Boolean = userRecord.get(APP_USER.EMAIL_VERIFIED)
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
    NOT_FOUND, // account with such email was not found
    NOT_VERIFIED, // account with such email exists, but it's not verified
    INVITED,  // account with such email was not found, but there is an invitation pending
    OK // account is OK and active
}

enum class UpdateUserEmailResult {
    OK, // email updated
    NOT_OK, // email not updated for other reasons (possibly because no record to be updated was found)
    DUPLICATE_VALUE  // email not updated because another account with same email already exists
}