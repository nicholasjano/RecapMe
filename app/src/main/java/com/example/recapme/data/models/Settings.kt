package com.example.recapme.data.models

data class AppSettings(
    val recapTimeWindow: TimeWindow = TimeWindow.PAST_WEEK,
    val summaryStyle: SummaryStyle = SummaryStyle.CONCISE,
    val theme: AppTheme = AppTheme.SYSTEM_DEFAULT
)

enum class TimeWindow(val displayName: String) {
    PAST_DAY("Past day"),
    PAST_3_DAYS("Past 3 days"),
    PAST_WEEK("Past week"),
    PAST_MONTH("Past month")
}

enum class SummaryStyle(val displayName: String) {
    CONCISE("Concise"),
    DETAILED("Detailed"),
    BULLET("Bullet"),
    CASUAL("Casual"),
    FORMAL("Formal")
}


enum class AppTheme(val displayName: String) {
    SYSTEM_DEFAULT("System Default")
}