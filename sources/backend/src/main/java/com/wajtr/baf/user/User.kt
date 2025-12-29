package com.wajtr.baf.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable
import java.time.Instant
import java.util.*

/**
 *
 * @author Bretislav Wajtr
 */
data class User(
    var id: UUID,
    var name: String,
    var email: String,
    var emailIsVerified: Boolean,
    var createdAt: Instant
) : Serializable, UserDetails {

    override fun getAuthorities(): Set<GrantedAuthority> {
        // getAuthorities is forced by UserDetails interface, but makes no sense here for us
        // use UserContext.getCurrentUserAuthorities() instead, which takes authorities from current authentication
        // token. There is no reason to duplicate this information here. Also, UserDetails (and specifically UserDetailsService)
        // interface assumes that it's always possible to load user authorities early in the authentication process which is
        // not true in our case -> we need to resolve tenant first and to resolve tenant the user has to be authenticated in
        // security context. Therefore, we do not rely on User.getAuthorities, but rather load and keep authorities externally.

        return setOf() // returns immutable empty set
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getUsername(): String {
        return email
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun getPassword(): String? {
        // we will never present user's real password here
        // if you need to operate with user's password, do it in database function/operation only
        return null
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }
}