package com.wajtr.baf.user.password.reset

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

/**
 * Repository which stores the password reset tokens for a limited time. Tokens are removed from the repository automatically
 * after predefined time.
 *
 * @author Bretislav Wajtr
 */
interface PasswordResetTokenRepository {

    /**
     * Stores the given token for a limited period of time.
     *
     * @param email Not null valid email of an existing account
     * @param token Not null password reset token
     */
    fun storeToken(token: String, email: String)

    /**
     * @return Returns email assigned to the token if the provided token is a valid token in the storage and is non-expired. Returns null
     * for both cases when no such token ever existed or token existed but is expired.
     */
    fun retrieveEmailForToken(token: String): String?

    /**
     * @return Returns true if a valid token is present in the storage and is non-expired for the given email. Returns false otherwise
     */
    fun tokenExistsForEmail(email: String): Boolean

    /**
     * Removes token and email from the repository immediately. Null will be returned if retrieveEmailForToken is called for token which
     * was removed using this method.
     *
     * @param token Token information to be removed
     */
    fun removeToken(token: String)

    /**
     * Removes all known tokens from the repository immediately.
     */
    fun clearRepository()

}

const val PASSWORD_RESET_TOKEN_EXPIRY_MINUTES: Long = 30

/**
 *
 * @author Bretislav Wajtr
 */
@Repository
class PasswordResetTokenRepositoryImpl : PasswordResetTokenRepository {

    private val storage = Caffeine.newBuilder()
        .expireAfterWrite(PASSWORD_RESET_TOKEN_EXPIRY_MINUTES, TimeUnit.MINUTES)
        .build<String, String>()

    override fun storeToken(token: String, email: String) {
        storage.put(token, email)
    }

    override fun retrieveEmailForToken(token: String): String? {
        return storage.getIfPresent(token)
    }

    override fun tokenExistsForEmail(email: String): Boolean {
        // note that we want this operation to be case insensitive -> we want to treat John.Doe@gmail.com and john.doe@gmail.com as
        // same email when searching for it in the map
        return storage.asMap().values.any {it.equals(email, ignoreCase = true)}
    }

    override fun removeToken(token: String) {
        storage.invalidate(token)
    }

    override fun clearRepository() {
        storage.invalidateAll()
    }

}
