package com.nicholasjano.recapme.ui.components

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
    val context = androidx.compose.ui.platform.LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Take persistable URI permission for the selected file
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Continue anyway as some URIs might not support persistent permissions
            }
            onFileSelected(uri)
        }
    }

    LaunchedEffect(trigger) {
        if (trigger) {
            // Use OpenDocument instead of GetContent for better access
            // Allow ZIP files and any other files as fallback
            try {
                launcher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*"))
            } catch (_: Exception) {
                // Error launching file picker
            }
            onTriggerConsumed()
        }
    }
}