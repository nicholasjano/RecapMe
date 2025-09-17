package com.example.recapme.data.models

data class AppSettings(
    val recapTimeWindow: TimeWindow = TimeWindow.PAST_WEEK,
    val summaryStyle: SummaryStyle = SummaryStyle.CONCISE,
    val showParticipantsBy: ParticipantDisplay = ParticipantDisplay.NAME,
    val languagePreference: String = "auto",
    val theme: AppTheme = AppTheme.SYSTEM_DEFAULT
)

enum class TimeWindow(val displayName: String) {
    PAST_WEEK("Past week")
}

enum class SummaryStyle(val displayName: String) {
    CONCISE("Concise")
}

enum class ParticipantDisplay(val displayName: String) {
    NAME("Name")
}

enum class AppTheme(val displayName: String) {
    SYSTEM_DEFAULT("System Default")
}