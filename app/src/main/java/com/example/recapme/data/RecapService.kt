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
            // Step 1: Process the ZIP file and extract conversation
            val conversationResult = whatsAppProcessor.processWhatsAppFile(context, uri, settings)

            conversationResult.fold(
                onSuccess = { conversation ->
                    // Step 2: Call LLM API to generate recap
                    val apiResult = recapRepository.generateRecap(conversation, settings)

                    apiResult.fold(
                        onSuccess = { recapResponse ->
                            // Step 3: Convert API response to Recap model
                            val recap = Recap(
                                id = UUID.randomUUID().toString(),
                                title = recapResponse.title,
                                participants = recapResponse.participants,
                                recap = recapResponse.recap,
                                category = null,
                                timestamp = System.currentTimeMillis(),
                                isStarred = false
                            )

                            Result.success(recap)
                        },
                        onFailure = { error ->
                            Result.failure(Exception("Failed to generate recap: ${error.message}", error))
                        }
                    )
                },
                onFailure = { error ->
                    Result.failure(Exception("Failed to process WhatsApp file: ${error.message}", error))
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}", e))
        }
    }
}