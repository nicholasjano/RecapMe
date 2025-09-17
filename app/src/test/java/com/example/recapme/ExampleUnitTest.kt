package com.example.recapme

import com.example.recapme.data.models.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Settings data models and enums.
 */
class SettingsTest {

    @Test
    fun appSettings_defaultValues_areCorrect() {
        val settings = AppSettings()

        assertEquals(TimeWindow.PAST_WEEK, settings.recapTimeWindow)
        assertEquals(SummaryStyle.CONCISE, settings.summaryStyle)
        assertEquals(ParticipantDisplay.NAME, settings.showParticipantsBy)
        assertEquals("auto", settings.languagePreference)
        assertEquals(AppTheme.SYSTEM_DEFAULT, settings.theme)
    }

    @Test
    fun timeWindow_displayName_isCorrect() {
        assertEquals("Past week", TimeWindow.PAST_WEEK.displayName)
    }

    @Test
    fun summaryStyle_displayName_isCorrect() {
        assertEquals("Concise", SummaryStyle.CONCISE.displayName)
    }

    @Test
    fun participantDisplay_displayName_isCorrect() {
        assertEquals("Name", ParticipantDisplay.NAME.displayName)
    }

    @Test
    fun appTheme_displayName_isCorrect() {
        assertEquals("System Default", AppTheme.SYSTEM_DEFAULT.displayName)
    }

    @Test
    fun appSettings_copy_updatesValues() {
        val originalSettings = AppSettings()
        val updatedSettings = originalSettings.copy(
            recapTimeWindow = TimeWindow.PAST_WEEK,
            summaryStyle = SummaryStyle.CONCISE,
            languagePreference = "en"
        )

        assertEquals(TimeWindow.PAST_WEEK, updatedSettings.recapTimeWindow)
        assertEquals(SummaryStyle.CONCISE, updatedSettings.summaryStyle)
        assertEquals("en", updatedSettings.languagePreference)
        assertEquals(ParticipantDisplay.NAME, updatedSettings.showParticipantsBy) // unchanged
    }
}