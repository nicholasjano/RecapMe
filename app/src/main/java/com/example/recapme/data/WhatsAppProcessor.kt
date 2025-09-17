package com.example.recapme.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.recapme.data.models.ChatMessage
import com.example.recapme.data.models.Recap
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.random.Random

class WhatsAppProcessor {
    companion object {
        private val DATE_FORMATS = listOf(
            SimpleDateFormat("[dd/MM/yy, HH:mm:ss] ", Locale.getDefault()),
            SimpleDateFormat("[M/d/yy, h:mm:ss a] ", Locale.getDefault()),
            SimpleDateFormat("[dd.MM.yy, HH:mm:ss] ", Locale.getDefault())
        )
    }

    suspend fun processWhatsAppFile(context: Context, uri: Uri): Result<List<ChatMessage>> {
        return try {
            val messages = extractMessagesFromZip(context, uri)
            val filteredMessages = filterLastSevenDays(messages)
            Result.success(filteredMessages)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
        for (dateFormat in DATE_FORMATS) {
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
                                sender = sender.trim(),
                                content = content.trim()
                            )
                        }
                    }
                }
            } catch (e: Exception) {
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

    private fun filterLastSevenDays(messages: List<ChatMessage>): List<ChatMessage> {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return messages.filter { it.timestamp >= sevenDaysAgo }
    }


}