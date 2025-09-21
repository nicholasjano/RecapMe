package com.example.recapme.data

import android.content.Context
import android.net.Uri
import com.example.recapme.data.models.AppSettings
import com.example.recapme.data.models.Recap
import com.example.recapme.data.repository.RecapRepository
import java.util.UUID

class RecapService(
    private val whatsAppProcessor: WhatsAppProcessor,
    private val recapRepository: RecapRepository
) {

    suspend fun processFileAndGenerateRecap(
        context: Context,
        uri: Uri,
        settings: AppSettings
    ): Result<Recap> {
        return try {
            android.util.Log.d("RecapService", "Starting file processing and recap generation")

            // Step 1: Process the ZIP file and extract conversation
            android.util.Log.d("RecapService", "Processing WhatsApp file...")
            val conversationResult = whatsAppProcessor.processWhatsAppFile(context, uri, settings)

            conversationResult.fold(
                onSuccess = { conversation ->
                    android.util.Log.d("RecapService", "WhatsApp file processed successfully. Conversation length: ${conversation.length}")

                    // Step 2: Call LLM API to generate recap
                    android.util.Log.d("RecapService", "Calling API to generate recap...")
                    val apiResult = recapRepository.generateRecap(conversation, settings)

                    apiResult.fold(
                        onSuccess = { recapResponse ->
                            android.util.Log.d("RecapService", "API call successful. Title: ${recapResponse.title}")

                            // Step 3: Convert API response to Recap model
                            val recap = Recap(
                                id = UUID.randomUUID().toString(),
                                title = recapResponse.title,
                                participants = recapResponse.users,
                                content = recapResponse.recap,
                                category = null,
                                timestamp = System.currentTimeMillis(),
                                isStarred = false
                            )

                            android.util.Log.d("RecapService", "Recap model created successfully")
                            Result.success(recap)
                        },
                        onFailure = { error ->
                            android.util.Log.e("RecapService", "API call failed: ${error.message}")
                            Result.failure(Exception("Failed to generate recap: ${error.message}", error))
                        }
                    )
                },
                onFailure = { error ->
                    android.util.Log.e("RecapService", "WhatsApp file processing failed: ${error.message}")
                    Result.failure(Exception("Failed to process WhatsApp file: ${error.message}", error))
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("RecapService", "Unexpected error in processFileAndGenerateRecap", e)
            Result.failure(Exception("Unexpected error: ${e.message}", e))
        }
    }
}