package com.wajtr.baf.ui.views.user.settings.parts

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.value.ValueChangeMode
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.vaadin.extensions.bindMutableProperty
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.ui.views.user.settings.USER_SETTINGS_PAGE
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.User
import com.wajtr.baf.user.UserRepository
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class UserAccountComponent(
    private val identity: Identity,
    private val userRepository: UserRepository
) : VerticalLayout() {

    private val binder = Binder<User>()
    private lateinit var nameField: TextField
    private val saveButton: Button

    init {
        isPadding = false
        maxWidth = "350px"

        val user = identity.authenticatedUser

        h2(i18n("user.settings.account.header"))

        formLayout {
            // Email field (readonly)
            textField(i18n("user.settings.account.email")) {
                value = user.email
                isReadOnly = true
                width = "100%"
            }

            // Name field (editable)
            nameField = textField(i18n("user.settings.account.name")) {
                value = user.name
                valueChangeMode = ValueChangeMode.LAZY
                width = "100%"
            }

            // Roles field (readonly)
            textField(i18n("user.settings.account.roles")) {
                value = identity.grantedRoles.joinToString(", ") { i18n("role.$it") }
                isReadOnly = true
                width = "100%"
            }
        }

        saveButton = button(i18n("user.settings.account.save")) {
            addThemeVariants(ButtonVariant.AURA_PRIMARY)
            isEnabled = false
            onClick {
                saveChanges()
            }
        }

        bindModel(user)
    }

    private fun bindModel(user: User) {
        binder.forField(nameField)
            .asRequired(i18n("user.settings.account.name.required"))
            .withConverter(
                { it.trim() },
                { it }
            )
            .bindMutableProperty(User::name)

        binder.readBean(user)

        // Enable save button only when form is valid and changed
        binder.addStatusChangeListener { event ->
            saveButton.isEnabled = event.binder.isValid && event.binder.hasChanges()
        }
    }

    private fun saveChanges() {
        val user = identity.authenticatedUser

        if (binder.writeBeanIfValid(user)) {
            val success = userRepository.updateUserName(user.id, user.name)

            if (success) {
                UI.getCurrent()
                    .page.setLocation(USER_SETTINGS_PAGE) // reload UI to show changes in user name immediately
            } else {
                showErrorNotification(i18n("user.settings.account.update.failure"))
            }
        }
    }

}