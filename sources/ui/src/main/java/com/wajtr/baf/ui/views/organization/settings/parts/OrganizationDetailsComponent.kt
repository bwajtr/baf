package com.wajtr.baf.ui.views.organization.settings.parts

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.value.ValueChangeMode
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.core.tenants.Tenant
import com.wajtr.baf.core.tenants.TenantRepository
import com.wajtr.baf.ui.components.CountrySelectComboBox
import com.wajtr.baf.ui.components.countrySelectCombo
import com.wajtr.baf.ui.vaadin.extensions.bindMutableProperty
import com.wajtr.baf.ui.vaadin.extensions.showErrorNotification
import com.wajtr.baf.ui.vaadin.extensions.showSuccessNotification
import com.wajtr.baf.ui.views.organization.settings.ORGANIZATION_SETTINGS_PAGE
import com.wajtr.baf.user.Identity
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class OrganizationDetailsComponent(
    private val identity: Identity,
    private val tenantRepository: TenantRepository
) : VerticalLayout() {

    private val binder = Binder<Tenant>()
    private lateinit var nameField: TextField
    private lateinit var addressField: TextArea
    private lateinit var countryField: CountrySelectComboBox
    private val saveButton: Button

    init {
        isPadding = false
        maxWidth = "350px"

        h2(i18n("organization.details.header"))

        formLayout {
            // Organization name field (mandatory)
            nameField = textField(i18n("organization.details.name")) {
                valueChangeMode = ValueChangeMode.LAZY
                width = "100%"
            }

            // Address field (optional)
            addressField = textArea(i18n("organization.details.address")) {
                valueChangeMode = ValueChangeMode.LAZY
                width = "100%"
                setHeight("80px")
            }

            // Country field (optional)
            countryField = countrySelectCombo(i18n("organization.details.country")) {
                width = "100%"
            }
        }

        saveButton = button(i18n("organization.details.save")) {
            addThemeVariants(ButtonVariant.AURA_PRIMARY)
            isEnabled = false
            onClick {
                saveChanges()
            }
        }

        bindModel()
        loadTenant()
    }

    private fun loadTenant() {
        val currentTenantId = identity.authenticatedTenant?.id
            ?: throw IllegalStateException("No authenticated tenant found")

        val tenant = tenantRepository.findById(currentTenantId)
            ?: throw IllegalStateException("Tenant not found")

        binder.readBean(tenant)
    }

    private fun bindModel() {
        // Bind organization name (required)
        binder.forField(nameField)
            .asRequired(i18n("organization.details.name.required"))
            .withConverter(
                { it.trim() },
                { it }
            )
            .bindMutableProperty(Tenant::organizationName)

        // Bind address (optional)
        binder.forField(addressField)
            .withConverter(
                { if (it.isBlank()) null else it.trim() },
                { it ?: "" }
            )
            .bindMutableProperty(Tenant::organizationAddress)

        // Bind country code (optional)
        binder.forField(countryField)
            .bindMutableProperty(Tenant::organizationCountryCode)

        // Enable save button only when form is valid and changed
        binder.addStatusChangeListener { event ->
            saveButton.isEnabled = event.binder.isValid && event.binder.hasChanges()
        }
    }

    private fun saveChanges() {
        val currentTenantId = identity.authenticatedTenant?.id
            ?: throw IllegalStateException("No authenticated tenant found")

        val tenant = tenantRepository.findById(currentTenantId)
            ?: throw IllegalStateException("Tenant not found")

        val oldName = tenant.organizationName
        if (binder.writeBeanIfValid(tenant)) {

            val success = tenantRepository.updateOrganizationDetails(
                tenantId = currentTenantId,
                organizationName = tenant.organizationName,
                organizationAddress = tenant.organizationAddress,
                organizationCountryCode = tenant.organizationCountryCode
            )

            if (success) {
                showSuccessNotification(i18n("organization.details.update.success"))
                // Reload page to reflect changes in the rest of UI
                if (oldName != tenant.organizationName) {
                    UI.getCurrent().page.setLocation(ORGANIZATION_SETTINGS_PAGE)
                }
            } else {
                showErrorNotification(i18n("organization.details.update.failure"))
            }
        }
    }
}
