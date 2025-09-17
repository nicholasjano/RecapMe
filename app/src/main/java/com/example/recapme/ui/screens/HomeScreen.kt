package com.example.recapme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recapme.data.models.Recap
import com.example.recapme.data.models.Category
import com.example.recapme.data.models.RecapStatistics
import com.example.recapme.ui.components.FilePicker
import com.example.recapme.ui.components.AddCategoryDialog
import com.example.recapme.ui.theme.*
import com.example.recapme.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val recaps by viewModel.filteredRecaps.collectAsState(initial = emptyList())
    val statistics by viewModel.statistics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showFilePicker by viewModel.showFilePicker.collectAsState()
    val showAddCategoryDialog by viewModel.showAddCategoryDialog.collectAsState()
    val context = LocalContext.current

    FilePicker(
        onFileSelected = { uri -> viewModel.processSelectedFile(context, uri) },
        trigger = showFilePicker,
        onTriggerConsumed = viewModel::onFilePickerDismissed
    )

    errorMessage?.let { message ->
        LaunchedEffect(message) {
            viewModel.clearError()
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = viewModel::hideAddCategoryDialog,
            onConfirm = { name, color ->
                viewModel.addCustomCategory(name, color)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightGray)
        ) {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MediumGreen,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = "My Recaps",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO: Add filter functionality */ }
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarGreen
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    placeholder = {
                        Text(
                            "Search your recaps...",
                            color = MediumGray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MediumGray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SearchBarGray,
                        unfocusedContainerColor = SearchBarGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = { viewModel.selectCategory(Category.ALL_CATEGORY_ID) },
                            label = { Text("All") },
                            selected = selectedCategoryId == Category.ALL_CATEGORY_ID,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkGreen,
                                selectedLabelColor = White,
                                containerColor = Color.Transparent,
                                labelColor = DarkGray
                            ),
                            border = if (selectedCategoryId == Category.ALL_CATEGORY_ID) {
                                null
                            } else {
                                FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = MediumGray,
                                    selectedBorderColor = DarkGreen,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.dp
                                )
                            }
                        )
                    }

                    items(categories) { category ->
                        FilterChip(
                            onClick = { viewModel.selectCategory(category.id) },
                            label = { Text(category.name) },
                            selected = selectedCategoryId == category.id,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(android.graphics.Color.parseColor(category.color)),
                                selectedLabelColor = White,
                                containerColor = Color.Transparent,
                                labelColor = DarkGray
                            ),
                            border = if (selectedCategoryId == category.id) {
                                null
                            } else {
                                FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = MediumGray,
                                    selectedBorderColor = Color(android.graphics.Color.parseColor(category.color)),
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.dp
                                )
                            }
                        )
                    }

                    item {
                        FilterChip(
                            onClick = { viewModel.showAddCategoryDialog() },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add category",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Add")
                                }
                            },
                            selected = false,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                labelColor = DarkGreen
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = DarkGreen,
                                selectedBorderColor = DarkGreen,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CalendarToday,
                        iconBackgroundColor = DarkGreen,
                        label = "This Week",
                        value = statistics.thisWeek.toString()
                    )
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Star,
                        iconBackgroundColor = WarningOrange,
                        label = "Starred",
                        value = statistics.starred.toString()
                    )
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FolderOpen,
                        iconBackgroundColor = MediumGray,
                        label = "Total",
                        value = statistics.total.toString()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recaps) { recap ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = recap.title,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkGray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = recap.content.take(100) + if (recap.content.length > 100) "..." else "",
                                            fontSize = 14.sp,
                                            color = MediumGray
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleStar(recap.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (recap.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = if (recap.isStarred) "Remove from favorites" else "Add to favorites",
                                            tint = if (recap.isStarred) WarningOrange else MediumGray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        recap.participants.take(3).forEachIndexed { index, participant ->
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .offset(x = (-4 * index).dp)
                                                    .clip(CircleShape)
                                                    .background(DarkGreen),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = participant.firstOrNull()?.uppercase() ?: "?",
                                                    color = White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        if (recap.participants.size > 3) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "+${recap.participants.size - 3}",
                                                fontSize = 12.sp,
                                                color = MediumGray
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = run {
                                                    val category = categories.find { it.id == recap.category }
                                                    if (category != null) {
                                                        Color(android.graphics.Color.parseColor(category.color))
                                                    } else {
                                                        MediumGray
                                                    }
                                                }
                                            )
                                        ) {
                                            Text(
                                                text = run {
                                                    val category = categories.find { it.id == recap.category }
                                                    category?.name ?: recap.category.replaceFirstChar { it.uppercase() }
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 10.sp,
                                                color = White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = formatTimestamp(recap.timestamp),
                                            fontSize = 12.sp,
                                            color = MediumGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = DarkGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Processing WhatsApp file...",
                            color = DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackgroundColor: Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconBackgroundColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                fontSize = 12.sp,
                color = MediumGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        hours < 1 -> "Just now"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "Yesterday"
        days < 7 -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}