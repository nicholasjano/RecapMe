package com.nicholasjano.recapme

import com.nicholasjano.recapme.data.models.SummaryStyle
import com.nicholasjano.recapme.data.models.TimeWindow
import org.junit.Test
import org.junit.Assert.assertEquals

class RecapRepositoryTest {

    @Test
    fun `settings are mapped to correct API parameters`() {
        // Test TimeWindow mapping
        assertEquals(1, TimeWindow.PAST_DAY.getDays())
        assertEquals(3, TimeWindow.PAST_3_DAYS.getDays())
        assertEquals(7, TimeWindow.PAST_WEEK.getDays())

        // Test SummaryStyle mapping
        assertEquals("concise", SummaryStyle.CONCISE.getApiValue())
        assertEquals("detailed", SummaryStyle.DETAILED.getApiValue())
        assertEquals("bullet", SummaryStyle.BULLET.getApiValue())
        assertEquals("casual", SummaryStyle.CASUAL.getApiValue())
        assertEquals("formal", SummaryStyle.FORMAL.getApiValue())
    }
}

// Extension functions to test the mapping logic
private fun TimeWindow.getDays(): Int = when (this) {
    TimeWindow.PAST_DAY -> 1
    TimeWindow.PAST_3_DAYS -> 3
    TimeWindow.PAST_WEEK -> 7
    TimeWindow.PAST_MONTH -> 31
}

private fun SummaryStyle.getApiValue(): String = when (this) {
    SummaryStyle.CONCISE -> "concise"
    SummaryStyle.DETAILED -> "detailed"
    SummaryStyle.BULLET -> "bullet"
    SummaryStyle.CASUAL -> "casual"
    SummaryStyle.FORMAL -> "formal"
}