package com.nicholasjano.recapme.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nicholasjano.recapme.data.models.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryDataStore(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("categories")
        private val CATEGORIES_KEY = stringPreferencesKey("custom_categories")
    }

    val categoriesFlow: Flow<List<Category>> = context.dataStore.data.map { preferences ->
        val customCategoriesString = preferences[CATEGORIES_KEY] ?: ""
        val customCategories = if (customCategoriesString.isNotEmpty()) {
            try {
                customCategoriesString.split("|").mapNotNull { categoryString ->
                    val parts = categoryString.split(":::")
                    if (parts.size == 3) {
                        Category(
                            id = parts[0],
                            name = parts[1],
                            isDefault = false,
                            color = parts[2]
                        )
                    } else null
                }
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        // Combine default categories with custom ones
        Category.getDefaultCategories() + customCategories
    }

    suspend fun addCategory(category: Category) {
        if (!category.isDefault) {  // Only persist custom categories
            context.dataStore.edit { preferences ->
                val currentCategoriesString = preferences[CATEGORIES_KEY] ?: ""
                val existingCategories = if (currentCategoriesString.isNotEmpty()) {
                    currentCategoriesString.split("|").toMutableList()
                } else {
                    mutableListOf()
                }

                // Create category string: id:::name:::color
                val newCategoryString = "${category.id}:::${category.name}:::${category.color}"

                // Check if category already exists
                val categoryExists = existingCategories.any { it.startsWith("${category.id}:::") }

                if (!categoryExists) {
                    existingCategories.add(newCategoryString)
                    preferences[CATEGORIES_KEY] = existingCategories.joinToString("|")
                }
            }
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        context.dataStore.edit { preferences ->
            val currentCategoriesString = preferences[CATEGORIES_KEY] ?: ""
            if (currentCategoriesString.isNotEmpty()) {
                val existingCategories = currentCategoriesString.split("|").toMutableList()
                existingCategories.removeAll { it.startsWith("${categoryId}:::") }
                preferences[CATEGORIES_KEY] = existingCategories.joinToString("|")
            }
        }
    }
}