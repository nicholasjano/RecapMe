package com.example.recapme

import com.example.recapme.data.models.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Settings enums.
 */
class SettingsEnumTest {

    @Test
    fun enumValues_haveCorrectDisplayNames() {
        // Test the new enum values have proper display names
        assertEquals("Past day", TimeWindow.PAST_DAY.displayName)
        assertEquals("Past 3 days", TimeWindow.PAST_3_DAYS.displayName)
        assertEquals("Past week", TimeWindow.PAST_WEEK.displayName)

        assertEquals("Concise", SummaryStyle.CONCISE.displayName)
        assertEquals("Detailed", SummaryStyle.DETAILED.displayName)
        assertEquals("Bullet", SummaryStyle.BULLET.displayName)
        assertEquals("Casual", SummaryStyle.CASUAL.displayName)
        assertEquals("Formal", SummaryStyle.FORMAL.displayName)
    }

    // Note: DataStore integration tests are complex and require Android context
    // These tests would be better suited for integration/instrumented tests
}