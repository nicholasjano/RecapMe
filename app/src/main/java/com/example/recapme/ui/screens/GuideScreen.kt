package com.example.recapme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recapme.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Guide",
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
            GuideSection(
                title = "📤 How to Export a WhatsApp Chat to Google Drive",
                subtitle = "Follow these steps to export your WhatsApp chat so you can generate a recap in RecapMe."
            ) {
                GuideStep(
                    stepNumber = "1",
                    title = "Open the Chat You Want to Export",
                    description = "Open WhatsApp on your phone.\n\nTap the conversation (individual or group) you want to recap."
                )

                GuideStep(
                    stepNumber = "2",
                    title = "Export the Chat",
                    description = "On Android: Tap the three dots (⋮ > More > Export chat).\n\nOn iPhone: Tap the contact or group name at the top → scroll down → Export Chat."
                )

                GuideStep(
                    stepNumber = "3",
                    title = "Choose Without Media",
                    description = "When WhatsApp asks:\n\nSelect Without Media ✅\n\n(This keeps the file size small by only exporting text messages)."
                )

                GuideStep(
                    stepNumber = "4",
                    title = "Save to Google Drive",
                    description = "Choose Google Drive as the destination.\n\nPick the account/folder where you'd like the file saved.\n\nTap Save."
                )

                GuideStep(
                    stepNumber = "5",
                    title = "Import the File into RecapMe",
                    description = "Open RecapMe.\n\nFrom the Home page, tap the ➕ icon in the top-right corner.\n\nFind the exported file (e.g., WhatsApp Chat - [contact name].txt) on Google Drive.\n\nSelect it, and RecapMe will download the file and generate your recap automatically."
                )
            }

            ImportantNotesSection()
        }
    }
}

@Composable
fun GuideSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MediumGray,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun GuideStep(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.size(32.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = DarkGray,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ImportantNotesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = WarningOrange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "⚠️ Important Notes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )
            }

            ImportantNote(
                title = "File size limit:",
                description = "RecapMe supports files up to 20 MB."
            )

            ImportantNote(
                title = "Always select \"Without Media.\"",
                description = "Media exports make files too large."
            )

            ImportantNote(
                title = "If your chat is still over 20 MB,",
                description = "you'll need to split/truncate the file manually (e.g., by removing older messages in a text editor) before uploading."
            )
        }
    }
}

@Composable
fun ImportantNote(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            fontSize = 14.sp,
            color = WarningOrange,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkGray
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = DarkGray,
                lineHeight = 18.sp
            )
        }
    }
}