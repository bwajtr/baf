package com.wajtr.baf.ui.commons.l12n

import com.github.mvysny.kaributools.timeZone
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinSession
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Extension of the Instant class to convert it to the timezone of currently logged in user and formatting it to the string
 * of the user locale. This extension should be used when displaying Instant data to the user in UI. Remember, that server
 * side (and database too) operate exclusively at GMT timezone, so all Instants read and written to database represent GMT time.
 *
 * Note: Instants should be used for DateTime operations on the serverside almost exclusively for
 * events which happened at some exact point in time (created time, updated time, logged in time etc.)
 */
fun Instant.toLocalizedString(dateTimeStyle: FormatStyle): String {
    return this.toLocalizedString(DateTimeFormatter.ofLocalizedDateTime(dateTimeStyle))
}

fun Instant.toLocalizedString(dateStyle: FormatStyle, timeStyle: FormatStyle): String {
    return this.toLocalizedString(DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle))
}

fun Instant.toLocalizedString(format: DateTimeFormatter): String {
    return this.atZone(UI.getCurrent().page.extendedClientDetails.timeZone)
        .format(format.withLocale(VaadinSession.getCurrent().locale))
}