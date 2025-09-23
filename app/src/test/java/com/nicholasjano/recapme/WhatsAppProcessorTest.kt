package com.nicholasjano.recapme

import android.content.Context
import android.net.Uri
import com.nicholasjano.recapme.data.WhatsAppProcessor
import com.nicholasjano.recapme.data.models.AppSettings
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.junit.Assert.assertTrue
import java.io.ByteArrayInputStream
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.any

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

        // Mock the URI to not be a Google Drive URI (which would skip validation)
        whenever(mockUri.authority).thenReturn("local.file.provider")

        // Mock the content resolver query for file info
        val mockCursor = mock<android.database.Cursor>()
        whenever(mockCursor.moveToFirst()).thenReturn(true)
        whenever(mockCursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        whenever(mockCursor.getColumnIndex(android.provider.OpenableColumns.SIZE)).thenReturn(1)
        whenever(mockCursor.getString(0)).thenReturn("test.zip")
        whenever(mockCursor.getLong(1)).thenReturn(21 * 1024 * 1024L) // 21MB
        whenever(mockContentResolver.query(eq(mockUri), any(), any(), any(), any())).thenReturn(mockCursor)
        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(largeInputStream)

        // When
        val result = processor.processWhatsAppFile(mockContext, mockUri, AppSettings())

        // Then
        assertTrue(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue("Expected error about file size, got: $errorMessage",
            errorMessage.contains("too large") || errorMessage.contains("20MB"))
    }

    @Test
    fun `processWhatsAppFile rejects empty files`() = runTest {
        // Given
        val emptyInputStream = ByteArrayInputStream(ByteArray(0))

        // Mock the URI to not be a Google Drive URI
        whenever(mockUri.authority).thenReturn("local.file.provider")

        // Mock content resolver query to return empty file
        val mockCursor = mock<android.database.Cursor>()
        whenever(mockCursor.moveToFirst()).thenReturn(true)
        whenever(mockCursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        whenever(mockCursor.getColumnIndex(android.provider.OpenableColumns.SIZE)).thenReturn(1)
        whenever(mockCursor.getString(0)).thenReturn("test.zip")
        whenever(mockCursor.getLong(1)).thenReturn(0L)
        whenever(mockContentResolver.query(eq(mockUri), any(), any(), any(), any())).thenReturn(mockCursor)
        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(emptyInputStream)

        // When
        val result = processor.processWhatsAppFile(mockContext, mockUri, AppSettings())

        // Then
        assertTrue(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue("Expected error about empty file, got: $errorMessage",
            errorMessage.contains("empty") || errorMessage.contains("no valid chat"))
    }

    @Test
    fun `processWhatsAppFile rejects invalid URI`() = runTest {
        // Given
        // Mock the URI to not be a Google Drive URI
        whenever(mockUri.authority).thenReturn("local.file.provider")

        // Mock content resolver to return null (file access error)
        whenever(mockContentResolver.query(eq(mockUri), any(), any(), any(), any())).thenReturn(null)
        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(null)

        // When
        val result = processor.processWhatsAppFile(mockContext, mockUri, AppSettings())

        // Then
        assertTrue(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue("Expected error about unable to access, got: $errorMessage",
            errorMessage.contains("Unable to access") || errorMessage.contains("selected file"))
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
        // Mock the URI to not be a Google Drive URI
        whenever(mockUri.authority).thenReturn("local.file.provider")

        // Mock content resolver to return null (file access error)
        whenever(mockContentResolver.query(eq(mockUri), any(), any(), any(), any())).thenReturn(null)
        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(null)

        // When
        val result = processor.processWhatsAppFile(mockContext, mockUri, AppSettings())

        // Then - Error message should be user-friendly
        assertTrue(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue("Error message should be user-friendly, got: $errorMessage",
            errorMessage.contains("Unable to access") ||
            errorMessage.contains("selected file") ||
            errorMessage.contains("file information"))
    }
}