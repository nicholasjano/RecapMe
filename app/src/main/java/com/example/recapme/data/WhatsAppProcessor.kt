package com.example.recapme.data

import android.content.Context
import android.net.Uri
import com.example.recapme.data.models.ChatMessage
import com.example.recapme.data.models.AppSettings
import com.example.recapme.data.models.TimeWindow
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipInputStream

class WhatsAppProcessor {
    private fun getDateFormats() = listOf(
        // Standard formats with brackets
        SimpleDateFormat("[yyyy-MM-dd, h:mm:ss a] ", Locale.getDefault()),
        SimpleDateFormat("[dd/MM/yy, HH:mm:ss] ", Locale.getDefault()),
        SimpleDateFormat("[M/d/yy, h:mm:ss a] ", Locale.getDefault()),
        SimpleDateFormat("[dd.MM.yy, HH:mm:ss] ", Locale.getDefault()),
        SimpleDateFormat("[dd/MM/yyyy, HH:mm:ss] ", Locale.getDefault()),
        SimpleDateFormat("[M/d/yyyy, h:mm:ss a] ", Locale.getDefault()),
        SimpleDateFormat("[dd.MM.yyyy, HH:mm:ss] ", Locale.getDefault()),

        // Without AM/PM
        SimpleDateFormat("[dd/MM/yy, HH:mm] ", Locale.getDefault()),
        SimpleDateFormat("[M/d/yy, HH:mm] ", Locale.getDefault()),
        SimpleDateFormat("[dd.MM.yy, HH:mm] ", Locale.getDefault()),
        SimpleDateFormat("[dd/MM/yyyy, HH:mm] ", Locale.getDefault()),
        SimpleDateFormat("[M/d/yyyy, HH:mm] ", Locale.getDefault()),
        SimpleDateFormat("[dd.MM.yyyy, HH:mm] ", Locale.getDefault()),

        // Alternative bracket styles
        SimpleDateFormat("\\[yyyy-MM-dd, h:mm:ss a\\] ", Locale.getDefault()),
        SimpleDateFormat("\\[dd/MM/yy, HH:mm:ss\\] ", Locale.getDefault()),

        // Some regions use different separators
        SimpleDateFormat("[dd-MM-yy, HH:mm:ss] ", Locale.getDefault()),
        SimpleDateFormat("[dd-MM-yyyy, HH:mm:ss] ", Locale.getDefault()),
        SimpleDateFormat("[yyyy-MM-dd, HH:mm:ss] ", Locale.getDefault())
    )

    companion object {
        private const val MAX_FILE_SIZE_MB = 20
        private const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024L
    }

    fun processWhatsAppFile(context: Context, uri: Uri, settings: AppSettings): Result<String> {
        return try {
            // Validate file input
            validateFileInput(context, uri)?. let { error ->
                return Result.failure(error)
            }

            val messages = extractMessagesFromZip(context, uri)
            if (messages.isEmpty()) {
                return Result.failure(Exception("No valid chat messages found in the uploaded file"))
            }

            val filteredMessages = filterByTimeWindow(messages, settings.recapTimeWindow)
            val singleLineContent = messagesToSingleLine(filteredMessages)

            if (singleLineContent.isBlank()) {
                return Result.failure(Exception("No messages found in the selected time window"))
            }

            Result.success(singleLineContent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validateFileInput(context: Context, uri: Uri): Exception? {
        return try {

            // Check if this is a Google Drive URI
            val isGoogleDrive = uri.authority?.contains("com.google.android.apps.docs.storage") == true ||
                    uri.authority?.contains("drive.google.com") == true

            if (isGoogleDrive) {
                return null // Skip further validation as we'll handle this during processing
            }

            // First check if we have the necessary permissions for this URI
            try {
                context.contentResolver.persistedUriPermissions
            } catch (_: Exception) {
                // Could not check URI permissions
            }

            // Check if URI is valid and get basic info
            val cursor = try {
                context.contentResolver.query(uri, null, null, null, null)
            } catch (_: SecurityException) {
                return Exception("Permission denied - please ensure the file is accessible and try again")
            } catch (_: Exception) {
                return Exception("Unable to access file information - please try selecting the file again")
            }

            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)

                    val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else -1

                    if (size > 0 && size > MAX_FILE_SIZE_BYTES) {
                        return Exception("File too large. Maximum size allowed is ${MAX_FILE_SIZE_MB}MB")
                    }
                }
            }

            // Check if URI is accessible
            val inputStream = try {
                context.contentResolver.openInputStream(uri)
            } catch (_: SecurityException) {
                return Exception("Permission denied - unable to access the selected file. Please try selecting the file again or check app permissions in Settings")
            } catch (_: Exception) {
                return Exception("Unable to open the selected file - please try again")
            }

            if (inputStream == null) {
                return Exception("Unable to access the selected file - please try selecting the file again")
            }

            // Check if we can read from the stream
            val testBuffer = ByteArray(4)
            val bytesRead = try {
                inputStream.use { stream ->
                    stream.read(testBuffer)
                }
            } catch (_: Exception) {
                return Exception("Unable to read from the selected file - please ensure it's not corrupted")
            }

            if (bytesRead <= 0) {
                return Exception("The selected file appears to be empty")
            }

            // Validate it's a ZIP file by trying to read ZIP structure
            validateZipStructure(context, uri)
        } catch (_: SecurityException) {
            Exception("Permission denied - please check app permissions in Settings or try selecting the file again")
        } catch (e: Exception) {
            Exception("Error validating file: ${e.message}")
        }
    }

    private fun validateZipStructure(context: Context, uri: Uri): Exception? {
        return try {

            // Check if this is a Google Drive URI - skip detailed validation for streaming
            val isGoogleDrive = uri.authority?.contains("com.google.android.apps.docs.storage") == true ||
                    uri.authority?.contains("drive.google.com") == true

            if (isGoogleDrive) {
                // For Google Drive files, just check if we can open the stream
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return Exception("Unable to access Google Drive file - please try downloading it locally first")

                inputStream.use {
                    // Try to read first few bytes to confirm it's accessible
                    val buffer = ByteArray(4)
                    val bytesRead = it.read(buffer)
                    if (bytesRead <= 0) {
                        return Exception("Unable to read from Google Drive file - please ensure it's downloaded and try again")
                    }
                    return null // Skip detailed ZIP validation for Google Drive
                }
            }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->

                    var hasValidTxtFile = false
                    var entry = zipStream.nextEntry
                    var entryCount = 0

                    while (entry != null) {
                        entryCount++

                        // Skip directories
                        if (entry.isDirectory) {
                            entry = zipStream.nextEntry
                            continue
                        }

                        val fileName = entry.name
                        val fileSize = entry.size

                        // Only process .txt files, ignore all other file types
                        if (fileName.endsWith(".txt", ignoreCase = true)) {

                            try {
                                // Check individual file size (if available from ZIP entry)
                                if (fileSize > 0 && fileSize > MAX_FILE_SIZE_BYTES) {
                                    return Exception("Text file '$fileName' is too large (${fileSize / (1024 * 1024)}MB). Maximum size allowed is ${MAX_FILE_SIZE_MB}MB per file")
                                }

                                // Validate file size by reading content if ZIP entry size is not available
                                val content = readZipEntryContent(zipStream)

                                if (content.length > MAX_FILE_SIZE_BYTES) {
                                    return Exception("Text file '$fileName' is too large. Maximum size allowed is ${MAX_FILE_SIZE_MB}MB per file")
                                }

                                // Validate file content is not suspicious
                                if (content.isNotBlank()) {
                                    hasValidTxtFile = true
                                }
                            } catch (_: Exception) {
                                // Continue to next entry instead of failing completely
                            }
                        }
                        // Silently ignore non-txt files (images, videos, etc.)

                        try {
                            entry = zipStream.nextEntry
                        } catch (_: Exception) {
                            break
                        }
                    }


                    if (!hasValidTxtFile) {
                        return Exception("No valid WhatsApp chat text files found in the ZIP archive")
                    }

                    null // No errors
                }
            } ?: Exception("Unable to open file - please check file permissions")
        } catch (_: java.util.zip.ZipException) {
            Exception("Invalid ZIP file format - please ensure you selected a valid WhatsApp chat export")
        } catch (_: SecurityException) {
            Exception("Permission denied - unable to access the selected file")
        } catch (e: Exception) {
            Exception("Error reading ZIP file: ${e.message}")
        }
    }

    private fun readZipEntryContent(zipStream: ZipInputStream): String {
        return try {
            val buffer = ByteArray(8192)
            val content = StringBuilder()
            var totalBytes = 0L
            var bytesRead: Int

            while (zipStream.read(buffer).also { bytesRead = it } != -1) {
                totalBytes += bytesRead
                // Prevent memory issues with very large files
                if (totalBytes > MAX_FILE_SIZE_BYTES) {
                    throw Exception("File content exceeds maximum size limit")
                }
                content.append(String(buffer, 0, bytesRead, Charsets.UTF_8))
            }

            content.toString()
        } catch (e: Exception) {
            throw Exception("Error reading file content: ${e.message}")
        }
    }

    private fun extractMessagesFromZip(context: Context, uri: Uri): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            try {
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    var entryCount = 0

                    while (entry != null) {
                        entryCount++

                        // Only process .txt files, ignore everything else
                        if (entry.name.endsWith(".txt", ignoreCase = true) && !entry.isDirectory) {
                            try {
                                // Read content with size validation
                                val content = readZipEntryContent(zipStream)

                                // Additional validation for chat content
                                if (isValidChatContent(content)) {
                                    val parsedMessages = parseChatContent(content)
                                    messages.addAll(parsedMessages)
                                }
                            } catch (_: Exception) {
                                // Skip this file if there's an error reading it
                                // Continue processing other files
                            }
                        }
                        // Silently skip non-txt files (images, media, etc.)

                        try {
                            entry = zipStream.nextEntry
                        } catch (_: Exception) {
                            break
                        }
                    }

                }
            } catch (e: Exception) {
                throw e
            }
        } ?: throw Exception("Unable to access the selected file")

        return messages
    }

    private fun isValidChatContent(content: String): Boolean {
        if (content.isBlank()) {
            return false
        }

        // More flexible validation - check if content looks like any chat format
        val lines = content.split("\n")
        var validMessageCount = 0
        val sampleLines = lines.take(100) // Check more lines to be thorough

        for ((_, line) in sampleLines.withIndex()) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue


            // Very flexible pattern matching for any chat-like format
            val hasTimePattern = hasTimeFormat(trimmedLine)
            val hasUserPattern = hasUserFormat(trimmedLine)

            if (hasTimePattern || hasUserPattern) {
                validMessageCount++
            }

            // Early return if we find enough valid patterns
            if (validMessageCount >= 3) {
                return true
            }
        }


        // If it's a text file with some chat-like patterns, accept it
        return validMessageCount >= 1 || (lines.size > 10 && content.contains(":"))
    }

    private fun hasTimeFormat(line: String): Boolean {
        // Look for various time/date patterns commonly used in chat exports
        val patterns = listOf(
            "\\[\\d{1,4}[\\-\\/\\.]{1}\\d{1,2}[\\-\\/\\.]{1}\\d{1,4}.+?\\d{1,2}:\\d{2}.*?\\]", // [date, time] format
            "\\d{1,2}[\\-\\/\\.]{1}\\d{1,2}[\\-\\/\\.]{1}\\d{2,4}.+?\\d{1,2}:\\d{2}", // date time without brackets
            "\\d{1,2}:\\d{2}(:\\d{2})?\\s*(AM|PM|am|pm)?", // time format
            "\\[.*?\\d{1,2}:\\d{2}.*?\\]" // any bracketed content with time
        )

        return patterns.any { line.matches(it.toRegex()) || line.contains(it.toRegex()) }
    }

    private fun hasUserFormat(line: String): Boolean {
        // Look for user patterns in chat messages
        return when {
            // Pattern: "Username: message content"
            line.contains(":") && line.indexOf(":") < line.length / 2 -> true
            // Pattern with phone numbers: "+1234567890: message"
            line.matches(".*[+]?\\d{8,}.*:.*".toRegex()) -> true
            // Pattern with common names followed by colon
            line.matches(".*[A-Za-z]+.*:.*".toRegex()) -> true
            else -> false
        }
    }

    private fun parseChatContent(content: String): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        val lines = content.split("\n")

        for (line in lines) {
            if (line.trim().isEmpty()) continue

            val message = parseMessageLine(line)
            if (message != null) {
                messages.add(message)
            }
        }

        return messages
    }

    private fun parseMessageLine(line: String): ChatMessage? {
        val trimmedLine = line.trim()
        if (trimmedLine.isEmpty()) return null

        // Approach 1: Handle the user's specific format: [2025-09-16, 5:31:01 PM] ~Justin: Hi
        val userFormatPattern = "\\[(\\d{4}-\\d{2}-\\d{2}, \\d{1,2}:\\d{2}:\\d{2} [AP]M)]\\s*([^:]+):\\s*(.*)".toRegex()
        val userMatch = userFormatPattern.find(trimmedLine)
        if (userMatch != null) {
            val timestamp = userMatch.groupValues[1]
            val sender = userMatch.groupValues[2].trim()
            val content = userMatch.groupValues[3].trim()

            if (sender.isNotEmpty() && content.isNotEmpty()) {
                return ChatMessage(
                    timestamp = parseDateString(timestamp) ?: System.currentTimeMillis(),
                    sender = cleanText(sender),
                    content = cleanText(content)
                )
            }
        }

        // Approach 2: Generic bracket format [timestamp] user: content
        val genericBracketPattern = "\\[([^]]+)]\\s*([^:]+):\\s*(.*)".toRegex()
        val bracketMatch = genericBracketPattern.find(trimmedLine)
        if (bracketMatch != null) {
            val timestamp = bracketMatch.groupValues[1]
            val sender = bracketMatch.groupValues[2].trim()
            val content = bracketMatch.groupValues[3].trim()

            if (sender.isNotEmpty() && content.isNotEmpty()) {
                return ChatMessage(
                    timestamp = parseDateString(timestamp) ?: System.currentTimeMillis(),
                    sender = cleanText(sender),
                    content = cleanText(content)
                )
            }
        }

        // Approach 3: Simple pattern - look for any line with "username: content" format
        val simpleColonIndex = trimmedLine.indexOf(": ")
        if (simpleColonIndex > 0 && simpleColonIndex < trimmedLine.length / 2) {
            val sender = trimmedLine.substring(0, simpleColonIndex).trim()
            val content = trimmedLine.substring(simpleColonIndex + 2).trim()

            // Basic validation - sender should not be too long and should contain word characters
            if (sender.length <= 50 && sender.matches(".*[A-Za-z0-9]+.*".toRegex()) &&
                content.isNotEmpty()) {

                return ChatMessage(
                    timestamp = System.currentTimeMillis(),
                    sender = cleanText(sender),
                    content = cleanText(content)
                )
            }
        }

        // Approach 4: Try original method as absolute fallback
        for (dateFormat in getDateFormats()) {
            try {
                val matcher = dateFormat.toPattern().toRegex().find(trimmedLine)
                if (matcher != null) {
                    val dateStr = matcher.value
                    val date = dateFormat.parse(dateStr)
                    val remainingContent = trimmedLine.substring(matcher.range.last + 1)

                    val colonIndex = remainingContent.indexOf(": ")
                    if (colonIndex > 0) {
                        val sender = remainingContent.substring(0, colonIndex)
                        val content = remainingContent.substring(colonIndex + 2)

                        return ChatMessage(
                            timestamp = date?.time ?: System.currentTimeMillis(),
                            sender = cleanText(sender),
                            content = cleanText(content)
                        )
                    }
                }
            } catch (_: Exception) {
                continue
            }
        }

        return null
    }

    private fun parseDateString(dateStr: String): Long? {
        // Based on the example: [2025-09-16, 5:31:01 PM]
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd, h:mm:ss a", Locale.ENGLISH), // Matches format exactly
            SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yy, HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("M/d/yy, h:mm:ss a", Locale.ENGLISH),
            SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.ENGLISH),
            SimpleDateFormat("dd.MM.yy, HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yy, HH:mm", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault()),
            SimpleDateFormat("M/d/yy, HH:mm", Locale.getDefault()),
            SimpleDateFormat("M/d/yyyy, HH:mm", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yy, HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy, HH:mm:ss", Locale.getDefault())
        )

        for (format in formats) {
            try {
                val date = format.parse(dateStr)
                if (date != null) {
                    return date.time
                }
            } catch (_: Exception) {
                continue
            }
        }

        return null
    }

    private fun filterByTimeWindow(messages: List<ChatMessage>, timeWindow: TimeWindow): List<ChatMessage> {
        val now = System.currentTimeMillis()
        val cutoffTime = when (timeWindow) {
            TimeWindow.PAST_DAY -> now - (1 * 24 * 60 * 60 * 1000L)
            TimeWindow.PAST_3_DAYS -> now - (3 * 24 * 60 * 60 * 1000L)
            TimeWindow.PAST_WEEK -> now - (7 * 24 * 60 * 60 * 1000L)
            TimeWindow.PAST_MONTH -> now - (31 * 24 * 60 * 60 * 1000L)
        }
        return messages.filter { it.timestamp >= cutoffTime }
    }

    private fun messagesToSingleLine(messages: List<ChatMessage>): String {
        return messages.joinToString("\n") { message ->
            "${cleanText(message.sender)}: ${cleanText(message.content)}"
        }
    }

    private fun cleanText(text: String): String {
        return text
            // Remove zero-width characters and other invisible Unicode characters
            .replace("[\u200B-\u200F\u202A-\u202E\u2060-\u206F\uFEFF]".toRegex(), "")
            // Remove control characters (except basic whitespace)
            .replace("[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F-\u009F]".toRegex(), "")
            // Replace various types of quotes with standard quotes
            .replace("[\u201C\u201D\u2018\u2019`\u00B4]".toRegex(), "\"")
            // Replace various dashes with standard dash
            .replace("[\u2013\u2014]".toRegex(), "-")
            // Replace various ellipsis characters with standard dots
            .replace("\u2026".toRegex(), "...")
            // Remove emojis and other extended Unicode characters (keep basic Latin, numbers, punctuation)
            .replace("[^\\u0020-\\u007E\\u00A0-\\u00FF\\s]".toRegex(), "")
            // Normalize whitespace - replace multiple spaces/tabs with single space
            .replace("\\s+".toRegex(), " ")
            // Trim whitespace from start and end
            .trim()
    }


}