package com.example.recapme.ui.viewmodels

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.recapme.data.models.*
import com.example.recapme.data.SettingsDataStore
import com.example.recapme.data.RecapDataStore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SettingsViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val settingsDataStore = SettingsDataStore(context)
    private val recapDataStore = RecapDataStore(context)

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _availableRecaps = MutableStateFlow<List<Recap>>(emptyList())
    val availableRecaps: StateFlow<List<Recap>> = _availableRecaps.asStateFlow()

    fun updateTimeWindow(timeWindow: TimeWindow) {
        viewModelScope.launch {
            settingsDataStore.updateTimeWindow(timeWindow)
        }
    }

    fun updateSummaryStyle(style: SummaryStyle) {
        viewModelScope.launch {
            settingsDataStore.updateSummaryStyle(style)
        }
    }

    fun updateParticipantDisplay(display: ParticipantDisplay) {
        viewModelScope.launch {
            settingsDataStore.updateParticipantDisplay(display)
        }
    }


    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsDataStore.updateTheme(theme)
        }
    }

    fun exportSummaries() {
        viewModelScope.launch {
            try {
                val recaps = recapDataStore.recapsFlow.first()
                if (recaps.isEmpty()) {
                    android.util.Log.w("SettingsViewModel", "No recaps to export")
                    return@launch
                }

                _availableRecaps.value = recaps
                _showExportDialog.value = true

            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Failed to load recaps for export", e)
            }
        }
    }

    fun hideExportDialog() {
        _showExportDialog.value = false
    }

    fun exportSelectedRecaps(selectedRecaps: List<Recap>) {
        viewModelScope.launch {
            try {
                if (selectedRecaps.isEmpty()) {
                    android.util.Log.w("SettingsViewModel", "No recaps selected for export")
                    return@launch
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                val timestamp = dateFormat.format(Date())
                val zipFileName = "RecapMe_Export_$timestamp.zip"

                val cacheDir = File(appContext.cacheDir, "exports")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                val zipFile = File(cacheDir, zipFileName)

                ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                    selectedRecaps.forEachIndexed { index, recap ->
                        val sanitizedTitle = recap.title.replace(Regex("[^a-zA-Z0-9\\-_\\s]"), "")
                            .replace(Regex("\\s+"), "_")
                            .take(50)

                        val fileName = "${index + 1}_${sanitizedTitle}.txt"
                        val zipEntry = ZipEntry(fileName)
                        zipOut.putNextEntry(zipEntry)

                        val content = buildString {
                            appendLine("Title: ${recap.title}")
                            appendLine("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(recap.timestamp))}")
                            if (recap.participants.isNotEmpty()) {
                                appendLine("Participants: ${recap.participants.joinToString(", ")}")
                            }
                            if (recap.category != null) {
                                appendLine("Category: ${recap.category}")
                            }
                            appendLine()
                            appendLine("Content:")
                            appendLine(recap.content)
                        }

                        zipOut.write(content.toByteArray())
                        zipOut.closeEntry()
                    }
                }

                // Share the zip file
                val uri = FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.fileprovider",
                    zipFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "RecapMe Export")
                    putExtra(Intent.EXTRA_TEXT, "Exported ${selectedRecaps.size} recaps from RecapMe")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }

                val chooserIntent = Intent.createChooser(shareIntent, "Save RecapMe Export")
                chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                appContext.startActivity(chooserIntent)

                android.util.Log.i("SettingsViewModel", "Export completed: ${selectedRecaps.size} recaps exported to $zipFileName")

                // Hide dialog after successful export
                _showExportDialog.value = false

            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Export failed", e)
            }
        }
    }
}