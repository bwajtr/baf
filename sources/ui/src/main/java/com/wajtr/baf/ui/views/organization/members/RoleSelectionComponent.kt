package com.wajtr.baf.ui.views.organization.members

import com.github.mvysny.karibudsl.v10.checkBox
import com.github.mvysny.karibudsl.v10.radioButtonGroup
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.wajtr.baf.core.i18n.i18n
import com.wajtr.baf.organization.member.UserRole

class RoleSelectionComponent(
    showAdditionalRights: Boolean = true
) : VerticalLayout() {

    val organizationRoleGroup: RadioButtonGroup<String>
    lateinit var billingManagerCheckbox: Checkbox

    init {
        isPadding = false

        // Organization Role Section
        val roleSection = VerticalLayout().apply {
            isPadding = false
            style.set("margin-bottom", "2rem")

            add(H2(i18n("member.settings.role.section")))

            organizationRoleGroup = radioButtonGroup {
                setItems(UserRole.USER_ROLE, UserRole.ADMIN_ROLE, UserRole.OWNER_ROLE)
                setRenderer(ComponentRenderer { role ->
                    createRoleOption(role, i18n("member.settings.role.description.$role"))
                })
            }

            add(organizationRoleGroup)
        }
        add(roleSection)

        // Additional Rights Section (optional)
        if (showAdditionalRights) {
            val additionalSection = VerticalLayout().apply {
                isPadding = false
                style.set("margin-bottom", "2rem")

                add(H2(i18n("member.settings.additional.section")))

                billingManagerCheckbox = checkBox {
                    setLabelComponent(
                        createRoleOption(
                            UserRole.BILLING_MANAGER_ROLE,
                            i18n("member.settings.additional.description.${UserRole.BILLING_MANAGER_ROLE}")
                        )
                    )
                }

                add(billingManagerCheckbox)
            }
            add(additionalSection)
        }
    }

    private fun createRoleOption(roleName: String, description: String): Div {
        return Div().apply {
            val nameSpan = Span(i18n("role.$roleName"))
            nameSpan.style.set("font-weight", "bold")
            nameSpan.style.set("display", "block")

            val descSpan = Span(description)
            descSpan.style.set("font-size", "0.875rem")
            descSpan.style.set("color", "var(--vaadin-text-color-secondary)")
            descSpan.style.set("display", "block")
            descSpan.style.set("margin-top", "0.25rem")

            add(nameSpan, descSpan)
        }
    }
}
