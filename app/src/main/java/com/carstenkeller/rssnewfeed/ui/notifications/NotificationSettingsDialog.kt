package com.carstenkeller.rssnewfeed.ui.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carstenkeller.rssnewfeed.R
import com.carstenkeller.rssnewfeed.data.filterpresets.FilterPresetsStore
import com.carstenkeller.rssnewfeed.data.notifications.NotificationPreferences
import com.carstenkeller.rssnewfeed.domain.PREDEFINED_TOPICS

@Composable
fun NotificationSettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var watchedTopics by remember { mutableStateOf(NotificationPreferences.getWatchedTopics(context)) }
    var watchedPresetIds by remember { mutableStateOf(NotificationPreferences.getWatchedPresetIds(context)) }
    val presets = remember { FilterPresetsStore.getAll(context) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.menu_notifications)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    stringResource(R.string.notification_topics_hint),
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
                                if (checked) requestPermissionIfNeeded()
                            },
                        )
                        Text(topic)
                    }
                }

                if (presets.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        stringResource(R.string.notification_presets_hint),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    presets.forEach { preset ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Checkbox(
                                checked = preset.id in watchedPresetIds,
                                onCheckedChange = { checked ->
                                    val updated = if (checked) watchedPresetIds + preset.id else watchedPresetIds - preset.id
                                    watchedPresetIds = updated
                                    NotificationPreferences.setWatchedPresetIds(context, updated)
                                    if (checked) requestPermissionIfNeeded()
                                },
                            )
                            Text(preset.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_done)) }
        },
    )
}
