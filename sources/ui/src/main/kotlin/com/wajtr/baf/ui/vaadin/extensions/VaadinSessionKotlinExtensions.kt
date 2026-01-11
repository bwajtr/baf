package com.wajtr.baf.ui.vaadin.extensions


import com.vaadin.flow.server.VaadinSession
import java.time.ZoneId

/**
 * Kotlin extensions of the VaadinSession object
 *
 * @author Bretislav Wajtr
 */


/**
 * Timezone of this Vaadin session
 *
 * @author Bretislav Wajtr
 */
fun VaadinSession.setTimezone(zoneId: ZoneId) {
    setAttribute("user.vaadin.session.timezone", zoneId)
}

/**
 * Timezone of this Vaadin session. If timezone is null then please ensure that the TimeZone had a chance to be initialized.
 * See Vaadin UI extension here: VaadinUIKotlinExtensions.kt -> ensureSessionTimeZoneIsSet
 * See also usages of ensureSessionTimeZoneIsSet in the "users" module, where we had to use it on several places.
 *
 * @author Bretislav Wajtr
 */
fun VaadinSession.getTimezone(): ZoneId {
    return getAttribute("user.vaadin.session.timezone") as ZoneId // read method comment is this is null!
}
