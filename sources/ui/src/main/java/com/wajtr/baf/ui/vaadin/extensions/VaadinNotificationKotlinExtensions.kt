package com.wajtr.baf.ui.vaadin.extensions

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.wajtr.baf.core.i18n.i18n

/**
 * Various styles of notifications
 *
 * @author Bretislav Wajtr
 */

fun showNotification(message: String, duration: Int = 5000) {
    Notification.show(message, duration, Notification.Position.TOP_CENTER)
}

/**
 * Use when some operation succeeded
 */
fun showSuccessNotification(message: String) {
    Notification.show(message, 5000, Notification.Position.TOP_CENTER)
}

/**
 * Use when you want the message to really stand out and you want the increased probability that the user will notice it
 */
fun showContrastNotification(message: String, duration: Int = 7000) {
    val notification = Notification(message, duration, Notification.Position.TOP_CENTER)
    notification.addThemeName("contrast")
    notification.open()
}

fun showErrorNotification(message: String) {
    val errorDialog = Dialog().apply {
        verticalLayout {
            isPadding = false
            maxWidth = "450px"

            h4(i18n("common.error")) {
                element.style.set("color", "var(--lumo-error-color)")
            }

            p {
                html(message.replace("\n", "<br>"))
                element.style.set("color", "var(--lumo-error-color)")
            }

            button(i18n("common.close")) {
                addThemeVariants(ButtonVariant.LUMO_ERROR)
                onClick {
                    this@apply.close()
                }
                alignSelf = FlexComponent.Alignment.CENTER
            }
        }
    }
    errorDialog.open()
}


