package com.wajtr.baf.core.commons

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * @author Bretislav Wajtr
 */
object HttpServletUtils {

    val session: HttpSession?
        get() {
            return currentRequest?.getSession(true)
        }

    val currentRequest: HttpServletRequest?
        get() {
            val attr = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
            return attr.request
        }

    val currentResponse: HttpServletResponse?
        get() {
            val attr = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
            return attr.response
        }

    /**
     * Based on http://www.mkyong.com/java/how-to-get-client-ip-address-in-java/
     *
     * @param request Request to extract IP from
     * @return The IP address of the client
     */
    fun getClientIp(): String? {
        var remoteAddress: String? = ""

        currentRequest?.let {
            remoteAddress = it.getHeader("X-FORWARDED-FOR")
            if (remoteAddress == null || "" == remoteAddress) {
                remoteAddress = it.remoteAddr
            }
        }

        return remoteAddress
    }

    fun getServerBaseUrl(): String {
        val format: String?
        format = if ((currentRequest?.scheme == "http" && currentRequest?.serverPort == 80)
            || (currentRequest?.scheme == "https" && currentRequest?.serverPort == 443)
        ) {
            "%s://%s"
        } else {
            "%s://%s:%d"
        }

        return String.format(format, currentRequest?.scheme, currentRequest?.serverName, currentRequest?.serverPort)
    }

    fun getCookieValue(cookieName: String?): String? {
        val cookies = currentRequest?.cookies
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == cookieName) {
                    return cookie.value
                }
            }
        }

        return null
    }

    fun setCookie(cookieName: String?, cookieValue: String?) {
        val cookie = Cookie(cookieName, cookieValue)
        cookie.path = currentRequest?.contextPath
        cookie.secure = true
        cookie.maxAge = Int.MAX_VALUE
        currentResponse!!.addCookie(cookie)
    }
}