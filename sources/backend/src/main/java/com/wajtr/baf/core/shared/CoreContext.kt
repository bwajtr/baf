package com.wajtr.baf.core.shared

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.Uuid.Companion.parse

/**
 * @author Bretislav Wajtr
 */
object CoreContext {

    val session: HttpSession?
        get() {
            return currentRequest.getSession(true)
        }

    val currentRequest: HttpServletRequest
        get() {
            val attr = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
            return attr.request
        }

    val currentResponse: HttpServletResponse?
        get() {
            val attr = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
            return attr.response
        }

    val currentTenantId: UUID?
        get() {
//            Authentication authentication = getCurrentAuthentication();
//            if (authentication instanceof TenantBoundAuthentication) {
//                return ((TenantBoundAuthentication) authentication).getTenantId();
//            }
            return UUID.fromString("2dcab49d-8807-4888-bb69-2afd663e2743")
        }

//    fun getCurrentAuthentication(): Authentication {
//        return SecurityContextHolder.getContext().getAuthentication()
//    }
//
//
//    fun setCurrentAuthentication(auth: Authentication) {
//        SecurityContextHolder.getContext().setAuthentication(auth)
//    }
}
