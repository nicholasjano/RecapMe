package com.example.recapme.data.repository

import com.example.recapme.data.api.RecapApiService
import com.example.recapme.data.api.RecapRequest
import com.example.recapme.data.api.RecapResponse
import com.example.recapme.data.models.AppSettings
import com.example.recapme.data.models.TimeWindow
import com.example.recapme.data.models.SummaryStyle
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RecapRepository {
    private val apiService: RecapApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://recapme-backend.onrender.com/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(RecapApiService::class.java)
    }

    suspend fun generateRecap(conversation: String, settings: AppSettings): Result<RecapResponse> {
        return try {
            // Validate input
            if (conversation.isBlank()) {
                return Result.failure(Exception("Conversation content cannot be empty"))
            }

            val days = when (settings.recapTimeWindow) {
                TimeWindow.PAST_DAY -> 1
                TimeWindow.PAST_3_DAYS -> 3
                TimeWindow.PAST_WEEK -> 7
                TimeWindow.PAST_MONTH -> 31
            }

            val style = when (settings.summaryStyle) {
                SummaryStyle.CONCISE -> "concise"
                SummaryStyle.DETAILED -> "detailed"
                SummaryStyle.BULLET -> "bullet"
                SummaryStyle.CASUAL -> "casual"
                SummaryStyle.FORMAL -> "formal"
            }

            val request = RecapRequest(
                conversation = conversation,
                days = days,
                style = style
            )

            val response = apiService.generateRecap(request)

            when {
                response.isSuccessful && response.body() != null -> {
                    val recapResponse = response.body()!!

                    // Validate response data
                    if (recapResponse.Title.isBlank() || recapResponse.Recap.isBlank()) {
                        Result.failure(Exception("Invalid response from server: missing title or recap content"))
                    } else {
                        Result.success(recapResponse)
                    }
                }
                response.code() == 400 -> Result.failure(Exception("Invalid request format"))
                response.code() == 429 -> Result.failure(Exception("Rate limit exceeded. Please try again later"))
                response.code() == 500 -> Result.failure(Exception("Server error. Please try again later"))
                response.code() in 500..599 -> Result.failure(Exception("Server is temporarily unavailable"))
                else -> Result.failure(Exception("Request failed: ${response.code()} ${response.message()}"))
            }
        } catch (_: java.net.UnknownHostException) {
            Result.failure(Exception("Network error: Please check your internet connection"))
        } catch (_: java.net.SocketTimeoutException) {
            Result.failure(Exception("Request timeout: The server took too long to respond"))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
}