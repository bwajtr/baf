package com.wajtr.baf.user.registration

import com.wajtr.baf.core.tenants.Tenant
import com.wajtr.baf.core.tenants.TenantRepository
import com.wajtr.baf.db.jooq.routines.EncryptPassword
import com.wajtr.baf.db.jooq.tables.AppUser
import com.wajtr.baf.user.AccountStatusCheckResult
import com.wajtr.baf.user.UserRepository
import com.wajtr.baf.user.UserRole.OWNER_ROLE
import com.wajtr.baf.user.UserRole.USER_ROLE
import com.wajtr.baf.user.UserRoleService
import com.wajtr.baf.user.UserRoleTenant
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.InetAddress
import java.time.ZoneId
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

data class RegisterUserBasedOnInvitationRequest(
    val name: String,
    val password: String,
    val ipAddress: InetAddress,
    val preferredLocale: Locale,
    val preferredTimezoneId: ZoneId,
    val invitationId: UUID,
    val emailVerificationToken: UUID
)

/**
 * Business service dealing with user registration process
 *
 * @author Bretislav Wajtr
 */
interface UserRegistrationService {

    /**
     * Registers new user, creates new tenant for him and assign full administrator access to such tenant. This method
     * is used on the public registration form, allowing public to create completely new accounts in the application.
     *
     * This method can be called without logged-in user.
     */
    fun registerUserOfNewTenant(request: UserAndTenantRegistrationRequest): UserRegistrationResult


    /**
     * Registers new user for existing tenant based on invitation
     *
     * This method can be called without logged-in user.
     */
    fun registerUserBasedOnInvitation(request: RegisterUserBasedOnInvitationRequest): UserRegistrationResult

    /**
     * Returns collection of role IDs (see BasicRoles class or content of table users.role) which should be initially
     * assigned to the user who created new tenant (i.e. by registering completely
     * new account) during registration. It makes sense to grant such roles to the user, which would give him access to every
     * part of the application (full access).
     */
    fun initialTenantOwnerRolesCollection(): Collection<String>

    /**
     * Returns collection of role IDs (see BasicRoles class or content of table users.role) which should be initially
     * assigned to the user was invited to join an existing tenant during registration. It makes sense to grant such roles to the user, which would
     * grant the user "basic user" access to the application (so no administration areas)
     */
    fun initialInvitedUserRolesCollection(): Collection<String>
}


/**
 * Default implementation of the service. Can be overriden by the application to supply custom registration process
 */
@Service
@Transactional
class UserRegistrationServiceImpl(
    private val userRoleService: UserRoleService,
    private val create: DSLContext,
    private val userRepository: UserRepository,
    private val tenantRepository: TenantRepository
) : UserRegistrationService {

    override fun registerUserBasedOnInvitation(request: RegisterUserBasedOnInvitationRequest): UserRegistrationResult {
//        val result =
//            userRegistrationDAO.registerUserBasedOnInvitation(
//                request.name,
//                request.password,
//                request.ipAddress,
//                request.invitationId,
//                request.emailVerificationToken,
//                request.preferredLocale.toLanguageTag(),
//                request.preferredTimezoneId.id
//            )
//
//        if (result is UserRegistrationSuccess) {
//            grantedAuthorityDAO.setUserRoles(result.userId, result.tenantId, initialInvitedUserRolesCollection())
//        }

        return UserRegistrationFailure(UserRegistrationResultStatus.ERROR_INVALID_EMAIL_TOKEN)
    }

    override fun registerUserOfNewTenant(request: UserAndTenantRegistrationRequest): UserRegistrationResult {
        val result = registerUser(
            request.name,
            request.email,
            request.password,
            request.ipAddress,
            request.preferredLocale.toLanguageTag(),
            request.preferredTimezoneId.id
        )

        if (result is UserRegistrationSuccess) {
            val roles = initialTenantOwnerRolesCollection()
            for (role in roles) {
                userRoleService.insertRole(
                    UserRoleTenant(
                        result.userId,
                        role,
                        result.tenantId
                    )
                )
            }
        }

        return result
    }

    fun registerUserBasedOnInvitation(
        name: String?, password: String?, ipAddress: InetAddress,
        invitationId: UUID?, emailVerificationToken: UUID?,
        preferredLocale: String?, preferredTimezoneId: String?
    ): UserRegistrationResult {
        // TODO finish when invitation process is done
//        val result: RegisterAppUserByInvitationRecord? = create.selectFrom(
//            Routines.registerAppUserByInvitation(
//                name,
//                password,
//                ipAddress.getHostAddress(),
//                invitationId,
//                emailVerificationToken,
//                ADMIN_SECURITY_KEY,
//                preferredLocale,
//                preferredTimezoneId
//            )
//        )
//            .fetchOne()
//
//        if (result.getStatus().equals(UserRegistrationResultStatus.OK.name)) {
//            return UserRegistrationSuccess(
//                UserRegistrationResultStatus.valueOf(result.getStatus()),
//                result.getNewUserId(),
//                result.getTenantId()
//            )
//        } else {
        return UserRegistrationFailure(UserRegistrationResultStatus.ERROR_INVITATION_EXISTS)
//        }
    }

    override fun initialTenantOwnerRolesCollection(): Collection<String> {
        return listOf(OWNER_ROLE)
    }

    override fun initialInvitedUserRolesCollection(): Collection<String> {
        return listOf(USER_ROLE)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun registerUser(
        name: String,
        email: String,
        password: String,
        ipAddress: InetAddress,
        preferredLocale: String,
        preferredTimezoneId: String
    ): UserRegistrationResult {
        // Preconditions
        require(name.isNotBlank()) { "name must not be empty" }
        require(email.isNotBlank()) { "email must not be empty" }
        require(password.isNotBlank()) { "password must not be empty" }
        require(password.length > 3) { "password must be at least 4 characters long!" }
        require(ipAddress.hostAddress.isNotBlank()) { "ip_address must not be empty" }
        require(preferredLocale.isNotBlank()) { "p_preferred_locale must not be empty" }
        require(preferredTimezoneId.isNotBlank()) { "p_preferred_timezone_id must not be empty" }


        // Check if user exists
        val status = userRepository.checkAccountStatus(email)
        if (status == AccountStatusCheckResult.OK || status == AccountStatusCheckResult.NOT_VERIFIED) {
            return UserRegistrationFailure(UserRegistrationResultStatus.ERROR_DUPLICATE)
        }
        if (status == AccountStatusCheckResult.INVITED) {
            return UserRegistrationFailure(UserRegistrationResultStatus.ERROR_INVITATION_EXISTS)
        }

        // Generate new user ID
        val userId = Uuid.generateV7().toJavaUuid()

        // Create new tenant for new user
        val tenant = Tenant(null, "", null, null, true)
        tenantRepository.insert(tenant)
        val resolvedTenantId = tenant.id

        // Encrypt password
        val encryptPassword = EncryptPassword()
        encryptPassword.setPassword(password)
        encryptPassword.execute(create.configuration())
        val encryptedPassword = encryptPassword.returnValue
            ?: throw IllegalStateException("Failed to encrypt password")

        // Create the user in database
        create.insertInto(AppUser.APP_USER)
            .set(AppUser.APP_USER.ID, userId)
            .set(AppUser.APP_USER.NAME, name)
            .set(AppUser.APP_USER.EMAIL, email)
            .set(AppUser.APP_USER.PASSWORD, encryptedPassword)
            .set(AppUser.APP_USER.EMAIL_VERIFIED, false)
            .set(AppUser.APP_USER.EMAIL_VERIFICATION_TOKEN, Uuid.generateV7().toJavaUuid())
            .execute()

        // Return success result
        return UserRegistrationSuccess(
            UserRegistrationResultStatus.OK,
            userId,
            resolvedTenantId!!
        )
    }

}


/**
 *
 * @author Bretislav Wajtr
 */

data class UserAndTenantRegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
    val ipAddress: InetAddress,
    val preferredLocale: Locale,
    val preferredTimezoneId: ZoneId
)

sealed interface UserRegistrationResult {
    val status: UserRegistrationResultStatus
}

data class UserRegistrationFailure(override val status: UserRegistrationResultStatus) : UserRegistrationResult

data class UserRegistrationSuccess(
    override val status: UserRegistrationResultStatus,
    val userId: UUID,
    val tenantId: UUID
) : UserRegistrationResult


enum class UserRegistrationResultStatus {
    OK, // User was successfully registered
    ERROR_DUPLICATE,  // User registration failed, user with same email already exists
    ERROR_INVITATION_EXISTS,  // User registration and creation of new tenant failed because there is a pending invitation for this users email. The invitation must be cancelled first.
    ERROR_INVALID_INVITATION_ID, // invitation Id does not match - valid only for invitation based registrations
    ERROR_INVALID_EMAIL_TOKEN, // email verification token does not match given Invitation ID - valid only for invitation based registrations
}