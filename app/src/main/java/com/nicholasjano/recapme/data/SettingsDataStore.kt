package com.nicholasjano.recapme.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nicholasjano.recapme.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

        private val RECAP_TIME_WINDOW = stringPreferencesKey("recap_time_window")
        private val SUMMARY_STYLE = stringPreferencesKey("summary_style")
        private val THEME = stringPreferencesKey("theme")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            recapTimeWindow = try {
                TimeWindow.valueOf(preferences[RECAP_TIME_WINDOW] ?: TimeWindow.PAST_WEEK.name)
            } catch (_: IllegalArgumentException) {
                TimeWindow.PAST_WEEK
            },
            summaryStyle = try {
                SummaryStyle.valueOf(preferences[SUMMARY_STYLE] ?: SummaryStyle.CONCISE.name)
            } catch (_: IllegalArgumentException) {
                SummaryStyle.CONCISE
            },
            theme = try {
                AppTheme.valueOf(preferences[THEME] ?: AppTheme.SYSTEM_DEFAULT.name)
            } catch (_: IllegalArgumentException) {
                AppTheme.SYSTEM_DEFAULT
            }
        )
    }

    suspend fun updateTimeWindow(timeWindow: TimeWindow) {
        context.dataStore.edit { preferences ->
            preferences[RECAP_TIME_WINDOW] = timeWindow.name
        }
    }

    suspend fun updateSummaryStyle(style: SummaryStyle) {
        context.dataStore.edit { preferences ->
            preferences[SUMMARY_STYLE] = style.name
        }
    }



    suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME] = theme.name
        }
    }
}