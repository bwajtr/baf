package com.wajtr.baf.core.email

import com.wajtr.baf.core.i18n.i18n
import freemarker.template.Configuration
import freemarker.template.TemplateNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils
import java.util.*

/**
 * Service for processing FreeMarker email templates with locale support.
 * 
 * Templates are located in: templates/email/{template-type}/template{_locale}.ftl
 * 
 * Example paths:
 * - templates/email/email-verification/template.ftl (English default)
 * - templates/email/email-verification/template_cs.ftl (Czech)
 * - templates/email/email-verification/template_de.ftl (German)
 *
 * @author Bretislav Wajtr
 */
@Service
class EmailTemplateService(
    private val freemarkerConfig: Configuration
) {
    
    private val log = LoggerFactory.getLogger(EmailTemplateService::class.java)
    
    /**
     * Processes a FreeMarker template with the given model and locale.
     * 
     * @param templateName The template type name (e.g., "email-verification", "password-reset")
     * @param model The data model to pass to the template
     * @param locale The locale to use for template selection (falls back to English if not available)
     * @return The processed HTML content
     */
    fun processTemplate(templateName: String, model: Map<String, Any>, locale: Locale?): String {
        val effectiveLocale = locale ?: LocaleContextHolder.getLocale() ?: Locale.ENGLISH
        val templatePath = resolveTemplatePath(templateName, effectiveLocale)
        
        // Add common model properties
        val enrichedModel = model.toMutableMap()
        enrichedModel["appName"] = i18n("application.title")
        
        val template = freemarkerConfig.getTemplate(templatePath)
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, enrichedModel)
    }
    
    /**
     * Resolves the template path based on locale.
     * First tries the locale-specific template, then falls back to the default (English) template.
     */
    private fun resolveTemplatePath(templateName: String, locale: Locale): String {
        // Try locale-specific template first: email/{templateName}/template_{lang}.ftl
        val localePath = "email/$templateName/template_${locale.language}.ftl"
        // Fallback to default: email/{templateName}/template.ftl
        val defaultPath = "email/$templateName/template.ftl"
        
        return try {
            freemarkerConfig.getTemplate(localePath)
            log.debug("Using locale-specific template: $localePath")
            localePath
        } catch (_: TemplateNotFoundException) {
            log.debug("Locale-specific template not found for locale '${locale.language}', falling back to default: $defaultPath")
            defaultPath
        }
    }
}
