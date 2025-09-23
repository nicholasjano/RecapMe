package com.nicholasjano.recapme.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nicholasjano.recapme.data.models.Recap
import com.nicholasjano.recapme.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExportSelectionDialog(
    recaps: List<Recap>,
    onDismiss: () -> Unit,
    onExport: (List<Recap>) -> Unit
) {
    var selectedRecaps by remember { mutableStateOf(setOf<String>()) }

    val allSelected = selectedRecaps.size == recaps.size
    val noneSelected = selectedRecaps.isEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Recaps to Export",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MediumGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selection summary and controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedRecaps.size} of ${recaps.size} selected",
                        fontSize = 14.sp,
                        color = MediumGray
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                selectedRecaps = if (allSelected) {
                                    emptySet()
                                } else {
                                    recaps.map { it.id }.toSet()
                                }
                            }
                        ) {
                            Text(
                                text = if (allSelected) "Deselect All" else "Select All",
                                color = DarkGreen,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recap list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recaps) { recap ->
                        RecapSelectionItem(
                            recap = recap,
                            isSelected = selectedRecaps.contains(recap.id),
                            onSelectionChanged = { isSelected ->
                                selectedRecaps = if (isSelected) {
                                    selectedRecaps + recap.id
                                } else {
                                    selectedRecaps - recap.id
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MediumGray
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val selectedRecapObjects = recaps.filter { selectedRecaps.contains(it.id) }
                            onExport(selectedRecapObjects)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !noneSelected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGreen,
                            contentColor = White
                        )
                    ) {
                        Text("Export (${selectedRecaps.size})")
                    }
                }
            }
        }
    }
}

@Composable
private fun RecapSelectionItem(
    recap: Recap,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkGreen.copy(alpha = 0.1f) else LightGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onSelectionChanged(!isSelected) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = if (isSelected) "Deselect" else "Select",
                    tint = if (isSelected) DarkGreen else MediumGray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recap.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(recap.timestamp),
                        fontSize = 12.sp,
                        color = MediumGray
                    )

                    if (recap.participants.isNotEmpty()) {
                        Text(
                            text = "${recap.participants.size} participants",
                            fontSize = 12.sp,
                            color = MediumGray
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}