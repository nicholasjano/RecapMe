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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import com.example.recapme.data.WhatsAppProcessor
import com.example.recapme.data.SettingsDataStore
import com.example.recapme.data.RecapDataStore
import com.example.recapme.data.CategoryDataStore
import com.example.recapme.data.RecapService
import com.example.recapme.data.repository.RecapRepository
import com.example.recapme.data.models.Recap
import com.example.recapme.data.models.Category
import com.example.recapme.data.models.RecapStatistics

class HomeViewModel(
    private val settingsDataStore: SettingsDataStore? = null,
    private val recapDataStore: RecapDataStore? = null,
    private val categoryDataStore: CategoryDataStore? = null
) : ViewModel() {
    private val whatsAppProcessor = WhatsAppProcessor()
    private val recapRepository = RecapRepository()
    private val recapService = RecapService(whatsAppProcessor, recapRepository)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow(Category.ALL_CATEGORY_ID)
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val allRecaps: StateFlow<List<Recap>> = recapDataStore?.recapsFlow?.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    ) ?: MutableStateFlow<List<Recap>>(emptyList()).asStateFlow()

    val categories: StateFlow<List<Category>> = categoryDataStore?.categoriesFlow?.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Category.getDefaultCategories()
    ) ?: MutableStateFlow(Category.getDefaultCategories()).asStateFlow()

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

    private val _showDeleteConfirmation = MutableStateFlow<String?>(null)
    val showDeleteConfirmation: StateFlow<String?> = _showDeleteConfirmation.asStateFlow()

    private val _showClearAllConfirmation = MutableStateFlow(false)
    val showClearAllConfirmation: StateFlow<Boolean> = _showClearAllConfirmation.asStateFlow()

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

    fun canAddMoreCategories(): Boolean {
        val currentCategories = categories.value
        val customCategoriesCount = currentCategories.count { !it.isDefault }
        return customCategoriesCount < Category.MAX_CUSTOM_CATEGORIES
    }

    fun addCustomCategory(name: String, color: String = "#4CAF50") {
        if (name.isNotBlank()) {
            val currentCategories = categories.value
            val customCategoriesCount = currentCategories.count { !it.isDefault }

            if (customCategoriesCount >= Category.MAX_CUSTOM_CATEGORIES) {
                _errorMessage.value = "Maximum of ${Category.MAX_CUSTOM_CATEGORIES} custom categories allowed (${Category.MAX_TOTAL_CATEGORIES} total including defaults)"
                hideAddCategoryDialog()
                return
            }

            val newCategory = Category(
                id = name.lowercase().replace(" ", "_"),
                name = name,
                isDefault = false,
                color = color
            )
            viewModelScope.launch {
                categoryDataStore?.addCategory(newCategory)
            }
        }
        hideAddCategoryDialog()
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            // Remove from persistent storage
            categoryDataStore?.deleteCategory(categoryId)

            if (_selectedCategoryId.value == categoryId) {
                _selectedCategoryId.value = Category.ALL_CATEGORY_ID
            }

            // Update all recaps that had this category
            val currentRecaps = allRecaps.value
            val recapsToUpdate = currentRecaps.filter { it.category == categoryId }

            var hasErrors = false
            recapsToUpdate.forEach { recap ->
                val updatedRecap = recap.copy(category = null)
                recapDataStore?.updateRecap(updatedRecap)
                    ?.onFailure { error ->
                        hasErrors = true
                        _errorMessage.value = "Failed to update some recaps: ${error.message}"
                    }
            }

            if (!hasErrors) {
                _statistics.value = calculateStatistics(allRecaps.value)
            }
        }
    }

    fun toggleStar(recapId: String) {
        viewModelScope.launch {
            val currentRecaps = allRecaps.value
            val recapToUpdate = currentRecaps.find { it.id == recapId }
            if (recapToUpdate != null) {
                val updatedRecap = recapToUpdate.copy(isStarred = !recapToUpdate.isStarred)
                recapDataStore?.updateRecap(updatedRecap)
                    ?.onSuccess {
                        _statistics.value = calculateStatistics(allRecaps.value)
                    }
                    ?.onFailure { error ->
                        _errorMessage.value = "Failed to update star status: ${error.message}"
                    }
            }
        }
    }


    fun onFilePickerDismissed() {
        _showFilePicker.value = false
    }

    fun processSelectedFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                android.util.Log.d("HomeViewModel", "Starting file processing for URI: $uri")

                // Get current settings, or use defaults if datastore is not available
                val settings = settingsDataStore?.settingsFlow?.first()
                    ?: com.example.recapme.data.models.AppSettings()

                android.util.Log.d("HomeViewModel", "Settings loaded: $settings")

                recapService.processFileAndGenerateRecap(context, uri, settings)
                    .onSuccess { recap ->
                        android.util.Log.d("HomeViewModel", "Recap generated successfully: ${recap.title}")
                        // Save recap to datastore
                        recapDataStore?.saveRecap(recap)
                            ?.onSuccess {
                                android.util.Log.d("HomeViewModel", "Recap saved successfully")
                                _statistics.value = calculateStatistics(allRecaps.value)
                            }
                            ?.onFailure { saveError ->
                                android.util.Log.e("HomeViewModel", "Failed to save recap", saveError)
                                _errorMessage.value = "Failed to save recap: ${saveError.message}"
                            }
                    }
                    .onFailure { error ->
                        android.util.Log.e("HomeViewModel", "Failed to generate recap", error)
                        _errorMessage.value = "Failed to generate recap: ${error.message}"
                    }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error processing file", e)
                _errorMessage.value = "Error processing file: ${e.message}"
            }

            _isLoading.value = false
            android.util.Log.d("HomeViewModel", "File processing completed")
        }
    }


    fun clearError() {
        _errorMessage.value = null
    }

    fun updateRecapCategory(recapId: String, categoryId: String?) {
        viewModelScope.launch {
            val currentRecaps = allRecaps.value
            val recapToUpdate = currentRecaps.find { it.id == recapId }
            if (recapToUpdate != null) {
                val updatedRecap = recapToUpdate.copy(category = categoryId)
                recapDataStore?.updateRecap(updatedRecap)
                    ?.onSuccess {
                        _statistics.value = calculateStatistics(allRecaps.value)
                        _showCategoryPickerForRecap.value = null
                    }
                    ?.onFailure { error ->
                        _errorMessage.value = "Failed to update category: ${error.message}"
                    }
            } else {
                _showCategoryPickerForRecap.value = null
            }
        }
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

    fun showDeleteConfirmation(recapId: String) {
        _showDeleteConfirmation.value = recapId
    }

    fun hideDeleteConfirmation() {
        _showDeleteConfirmation.value = null
    }

    fun confirmDeleteRecap() {
        val recapId = _showDeleteConfirmation.value
        if (recapId != null) {
            viewModelScope.launch {
                recapDataStore?.deleteRecap(recapId)
                    ?.onSuccess {
                        _statistics.value = calculateStatistics(allRecaps.value)
                        _showDeleteConfirmation.value = null
                    }
                    ?.onFailure { error ->
                        _errorMessage.value = "Failed to delete recap: ${error.message}"
                        _showDeleteConfirmation.value = null
                    }
            }
        }
    }

    fun showClearAllConfirmation() {
        _showClearAllConfirmation.value = true
    }

    fun hideClearAllConfirmation() {
        _showClearAllConfirmation.value = false
    }

    fun confirmClearAllRecaps() {
        viewModelScope.launch {
            recapDataStore?.clearAllRecaps()
                ?.onSuccess {
                    _statistics.value = calculateStatistics(emptyList())
                    _showClearAllConfirmation.value = false
                }
                ?.onFailure { error ->
                    _errorMessage.value = "Failed to clear all recaps: ${error.message}"
                    _showClearAllConfirmation.value = false
                }
        }
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