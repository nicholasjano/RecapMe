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
        assertEquals(AppTheme.SYSTEM_DEFAULT, settings.theme)
    }

    @Test
    fun timeWindow_displayNames_areCorrect() {
        assertEquals("Past day", TimeWindow.PAST_DAY.displayName)
        assertEquals("Past 3 days", TimeWindow.PAST_3_DAYS.displayName)
        assertEquals("Past week", TimeWindow.PAST_WEEK.displayName)
    }

    @Test
    fun summaryStyle_displayNames_areCorrect() {
        assertEquals("Concise", SummaryStyle.CONCISE.displayName)
        assertEquals("Detailed", SummaryStyle.DETAILED.displayName)
        assertEquals("Bullet", SummaryStyle.BULLET.displayName)
        assertEquals("Casual", SummaryStyle.CASUAL.displayName)
        assertEquals("Formal", SummaryStyle.FORMAL.displayName)
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
            summaryStyle = SummaryStyle.CONCISE
        )

        assertEquals(TimeWindow.PAST_WEEK, updatedSettings.recapTimeWindow)
        assertEquals(SummaryStyle.CONCISE, updatedSettings.summaryStyle)
    }
}