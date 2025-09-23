package com.nicholasjano.recapme

import android.content.Context
import android.net.Uri
import com.nicholasjano.recapme.data.RecapService
import com.nicholasjano.recapme.data.WhatsAppProcessor
import com.nicholasjano.recapme.data.api.RecapResponse
import com.nicholasjano.recapme.data.models.AppSettings
import com.nicholasjano.recapme.data.models.SummaryStyle
import com.nicholasjano.recapme.data.models.TimeWindow
import com.nicholasjano.recapme.data.repository.RecapRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class RecapServiceTest {

    @Mock
    private lateinit var mockWhatsAppProcessor: WhatsAppProcessor

    @Mock
    private lateinit var mockRecapRepository: RecapRepository

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockUri: Uri

    private lateinit var recapService: RecapService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        recapService = RecapService(mockWhatsAppProcessor, mockRecapRepository)
    }

    @Test
    fun `processFileAndGenerateRecap success case`() = runTest {
        // Given
        val testConversation = "John: Hello there! Mary: Hi John!"
        val testSettings = AppSettings(
            recapTimeWindow = TimeWindow.PAST_WEEK,
            summaryStyle = SummaryStyle.CONCISE
        )
        val expectedApiResponse = RecapResponse(
            title = "Test Conversation",
            participants = listOf("John", "Mary"),
            recap = "John and Mary exchanged greetings."
        )

        whenever(mockWhatsAppProcessor.processWhatsAppFile(mockContext, mockUri, testSettings))
            .thenReturn(Result.success(testConversation))

        whenever(mockRecapRepository.generateRecap(testConversation, testSettings))
            .thenReturn(Result.success(expectedApiResponse))

        // When
        val result = recapService.processFileAndGenerateRecap(mockContext, mockUri, testSettings)

        // Then
        assertTrue(result.isSuccess)
        val recap = result.getOrNull()!!
        assertEquals(expectedApiResponse.title, recap.title)
        assertEquals(expectedApiResponse.participants, recap.participants)
        assertEquals(expectedApiResponse.recap, recap.recap)
    }

    @Test
    fun `processFileAndGenerateRecap handles WhatsApp processor failure`() = runTest {
        // Given
        val testSettings = AppSettings()
        val errorMessage = "Failed to process WhatsApp file"

        whenever(mockWhatsAppProcessor.processWhatsAppFile(mockContext, mockUri, testSettings))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // When
        val result = recapService.processFileAndGenerateRecap(mockContext, mockUri, testSettings)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to process WhatsApp file") == true)
    }

    @Test
    fun `processFileAndGenerateRecap handles API failure`() = runTest {
        // Given
        val testConversation = "John: Hello there!"
        val testSettings = AppSettings()
        val apiErrorMessage = "API call failed"

        whenever(mockWhatsAppProcessor.processWhatsAppFile(mockContext, mockUri, testSettings))
            .thenReturn(Result.success(testConversation))

        whenever(mockRecapRepository.generateRecap(testConversation, testSettings))
            .thenReturn(Result.failure(Exception(apiErrorMessage)))

        // When
        val result = recapService.processFileAndGenerateRecap(mockContext, mockUri, testSettings)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to generate recap") == true)
    }
}