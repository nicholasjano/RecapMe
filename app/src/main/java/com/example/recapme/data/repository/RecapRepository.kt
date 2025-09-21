package com.example.recapme.data.repository

import com.example.recapme.data.api.RecapApiService
import com.example.recapme.data.api.RecapRequest
import com.example.recapme.data.api.RecapResponse
import com.example.recapme.data.models.AppSettings
import com.example.recapme.data.models.SummaryStyle
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RecapRepository {
    private val apiService: RecapApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = if (com.example.recapme.BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
            // Never log sensitive headers
            redactHeader("X-API-Key")
            redactHeader("Authorization")
        }

        // API Key interceptor
        val apiKeyInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            // Add API key header (never log this)
            if (com.example.recapme.BuildConfig.RECAP_API_KEY.isNotEmpty()) {
                requestBuilder.addHeader("X-API-Key", com.example.recapme.BuildConfig.RECAP_API_KEY)
            }

            chain.proceed(requestBuilder.build())
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor) // Add API key first
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(com.example.recapme.BuildConfig.RECAP_API_URL)
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

            val style = when (settings.summaryStyle) {
                SummaryStyle.CONCISE -> "concise"
                SummaryStyle.DETAILED -> "detailed"
                SummaryStyle.BULLET -> "bullet"
                SummaryStyle.CASUAL -> "casual"
                SummaryStyle.FORMAL -> "formal"
            }

            val request = RecapRequest(
                conversation = conversation,
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
                response.code() == 401 -> Result.failure(Exception("Authentication failed: API access denied. Please check if the server requires authentication"))
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