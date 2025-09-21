package com.example.recapme.data.models

data class Recap(
    val id: String,
    val title: String,
    val participants: List<String>,
    val content: String,
    val category: String? = null,
    val timestamp: Long,
    val isStarred: Boolean
)

data class Category(
    val id: String,
    val name: String,
    val isDefault: Boolean = false,
    val color: String = "#4CAF50"
) {
    companion object {
        fun getDefaultCategories(): List<Category> = listOf(
            Category("personal", "Personal", true, "#4CAF50"),
            Category("work", "Work", true, "#2196F3")
        )

        const val ALL_CATEGORY_ID = "all"
        const val MAX_TOTAL_CATEGORIES = 5
        const val MAX_CUSTOM_CATEGORIES = 3
    }
}

data class RecapStatistics(
    val thisWeek: Int,
    val starred: Int,
    val total: Int
)