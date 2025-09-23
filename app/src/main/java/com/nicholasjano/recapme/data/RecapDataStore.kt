package com.nicholasjano.recapme.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nicholasjano.recapme.data.models.Recap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecapDataStore(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("recaps")
        private val RECAPS_KEY = stringPreferencesKey("recaps_list")
    }

    private val gson = Gson()

    val recapsFlow: Flow<List<Recap>> = context.dataStore.data.map { preferences ->
        val recapsJson = preferences[RECAPS_KEY] ?: ""
        if (recapsJson.isEmpty()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<Recap>>() {}.type
                gson.fromJson<List<Recap>>(recapsJson, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveRecap(recap: Recap): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                val currentRecapsJson = preferences[RECAPS_KEY] ?: ""
                val currentRecaps = if (currentRecapsJson.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        val type = object : TypeToken<List<Recap>>() {}.type
                        gson.fromJson<List<Recap>>(currentRecapsJson, type) ?: emptyList()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }

                val updatedRecaps = listOf(recap) + currentRecaps
                preferences[RECAPS_KEY] = gson.toJson(updatedRecaps)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save recap: ${e.message}"))
        }
    }

    suspend fun updateRecap(updatedRecap: Recap): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                val currentRecapsJson = preferences[RECAPS_KEY] ?: ""
                val currentRecaps = if (currentRecapsJson.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        val type = object : TypeToken<List<Recap>>() {}.type
                        gson.fromJson<List<Recap>>(currentRecapsJson, type) ?: emptyList()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }

                val updatedRecaps = currentRecaps.map { recap ->
                    if (recap.id == updatedRecap.id) updatedRecap else recap
                }
                preferences[RECAPS_KEY] = gson.toJson(updatedRecaps)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update recap: ${e.message}"))
        }
    }

    suspend fun deleteRecap(recapId: String): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                val currentRecapsJson = preferences[RECAPS_KEY] ?: ""
                val currentRecaps = if (currentRecapsJson.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        val type = object : TypeToken<List<Recap>>() {}.type
                        gson.fromJson<List<Recap>>(currentRecapsJson, type) ?: emptyList()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }

                val filteredRecaps = currentRecaps.filter { it.id != recapId }
                preferences[RECAPS_KEY] = gson.toJson(filteredRecaps)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete recap: ${e.message}"))
        }
    }

    suspend fun clearAllRecaps(): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[RECAPS_KEY] = gson.toJson(emptyList<Recap>())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to clear all recaps: ${e.message}"))
        }
    }
}