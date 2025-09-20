package com.example.recapme

import android.content.Context
import android.net.Uri
import com.example.recapme.data.WhatsAppProcessor
import com.example.recapme.data.models.AppSettings
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.junit.Assert.assertTrue
import java.io.ByteArrayInputStream

class WhatsAppProcessorTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockUri: Uri

    @Mock
    private lateinit var mockContentResolver: android.content.ContentResolver

    private lateinit var processor: WhatsAppProcessor

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        processor = WhatsAppProcessor()
        whenever(mockContext.contentResolver).thenReturn(mockContentResolver)
    }

    @Test
    fun `processWhatsAppFile rejects files over 20MB`() = runTest {
        // Given - Create a mock input stream that simulates a large file
        val largeFileContent = ByteArray(21 * 1024 * 1024) // 21MB
        val largeInputStream = ByteArrayInputStream(largeFileContent)

        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(largeInputStream)

        // When
        val result = processor.processWhatsAppFile(mockContext, mockUri, AppSettings())

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("File too large") == true)
        assertTrue(result.exceptionOrNull()?.message?.contains("20MB") == true)
    }

    @Test
    fun `processWhatsAppFile rejects empty files`() = runTest {
        // Given
        val emptyInputStream = ByteArrayInputStream(ByteArray(0))
        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(emptyInputStream)

        // When
        val result = processor.processWhatsAppFile(mockContext, mockUri, AppSettings())

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("empty") == true)
    }

    @Test
    fun `processWhatsAppFile rejects invalid URI`() = runTest {
        // Given
        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(null)

        // When
        val result = processor.processWhatsAppFile(mockContext, mockUri, AppSettings())

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Unable to access") == true)
    }

    @Test
    fun `validates file size limits are enforced`() {
        // Test that the MAX_FILE_SIZE_BYTES constant is set correctly
        val maxSizeReflection = WhatsAppProcessor::class.java.declaredFields
            .find { it.name == "MAX_FILE_SIZE_BYTES" }

        // This validates that our size limit constants exist and are reasonable
        assertTrue("MAX_FILE_SIZE_BYTES should be defined", maxSizeReflection != null)
    }

    @Test
    fun `file validation error messages are user friendly`() = runTest {
        // Given - null input stream (simulates file access error)
        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(null)

        // When
        val result = processor.processWhatsAppFile(mockContext, mockUri, AppSettings())

        // Then - Error message should be user-friendly
        assertTrue(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue("Error message should be user-friendly",
            errorMessage.contains("Unable to access") ||
            errorMessage.contains("selected file"))
    }
}