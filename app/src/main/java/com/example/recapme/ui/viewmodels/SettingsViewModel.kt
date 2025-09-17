package com.example.recapme.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.recapme.data.models.*

class SettingsViewModel : ViewModel() {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun updateTimeWindow(timeWindow: TimeWindow) {
        _settings.value = _settings.value.copy(recapTimeWindow = timeWindow)
    }

    fun updateSummaryStyle(style: SummaryStyle) {
        _settings.value = _settings.value.copy(summaryStyle = style)
    }

    fun updateParticipantDisplay(display: ParticipantDisplay) {
        _settings.value = _settings.value.copy(showParticipantsBy = display)
    }

    fun updateLanguagePreference(language: String) {
        _settings.value = _settings.value.copy(languagePreference = language)
    }

    fun updateTheme(theme: AppTheme) {
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun exportSummaries() {
        // TODO: Implement export functionality
    }
}