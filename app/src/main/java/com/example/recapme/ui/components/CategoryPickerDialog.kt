package com.example.recapme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt
import com.example.recapme.data.models.Category
import com.example.recapme.ui.theme.*

@Composable
fun CategoryPickerDialog(
    currentCategory: String?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Category",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoryOption(
                            name = "No Category",
                            color = MediumGray,
                            isSelected = currentCategory == null,
                            onClick = { onCategorySelected(null) }
                        )
                    }

                    items(categories) { category ->
                        CategoryOption(
                            name = category.name,
                            color = Color(category.color.toColorInt()),
                            isSelected = currentCategory == category.id,
                            onClick = { onCategorySelected(category.id) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MediumGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryOption(
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) LightGray else White
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, DarkGreen)
        } else {
            BorderStroke(1.dp, LightGray)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Text(
                text = name,
                fontSize = 16.sp,
                color = DarkGray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}