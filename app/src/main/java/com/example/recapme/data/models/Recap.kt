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
            Category("work", "Work", true, "#2E7D32"),
            Category("personal", "Personal", true, "#388E3C")
        )

        const val ALL_CATEGORY_ID = "all"
        const val NO_CATEGORY_ID = "no_category"
    }
}

data class RecapStatistics(
    val thisWeek: Int,
    val starred: Int,
    val total: Int
)