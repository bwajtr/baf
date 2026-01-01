package com.wajtr.baf.ui.views.user.settings.parts

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.Autocomplete
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.validator.StringLengthValidator
import com.vaadin.flow.data.value.ValueChangeMode
import com.wajtr.baf.authentication.db.PasswordVerificationResult
import com.wajtr.baf.authentication.db.PasswordVerificationService
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.ui.vaadin.extensions.bindMutableProperty
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.ui.vaadin.extensions.showSuccessNotification
import com.wajtr.baf.user.Identity
import com.wajtr.baf.user.UserRepository
import com.wajtr.baf.user.validation.ValidPasswordConstants
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

data class PasswordChangeFormData(
    var oldPassword: String = "",
    var newPassword: String = "",
    var confirmPassword: String = ""
)

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class PasswordChangeComponent(
    private val identity: Identity,
    private val userRepository: UserRepository,
    private val passwordVerificationService: PasswordVerificationService,
) : VerticalLayout() {

    private val binder = Binder<PasswordChangeFormData>()
    private lateinit var oldPasswordField: PasswordField
    private lateinit var newPasswordField: PasswordField
    private lateinit var confirmPasswordField: PasswordField
    private val changePasswordButton: Button

    init {
        isPadding = false
        maxWidth = "350px"

        h2(i18n("users.password.change.header"))

        formLayout {
            // Old password field
            oldPasswordField = passwordField(i18n("users.password.change.password.old")) {
                autocomplete = Autocomplete.NEW_PASSWORD
                valueChangeMode = ValueChangeMode.LAZY
                width = "100%"
            }

            // New password field
            newPasswordField = passwordField(i18n("users.password.change.password.new")) {
                autocomplete = Autocomplete.NEW_PASSWORD
                valueChangeMode = ValueChangeMode.LAZY
                width = "100%"
            }

            // Confirm password field
            confirmPasswordField = passwordField(i18n("users.password.change.password.confirmation")) {
                valueChangeMode = ValueChangeMode.LAZY
                width = "100%"
            }
        }

        changePasswordButton = button(i18n("users.password.change.button")) {
            addThemeVariants(ButtonVariant.AURA_PRIMARY)
            isEnabled = false
            onClick {
                changePassword()
            }
        }

        bindModel()
    }

    private fun bindModel() {
        // Bind old password
        binder.forField(oldPasswordField)
            .asRequired(i18n("users.password.change.password.oldrequired"))
            .bindMutableProperty(PasswordChangeFormData::oldPassword)

        // Bind new password
        binder.forField(newPasswordField)
            .asRequired(i18n("users.password.change.password.newrequired"))
            .withValidator(
                StringLengthValidator(
                    i18n("users.password.change.password.minsize", ValidPasswordConstants.MIN_LENGTH),
                    ValidPasswordConstants.MIN_LENGTH,
                    null
                )
            )
            .bindMutableProperty(PasswordChangeFormData::newPassword)

        // Bind confirm password with a custom validator for matching
        binder.forField(confirmPasswordField)
            .asRequired(i18n("users.password.change.password.confirmationrequired"))
            .withValidator(
                StringLengthValidator(
                    i18n("users.password.change.password.minsize", ValidPasswordConstants.MIN_LENGTH),
                    ValidPasswordConstants.MIN_LENGTH,
                    null
                )
            )
            .withValidator({ confirmPassword ->
                confirmPassword == newPasswordField.value
            }, i18n("users.password.change.password.dontmatch"))
            .bindMutableProperty(PasswordChangeFormData::confirmPassword)

        // Trigger validation when new password changes
        newPasswordField.addValueChangeListener {
            if (confirmPasswordField.value.isNotEmpty()) {
                binder.validate()
            }
        }

        binder.readBean(PasswordChangeFormData())

        // Enable button only when form is valid
        binder.addStatusChangeListener { event ->
            changePasswordButton.isEnabled = event.binder.isValid
        }
    }

    private fun changePassword() {
        val formData = PasswordChangeFormData()
        
        if (!binder.writeBeanIfValid(formData)) {
            return
        }

        // Validate passwords match
        if (formData.newPassword != formData.confirmPassword) {
            showErrorNotification(i18n("users.password.change.password.dontmatch"))
            return
        }

        val user = identity.authenticatedUser

        // Verify old password
        val verificationResult = passwordVerificationService.verifyPassword(user.email, formData.oldPassword)
        if (verificationResult != PasswordVerificationResult.OK) {
            showErrorNotification(i18n("users.password.change.outcome.wrongpassword"))
            return
        }

        try {
            // Update password in the database
            userRepository.updateUserPassword(user.id, formData.newPassword)
            showSuccessNotification(i18n("users.password.change.outcome.success"))
            
            // Clear form
            binder.readBean(PasswordChangeFormData())
            changePasswordButton.isEnabled = false
        } catch (_: Exception) {
            showErrorNotification(i18n("users.password.change.outcome.internalerror"))
        }
    }

}