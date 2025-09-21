package com.example.recapme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recapme.data.models.*
import com.example.recapme.data.SettingsDataStore
import com.example.recapme.data.RecapDataStore
import com.example.recapme.ui.theme.*
import com.example.recapme.ui.viewmodels.SettingsViewModel
import com.example.recapme.ui.viewmodels.SettingsViewModelFactory
import com.example.recapme.ui.viewmodels.HomeViewModel
import com.example.recapme.ui.components.AddCategoryDialog
import com.example.recapme.ui.components.ExportSelectionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    homeViewModel: HomeViewModel = run {
        val context = LocalContext.current
        val settingsDataStore = SettingsDataStore(context)
        val recapDataStore = RecapDataStore(context)
        viewModel { HomeViewModel(settingsDataStore, recapDataStore) }
    }
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )
    val settings by viewModel.settings.collectAsState()
    val categories by homeViewModel.categories.collectAsState()
    val showAddCategoryDialog by homeViewModel.showAddCategoryDialog.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val availableRecaps by viewModel.availableRecaps.collectAsState()

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = homeViewModel::hideAddCategoryDialog,
            onConfirm = { name, color ->
                homeViewModel.addCustomCategory(name, color)
            }
        )
    }

    if (showExportDialog) {
        ExportSelectionDialog(
            recaps = availableRecaps,
            onDismiss = viewModel::hideExportDialog,
            onExport = { selectedRecaps ->
                viewModel.exportSelectedRecaps(selectedRecaps)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkGreen
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                SettingsSection(title = "Summarization Settings") {
                    DropdownSetting(
                        title = "Recap Time Window",
                        subtitle = "How far back to analyze messages",
                        selectedValue = settings.recapTimeWindow.displayName,
                        options = TimeWindow.entries.map { it.displayName },
                        onValueSelected = { displayName ->
                            val timeWindow = TimeWindow.entries.find { it.displayName == displayName }
                            timeWindow?.let { viewModel.updateTimeWindow(it) }
                        }
                    )

                    DropdownSetting(
                        title = "Summary Style",
                        subtitle = "How detailed should the summaries be",
                        selectedValue = settings.summaryStyle.displayName,
                        options = SummaryStyle.entries.map { it.displayName },
                        onValueSelected = { displayName ->
                            val style = SummaryStyle.entries.find { it.displayName == displayName }
                            style?.let { viewModel.updateSummaryStyle(it) }
                        }
                    )
                }

                SettingsSection(title = "Data") {
                    ClickableSetting(
                        title = "Export Summaries",
                        subtitle = "Export your recaps as individual .txt files in a zip archive",
                        onClick = { viewModel.exportSummaries() }
                    )
                }


                SettingsSection(title = "Categories") {
                    Column {
                        Text(
                            text = "Manage your custom categories",
                            fontSize = 14.sp,
                            color = MediumGray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        categories.forEach { category ->
                            CategoryManagementItem(
                                category = category,
                                onDelete = if (!category.isDefault) {
                                    { homeViewModel.deleteCategory(category.id) }
                                } else null
                            )
                        }

                        ClickableSetting(
                            title = "Add New Category",
                            subtitle = "Create a custom category for your recaps",
                            onClick = { homeViewModel.showAddCategoryDialog() }
                        )
                    }
                }


                SettingsSection(title = "General") {
                    DropdownSetting(
                        title = "Theme",
                        subtitle = "App appearance",
                        selectedValue = settings.theme.displayName,
                        options = AppTheme.entries.map { it.displayName },
                        onValueSelected = { displayName ->
                            val theme = AppTheme.entries.find { it.displayName == displayName }
                            theme?.let { viewModel.updateTheme(it) }
                        }
                    )

                    ExpandableSetting(
                        title = "About",
                        subtitle = "App version and information"
                    ) {
                        AboutContent()
                    }
                }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun DropdownSetting(
    title: String,
    subtitle: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = DarkGray
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MediumGray
            )
        }

        Box {
            Row(
                modifier = Modifier.clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValue,
                    fontSize = 14.sp,
                    color = DarkGreen,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = DarkGreen
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    HorizontalDivider(
        color = LightGray,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ClickableSetting(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = DarkGray
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MediumGray
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = DarkGreen
        )
    }
    HorizontalDivider(
        color = LightGray,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ExpandableSetting(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkGray
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MediumGray
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = DarkGreen
            )
        }

        if (expanded) {
            content()
        }

        HorizontalDivider(
            color = LightGray,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun AboutContent() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "RecapMe",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen
        )
        Text(
            text = "Version 1.0.0",
            fontSize = 14.sp,
            color = MediumGray
        )
        Text(
            text = "An offline-first app for generating AI summaries of WhatsApp conversations.",
            fontSize = 14.sp,
            color = DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Developed with privacy in mind - all processing happens locally on your device.",
            fontSize = 12.sp,
            color = MediumGray
        )
    }
}

@Composable
fun CategoryManagementItem(
    category: Category,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color(category.color.toColorInt()),
                        shape = CircleShape
                    )
            )

            Column {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkGray
                )
                if (category.isDefault) {
                    Text(
                        text = "Default category",
                        fontSize = 12.sp,
                        color = MediumGray
                    )
                }
            }
        }

        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete category",
                    tint = ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    HorizontalDivider(
        color = LightGray,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}