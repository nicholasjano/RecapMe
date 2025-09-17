package com.example.recapme.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun FilePicker(
    onFileSelected: (Uri) -> Unit,
    trigger: Boolean,
    onTriggerConsumed: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onFileSelected(it) }
    }

    LaunchedEffect(trigger) {
        if (trigger) {
            launcher.launch("application/zip")
            onTriggerConsumed()
        }
    }
}