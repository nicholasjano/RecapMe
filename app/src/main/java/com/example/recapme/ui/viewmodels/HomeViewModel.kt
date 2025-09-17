package com.example.recapme.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.example.recapme.data.WhatsAppProcessor
import com.example.recapme.data.models.Recap
import com.example.recapme.data.models.Category
import com.example.recapme.data.models.RecapStatistics

class HomeViewModel : ViewModel() {
    private val whatsAppProcessor = WhatsAppProcessor()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow(Category.ALL_CATEGORY_ID)
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _allRecaps = MutableStateFlow(getSampleRecaps())
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

            val matchesCategory = categoryId == Category.ALL_CATEGORY_ID || recap.category == categoryId

            matchesSearch && matchesCategory
        }
    }

    private val _statistics = MutableStateFlow(calculateStatistics(getSampleRecaps()))
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
                recap.copy(category = "personal")
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

            whatsAppProcessor.processWhatsAppFile(context, uri)
                .onSuccess { messages ->
                    // Create placeholder recap - LLM will generate actual content later
                    val participants = messages.map { it.sender }.distinct()
                    val placeholderRecap = Recap(
                        id = java.util.UUID.randomUUID().toString(),
                        title = "WhatsApp Chat (${participants.size} participants)", // Placeholder title
                        participants = participants,
                        content = "Processing ${messages.size} messages...", // Placeholder content
                        category = "personal", // Default category, LLM will determine actual category
                        timestamp = System.currentTimeMillis(),
                        isStarred = false
                    )

                    val currentRecaps = _allRecaps.value.toMutableList()
                    currentRecaps.add(0, placeholderRecap)
                    _allRecaps.value = currentRecaps
                    _statistics.value = calculateStatistics(currentRecaps)
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to process file: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
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

    private fun getSampleRecaps(): List<Recap> {
        val now = System.currentTimeMillis()
        return listOf(
            Recap(
                id = "1",
                title = "Team Standup Discussion",
                participants = listOf("Alice", "Bob", "Charlie"),
                content = "Discussed sprint progress, blockers, and upcoming deadlines. Alice mentioned the API integration is 80% complete. Bob needs help with the authentication module.",
                category = "work",
                timestamp = now - (2 * 60 * 60 * 1000L), // 2 hours ago
                isStarred = true
            ),
            Recap(
                id = "2",
                title = "Weekend Plans with Friends",
                participants = listOf("Sarah", "Mike", "Jenny", "Dave"),
                content = "Planning a hiking trip for this weekend. Discussed meeting point, what to bring, and weather conditions. Everyone agreed on the 8 AM start time.",
                category = "personal",
                timestamp = now - (5 * 60 * 60 * 1000L), // 5 hours ago
                isStarred = false
            ),
            Recap(
                id = "3",
                title = "Client Project Requirements",
                participants = listOf("John", "Maria"),
                content = "Client wants to add new features to the mobile app. Discussed timeline, budget constraints, and technical feasibility. Need to prepare a detailed proposal by Friday.",
                category = "work",
                timestamp = now - (1 * 24 * 60 * 60 * 1000L), // 1 day ago
                isStarred = true
            ),
            Recap(
                id = "4",
                title = "Family Dinner Organization",
                participants = listOf("Mom", "Dad", "Sister"),
                content = "Planning family dinner for next Sunday. Mom will cook the main course, I'll bring dessert, and sister will handle appetizers. Dad will set up the table.",
                category = "personal",
                timestamp = now - (2 * 24 * 60 * 60 * 1000L), // 2 days ago
                isStarred = false
            ),
            Recap(
                id = "5",
                title = "Bug Fix Coordination",
                participants = listOf("Tech Lead", "QA Team", "DevOps"),
                content = "Critical bug found in production. Coordinating hotfix deployment. QA will test the patch, DevOps will handle the deployment during low-traffic hours.",
                category = "work",
                timestamp = now - (3 * 24 * 60 * 60 * 1000L), // 3 days ago
                isStarred = false
            ),
            Recap(
                id = "6",
                title = "Book Club Discussion",
                participants = listOf("Emma", "James", "Lily", "Marcus"),
                content = "Great discussion about this month's book selection. Everyone loved the plot twists and character development. Next month we're reading a sci-fi novel that James recommended.",
                category = "personal",
                timestamp = now - (4 * 24 * 60 * 60 * 1000L), // 4 days ago
                isStarred = true
            )
        )
    }
}