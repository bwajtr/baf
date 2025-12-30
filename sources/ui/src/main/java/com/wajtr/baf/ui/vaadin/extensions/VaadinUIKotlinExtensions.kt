package com.wajtr.baf.ui.vaadin.extensions

import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinSession
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.time.ZoneId
import java.util.*

/**
 * This method solves a problem that one extra round-trip to client is needed to obtain user's TimeZone from the browser.
 * TimeZoneId in VaadinSession can be therefore ensured only in the block which you pass into this method.
 *
 * See example:
 * ```
 *     // VaadinSession.getCurrent().getTimezone() might or might not be null here
 *     UI.getCurrent().ensureSessionTimeZoneIsSet {
 *         // VaadinSession.getCurrent().getTimezone() is definitely not null here
 *     }
 * ```
 * Based on a problem and solution which is described here:
 * [Client time zone rendering in Vaadin](https://stackoverflow.com/questions/67724284/in-vaadin-framework-how-can-i-get-the-client-time-zone-to-use-in-rendering-the)
 */
fun UI.ensureSessionTimeZoneIsSet(block: (() -> Unit)? = null) {
    val it = this.page.extendedClientDetails
    if (it != null && it.timeZoneId != null) {
        setSessionTimeZone(ZoneId.of(it.timeZoneId))
    } else {
        // fallback to System timezone resolving
        setSessionTimeZone(LocaleContextHolder.getTimeZone().toZoneId())
    }
    block?.invoke()
}

private fun setSessionTimeZone(timeZoneId: ZoneId) {
    VaadinSession.getCurrent().setTimezone(timeZoneId)

    // See SessionLocaleResolver, which is used by the rest of the spring application, not only Vaadin
    VaadinRequest.getCurrent().wrappedSession.setAttribute(
        SessionLocaleResolver.TIME_ZONE_SESSION_ATTRIBUTE_NAME,
        TimeZone.getTimeZone(timeZoneId)
    )
}
