package com.example.recapme.data

import android.content.Context
import android.net.Uri
import com.example.recapme.data.models.ChatMessage
import com.example.recapme.data.models.AppSettings
import com.example.recapme.data.models.TimeWindow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipInputStream

class WhatsAppProcessor {
    private fun getDateFormats() = listOf(
        SimpleDateFormat("[yyyy-MM-dd, h:mm:ss a] ", Locale.getDefault()),
        SimpleDateFormat("[dd/MM/yy, HH:mm:ss] ", Locale.getDefault()),
        SimpleDateFormat("[M/d/yy, h:mm:ss a] ", Locale.getDefault()),
        SimpleDateFormat("[dd.MM.yy, HH:mm:ss] ", Locale.getDefault())
    )

    companion object {
        private const val MAX_FILE_SIZE_MB = 20
        private const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024L
    }

    fun processWhatsAppFile(context: Context, uri: Uri, settings: AppSettings): Result<String> {
        return try {
            // Check file size first
            val fileSize = getFileSize(context, uri)
            if (fileSize > MAX_FILE_SIZE_BYTES) {
                return Result.failure(Exception("File too large. Maximum size allowed is ${MAX_FILE_SIZE_MB}MB"))
            }

            val messages = extractMessagesFromZip(context, uri)
            val filteredMessages = filterByTimeWindow(messages, settings.recapTimeWindow)
            val singleLineContent = messagesToSingleLine(filteredMessages)
            Result.success(singleLineContent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.available().toLong()
        } ?: 0L
    }

    private fun extractMessagesFromZip(context: Context, uri: Uri): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zipStream ->
                var entry = zipStream.nextEntry
                while (entry != null) {
                    if (entry.name.endsWith(".txt") && !entry.isDirectory) {
                        val content = BufferedReader(InputStreamReader(zipStream, "UTF-8")).use { reader ->
                            reader.readText()
                        }
                        messages.addAll(parseChatContent(content))
                    }
                    entry = zipStream.nextEntry
                }
            }
        }

        return messages
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
        for (dateFormat in getDateFormats()) {
            try {
                val matcher = dateFormat.toPattern().toRegex().find(line)
                if (matcher != null) {
                    val dateStr = matcher.value
                    val date = dateFormat.parse(dateStr)
                    val remainingContent = line.substring(matcher.range.last + 1)

                    val colonIndex = remainingContent.indexOf(": ")
                    if (colonIndex > 0) {
                        val sender = remainingContent.substring(0, colonIndex)
                        val content = remainingContent.substring(colonIndex + 2)

                        if (!isSystemMessage(content)) {
                            return ChatMessage(
                                timestamp = date?.time ?: System.currentTimeMillis(),
                                sender = cleanText(sender),
                                content = cleanText(content)
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                continue
            }
        }
        return null
    }

    private fun isSystemMessage(content: String): Boolean {
        val systemMessages = listOf(
            "joined using this group's invite link",
            "left",
            "was added",
            "was removed",
            "changed the group description",
            "changed the subject",
            "Messages and calls are end-to-end encrypted",
            "<Media omitted>",
            "This message was deleted"
        )

        return systemMessages.any { content.contains(it, ignoreCase = true) }
    }

    private fun filterByTimeWindow(messages: List<ChatMessage>, timeWindow: TimeWindow): List<ChatMessage> {
        val now = System.currentTimeMillis()
        val cutoffTime = when (timeWindow) {
            TimeWindow.PAST_DAY -> now - (1 * 24 * 60 * 60 * 1000L)
            TimeWindow.PAST_3_DAYS -> now - (3 * 24 * 60 * 60 * 1000L)
            TimeWindow.PAST_WEEK -> now - (7 * 24 * 60 * 60 * 1000L)
        }
        return messages.filter { it.timestamp >= cutoffTime }
    }

    private fun messagesToSingleLine(messages: List<ChatMessage>): String {
        return messages.joinToString(" ") { message ->
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