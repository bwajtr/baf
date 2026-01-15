package com.wajtr.baf.core.email.localpreview

import com.wajtr.baf.core.email.EmailSender
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Email sender implementation that saves emails to the filesystem.
 *
 * This is the default implementation when mailgun.enabled=false (default).
 * Useful for development and testing - allows developers to inspect sent emails
 * by opening the generated HTML files in a browser.
 *
 * Emails are saved to the "local_email_previews" directory in the working directory.
 * Each email is saved as an HTML file with a timestamped filename.
 *
 * @author Bretislav Wajtr
 */
@Service
@ConditionalOnProperty(name = ["mailgun.enabled"], havingValue = "false", matchIfMissing = true)
class LocalFilePreviewEmailSender : EmailSender {

    private val log = LoggerFactory.getLogger(LocalFilePreviewEmailSender::class.java)

    private val outputDirectory: Path = Paths.get("local_email_previews")

    override fun sendEmail(to: String, subject: String, htmlContent: String): Boolean {
        return try {
            // Ensure the output directory exists
            if (!Files.exists(outputDirectory)) {
                Files.createDirectories(outputDirectory)
                log.info("Created email output directory: ${outputDirectory.toAbsolutePath()}")
            }

            // Generate a unique filename based on timestamp and recipient
            val sanitizedTo = to.replace(Regex("[^a-zA-Z0-9@._-]"), "_")
            val filename = "${sanitizedTo}.html"
            val filePath = outputDirectory.resolve(filename)

            // Wrap the content with metadata header for easier inspection
            val fullContent = buildEmailFile(to, subject, htmlContent)

            // Write the file
            Files.writeString(filePath, fullContent)

            log.info("Email saved - Subject: '$subject', File: ${filePath.toAbsolutePath()}")
            true
        } catch (e: Exception) {
            log.error("Failed to save email to filesystem for recipient $to with subject '$subject'", e)
            false
        }
    }

    /**
     * Builds the complete HTML file content with metadata header.
     */
    private fun buildEmailFile(to: String, subject: String, htmlContent: String): String {
        // If the content already has an HTML structure, inject metadata at the top
        return if (htmlContent.contains("<html", ignoreCase = true)) {
            // Insert metadata comment after <html> tag or at the very beginning
            val metadataComment = """
                |<!--
                |  EMAIL METADATA
                |  ==============
                |  To: $to
                |  Subject: $subject
                |  Sent at: ${Instant.now()}
                |-->
                |""".trimMargin()

            val htmlTagIndex = htmlContent.indexOf("<html", ignoreCase = true)
            if (htmlTagIndex >= 0) {
                val insertPoint = htmlContent.indexOf(">", htmlTagIndex) + 1
                htmlContent.substring(0, insertPoint) + "\n" + metadataComment + htmlContent.substring(insertPoint)
            } else {
                metadataComment + "\n" + htmlContent
            }
        } else {
            // Wrap plain content in a basic HTML structure
            """
            |<!DOCTYPE html>
            |<html>
            |<head>
            |    <meta charset="UTF-8">
            |    <title>$subject</title>
            |</head>
            |<!--
            |  EMAIL METADATA
            |  ==============
            |  To: $to
            |  Subject: $subject
            |  Sent at: ${Instant.now()}
            |-->
            |<body>
            |$htmlContent
            |</body>
            |</html>
            """.trimMargin()
        }
    }

    /**
     * Returns the path to the output directory.
     * Useful for tests that need to read the saved emails.
     */
    fun getOutputDirectory(): Path = outputDirectory

    /**
     * Clears all saved emails from the output directory.
     * Useful for test cleanup.
     */
    fun clearSentEmails() {
        if (Files.exists(outputDirectory)) {
            Files.list(outputDirectory).use { files ->
                files.filter { it.toString().endsWith(".html") }
                    .forEach { Files.deleteIfExists(it) }
            }
        }
    }

    /**
     * Returns the most recently saved email file, if any.
     */
    fun getLastSentEmailFile(): Path? {
        if (!Files.exists(outputDirectory)) return null

        return Files.list(outputDirectory).use { files ->
            files.filter { it.toString().endsWith(".html") }
                .max(Comparator.comparing { Files.getLastModifiedTime(it) })
                .orElse(null)
        }
    }
}