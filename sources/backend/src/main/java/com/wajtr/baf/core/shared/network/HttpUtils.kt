package com.wajtr.baf.core.shared.network

import com.wajtr.baf.core.shared.CoreContext.currentRequest
import com.wajtr.baf.core.shared.CoreContext.currentResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest

/**
 * @author Bretislav Wajtr
 */
object HttpUtils {
    /**
     * Based on http://www.mkyong.com/java/how-to-get-client-ip-address-in-java/
     *
     * @param request Request to extract IP from
     * @return The IP address of the client
     */
    fun getClientIp(request: HttpServletRequest?): String? {
        var remoteAddress: String? = ""

        if (request != null) {
            remoteAddress = request.getHeader("X-FORWARDED-FOR")
            if (remoteAddress == null || "" == remoteAddress) {
                remoteAddress = request.getRemoteAddr()
            }
        }

        return remoteAddress
    }

    fun getServerBaseUrl(request: HttpServletRequest): String {
        val format: String?
        if ((request.getScheme() == "http" && request.getServerPort() == 80)
            || (request.getScheme() == "https" && request.getServerPort() == 443)
        ) {
            format = "%s://%s"
        } else {
            format = "%s://%s:%d"
        }

        return String.format(format, request.getScheme(), request.getServerName(), request.getServerPort())
    }

    fun getCookieValue(cookieName: String?): String? {
        val cookies = currentRequest.getCookies()
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.getName() == cookieName) {
                    return cookie.getValue()
                }
            }
        }

        return null
    }

    fun setCookie(cookieName: String?, cookieValue: String?) {
        val cookie = Cookie(cookieName, cookieValue)
        cookie.setPath(currentRequest.getContextPath())
        cookie.setSecure(true)
        cookie.setMaxAge(Int.Companion.MAX_VALUE)
        currentResponse!!.addCookie(cookie)
    }
}
