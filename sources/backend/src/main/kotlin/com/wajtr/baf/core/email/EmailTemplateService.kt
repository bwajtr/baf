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
 * Templates are located in:
 * - HTML: templates/email/{template-type}/template{_locale}.ftl
 * - Plain text: templates/email/{template-type}/template{_locale}.txt.ftl
 * 
 * Example paths:
 * - templates/email/email-verification/template.ftl (English HTML)
 * - templates/email/email-verification/template.txt.ftl (English plain text)
 * - templates/email/email-verification/template_cs.ftl (Czech HTML)
 * - templates/email/email-verification/template_cs.txt.ftl (Czech plain text)
 *
 * @author Bretislav Wajtr
 */
@Service
class EmailTemplateService(
    private val freemarkerConfig: Configuration,
    private val companyProperties: CompanyProperties
) {
    
    private val log = LoggerFactory.getLogger(EmailTemplateService::class.java)
    
    /**
     * Processes a FreeMarker HTML template with the given model and locale.
     * 
     * @param templateName The template type name (e.g., "email-verification", "password-reset")
     * @param model The data model to pass to the template
     * @param locale The locale to use for template selection (falls back to English if not available)
     * @return The processed HTML content
     */
    fun processTemplate(templateName: String, model: Map<String, Any>, locale: Locale?): String {
        val effectiveLocale = locale ?: LocaleContextHolder.getLocale()
        val templatePath = resolveTemplatePath(templateName, effectiveLocale, false)
        
        // Add common model properties
        val enrichedModel = model.toMutableMap()
        enrichedModel["appName"] = i18n("application.title")
        enrichedModel["companyName"] = companyProperties.name
        enrichedModel["companyAddress"] = companyProperties.getFormattedAddress()
        
        val template = freemarkerConfig.getTemplate(templatePath)
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, enrichedModel)
    }
    
    /**
     * Processes a FreeMarker plain text template with the given model and locale.
     * 
     * @param templateName The template type name (e.g., "email-verification", "password-reset")
     * @param model The data model to pass to the template
     * @param locale The locale to use for template selection (falls back to English if not available)
     * @return The processed plain text content
     */
    fun processPlainTextTemplate(templateName: String, model: Map<String, Any>, locale: Locale?): String {
        val effectiveLocale = locale ?: LocaleContextHolder.getLocale()
        val templatePath = resolveTemplatePath(templateName, effectiveLocale, true)
        
        // Add common model properties
        val enrichedModel = model.toMutableMap()
        enrichedModel["appName"] = i18n("application.title")
        enrichedModel["companyName"] = companyProperties.name
        enrichedModel["companyAddress"] = companyProperties.getFormattedAddress()
        
        val template = freemarkerConfig.getTemplate(templatePath)
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, enrichedModel)
    }
    
    /**
     * Resolves the template path based on locale and format (HTML or plain text).
     * First tries the locale-specific template, then falls back to the default (English) template.
     * 
     * @param templateName The template type name
     * @param locale The locale
     * @param isPlainText Whether to resolve plain text template (.txt.ftl) or HTML template (.ftl)
     */
    private fun resolveTemplatePath(templateName: String, locale: Locale, isPlainText: Boolean): String {
        val suffix = if (isPlainText) ".txt.ftl" else ".ftl"
        
        // Try locale-specific template first: email/{templateName}/template_{lang}{suffix}
        val localePath = "email/$templateName/template_${locale.language}$suffix"
        // Fallback to default: email/{templateName}/template{suffix}
        val defaultPath = "email/$templateName/template$suffix"
        
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
