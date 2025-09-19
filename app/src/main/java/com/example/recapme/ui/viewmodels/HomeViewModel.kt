package com.example.recapme.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.recapme.data.WhatsAppProcessor
import com.example.recapme.data.SettingsDataStore
import com.example.recapme.data.models.Recap
import com.example.recapme.data.models.Category
import com.example.recapme.data.models.RecapStatistics

class HomeViewModel(private val settingsDataStore: SettingsDataStore? = null) : ViewModel() {
    private val whatsAppProcessor = WhatsAppProcessor()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow(Category.ALL_CATEGORY_ID)
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _allRecaps = MutableStateFlow<List<Recap>>(emptyList())
    private val allRecaps: StateFlow<List<Recap>> = _allRecaps.asStateFlow()

    private val _categories = MutableStateFlow(Category.getDefaultCategories())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showFilePicker = MutableStateFlow(false)
    val showFilePicker: StateFlow<Boolean> = _showFilePicker.asStateFlow()

    private val _showAddCategoryDialog = MutableStateFlow(false)
    val showAddCategoryDialog: StateFlow<Boolean> = _showAddCategoryDialog.asStateFlow()

    private val _showCategoryPickerForRecap = MutableStateFlow<String?>(null)
    val showCategoryPickerForRecap: StateFlow<String?> = _showCategoryPickerForRecap.asStateFlow()

    val filteredRecaps = combine(
        allRecaps,
        searchQuery,
        selectedCategoryId
    ) { recaps, query, categoryId ->
        recaps.filter { recap ->
            val matchesSearch = query.isEmpty() ||
                recap.title.contains(query, ignoreCase = true) ||
                recap.content.contains(query, ignoreCase = true) ||
                recap.participants.any { it.contains(query, ignoreCase = true) }

            val matchesCategory = if (categoryId == Category.ALL_CATEGORY_ID) {
                true // Show all recaps including those without categories
            } else {
                recap.category == categoryId // Only show recaps with the selected category
            }

            matchesSearch && matchesCategory
        }
    }

    private val _statistics = MutableStateFlow(calculateStatistics(emptyList()))
    val statistics: StateFlow<RecapStatistics> = _statistics.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun showAddCategoryDialog() {
        _showAddCategoryDialog.value = true
    }

    fun hideAddCategoryDialog() {
        _showAddCategoryDialog.value = false
    }

    fun addCustomCategory(name: String, color: String = "#4CAF50") {
        if (name.isNotBlank()) {
            val newCategory = Category(
                id = name.lowercase().replace(" ", "_"),
                name = name,
                isDefault = false,
                color = color
            )
            val currentCategories = _categories.value.toMutableList()
            if (!currentCategories.any { it.id == newCategory.id }) {
                currentCategories.add(newCategory)
                _categories.value = currentCategories
            }
        }
        hideAddCategoryDialog()
    }

    fun deleteCategory(categoryId: String) {
        val currentCategories = _categories.value.toMutableList()
        currentCategories.removeAll { it.id == categoryId && !it.isDefault }
        _categories.value = currentCategories

        if (_selectedCategoryId.value == categoryId) {
            _selectedCategoryId.value = Category.ALL_CATEGORY_ID
        }

        val updatedRecaps = _allRecaps.value.map { recap ->
            if (recap.category == categoryId) {
                recap.copy(category = null)
            } else {
                recap
            }
        }
        _allRecaps.value = updatedRecaps
        _statistics.value = calculateStatistics(updatedRecaps)
    }

    fun toggleStar(recapId: String) {
        val currentRecaps = _allRecaps.value
        val updatedRecaps = currentRecaps.map { recap ->
            if (recap.id == recapId) {
                recap.copy(isStarred = !recap.isStarred)
            } else {
                recap
            }
        }
        _allRecaps.value = updatedRecaps
        _statistics.value = calculateStatistics(updatedRecaps)
    }


    fun onFilePickerDismissed() {
        _showFilePicker.value = false
    }

    fun processSelectedFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Get current settings, or use defaults if datastore is not available
                val settings = settingsDataStore?.settingsFlow?.first()
                    ?: com.example.recapme.data.models.AppSettings()

                whatsAppProcessor.processWhatsAppFile(context, uri, settings)
                    .onSuccess { chatContent ->
                        // Extract participants from the chat content
                        val participants = extractParticipantsFromContent(chatContent)

                        val recap = Recap(
                            id = java.util.UUID.randomUUID().toString(),
                            title = "WhatsApp Chat (${participants.size} participants)",
                            participants = participants,
                            content = chatContent,
                            category = null,
                            timestamp = System.currentTimeMillis(),
                            isStarred = false
                        )

                        val currentRecaps = _allRecaps.value.toMutableList()
                        currentRecaps.add(0, recap)
                        _allRecaps.value = currentRecaps
                        _statistics.value = calculateStatistics(currentRecaps)
                    }
                    .onFailure { error ->
                        _errorMessage.value = "Failed to process file: ${error.message}"
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Error accessing settings: ${e.message}"
            }

            _isLoading.value = false
        }
    }

    private fun extractParticipantsFromContent(content: String): List<String> {
        // Extract participant names from the single line content
        val participants = mutableSetOf<String>()
        val pattern = Regex("([^:]+):")

        pattern.findAll(content).forEach { match ->
            val participant = match.groupValues[1].trim()
            if (participant.isNotBlank()) {
                participants.add(participant)
            }
        }

        return participants.toList()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun updateRecapCategory(recapId: String, categoryId: String?) {
        val currentRecaps = _allRecaps.value
        val updatedRecaps = currentRecaps.map { recap ->
            if (recap.id == recapId) {
                recap.copy(category = categoryId)
            } else {
                recap
            }
        }
        _allRecaps.value = updatedRecaps
        _statistics.value = calculateStatistics(updatedRecaps)
        _showCategoryPickerForRecap.value = null
    }

    fun showCategoryPickerForRecap(recapId: String) {
        _showCategoryPickerForRecap.value = recapId
    }

    fun hideCategoryPicker() {
        _showCategoryPickerForRecap.value = null
    }

    fun showFilePicker() {
        _showFilePicker.value = true
    }

    private fun calculateStatistics(recaps: List<Recap>): RecapStatistics {
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000L)

        return RecapStatistics(
            thisWeek = recaps.count { it.timestamp >= weekAgo },
            starred = recaps.count { it.isStarred },
            total = recaps.size
        )
    }

}