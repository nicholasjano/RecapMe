package com.example.recapme

import com.example.recapme.data.models.*
import com.example.recapme.ui.viewmodels.SettingsViewModel
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit tests for SettingsViewModel.
 */
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        viewModel = SettingsViewModel()
    }

    @Test
    fun settings_initialState_hasDefaults() {
        val settings = viewModel.settings.value

        assertEquals(TimeWindow.PAST_WEEK, settings.recapTimeWindow)
        assertEquals(SummaryStyle.CONCISE, settings.summaryStyle)
        assertEquals(ParticipantDisplay.NAME, settings.showParticipantsBy)
        assertEquals("auto", settings.languagePreference)
        assertEquals(AppTheme.SYSTEM_DEFAULT, settings.theme)
    }

    @Test
    fun updateTimeWindow_changesTimeWindow() {
        viewModel.updateTimeWindow(TimeWindow.PAST_WEEK)

        assertEquals(TimeWindow.PAST_WEEK, viewModel.settings.value.recapTimeWindow)
    }

    @Test
    fun updateSummaryStyle_changesSummaryStyle() {
        viewModel.updateSummaryStyle(SummaryStyle.CONCISE)

        assertEquals(SummaryStyle.CONCISE, viewModel.settings.value.summaryStyle)
    }

    @Test
    fun updateParticipantDisplay_changesParticipantDisplay() {
        viewModel.updateParticipantDisplay(ParticipantDisplay.NAME)

        assertEquals(ParticipantDisplay.NAME, viewModel.settings.value.showParticipantsBy)
    }

    @Test
    fun updateLanguagePreference_changesLanguage() {
        viewModel.updateLanguagePreference("es")

        assertEquals("es", viewModel.settings.value.languagePreference)
    }

    @Test
    fun updateTheme_changesTheme() {
        viewModel.updateTheme(AppTheme.SYSTEM_DEFAULT)

        assertEquals(AppTheme.SYSTEM_DEFAULT, viewModel.settings.value.theme)
    }

    @Test
    fun exportSummaries_executesWithoutError() {
        // Test that the method executes without throwing exceptions
        viewModel.exportSummaries()
        // No assertion needed - we're just verifying it doesn't crash
    }
}