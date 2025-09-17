package com.example.recapme.data.models

data class AppSettings(
    val recapTimeWindow: TimeWindow = TimeWindow.PAST_WEEK,
    val summaryStyle: SummaryStyle = SummaryStyle.CONCISE,
    val showParticipantsBy: ParticipantDisplay = ParticipantDisplay.NAME,
    val languagePreference: String = "auto",
    val theme: AppTheme = AppTheme.SYSTEM_DEFAULT
)

enum class TimeWindow(val displayName: String) {
    PAST_24_HOURS("Past 24 hours"),
    PAST_3_DAYS("Past 3 days"),
    PAST_WEEK("Past week")
}

enum class SummaryStyle(val displayName: String) {
    CONCISE("Concise"),
    DETAILED("Detailed"),
    ACTION_FOCUSED("Action-focused")
}

enum class ParticipantDisplay(val displayName: String) {
    NAME("Name"),
    PHONE_NUMBER("Phone Number")
}

enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM_DEFAULT("System Default")
}