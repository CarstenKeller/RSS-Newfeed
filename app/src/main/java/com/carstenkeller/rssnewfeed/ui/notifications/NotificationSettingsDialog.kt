package com.carstenkeller.rssnewfeed.ui.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.carstenkeller.rssnewfeed.data.notifications.NotificationPreferences
import com.carstenkeller.rssnewfeed.domain.PREDEFINED_TOPICS

@Composable
fun NotificationSettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var watchedTopics by remember { mutableStateOf(NotificationPreferences.getWatchedTopics(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Themen-Benachrichtigungen") },
        text = {
            Column {
                Text(
                    "Bei neuen Artikeln zu diesen Themen benachrichtigen (grobe " +
                        "Einschätzung anhand Feed-Thema/Kategorie/Stichwörtern):",
                    style = MaterialTheme.typography.bodySmall,
                )
                PREDEFINED_TOPICS.forEach { topic ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = topic in watchedTopics,
                            onCheckedChange = { checked ->
                                val updated = if (checked) watchedTopics + topic else watchedTopics - topic
                                watchedTopics = updated
                                NotificationPreferences.setWatchedTopics(context, updated)
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                        )
                        Text(topic)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fertig") }
        },
    )
}
