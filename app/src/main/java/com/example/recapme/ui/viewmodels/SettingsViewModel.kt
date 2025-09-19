package com.example.recapme.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import com.example.recapme.data.models.*
import com.example.recapme.data.SettingsDataStore

class SettingsViewModel(context: Context) : ViewModel() {
    private val settingsDataStore = SettingsDataStore(context)
    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

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

    fun updateLanguagePreference(language: String) {
        viewModelScope.launch {
            settingsDataStore.updateLanguagePreference(language)
        }
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsDataStore.updateTheme(theme)
        }
    }

    fun exportSummaries() {
        // Basic implementation - can be expanded later
        println("Export summaries functionality called")
    }
}