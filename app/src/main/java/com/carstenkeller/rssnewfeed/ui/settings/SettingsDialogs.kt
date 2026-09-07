package com.carstenkeller.rssnewfeed.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carstenkeller.rssnewfeed.data.locale.AppLanguage
import com.carstenkeller.rssnewfeed.ui.theme.ThemeMode

@Composable
fun ThemeModeDialog(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Darstellung") },
        text = {
            Column {
                ThemeModeOption(ThemeMode.HELL, "Hell", current, onSelect)
                ThemeModeOption(ThemeMode.DUNKEL, "Dunkel", current, onSelect)
                ThemeModeOption(ThemeMode.SYSTEM, "System (Standard)", current, onSelect)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fertig") } },
    )
}

@Composable
private fun ThemeModeOption(
    mode: ThemeMode,
    label: String,
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = mode == current, onClick = { onSelect(mode) }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = mode == current, onClick = { onSelect(mode) })
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun LanguageDialog(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sprache") },
        text = {
            Column {
                LanguageOption(AppLanguage.DEUTSCH, "Deutsch", current, onSelect)
                LanguageOption(AppLanguage.ENGLISH, "English", current, onSelect)
                Text(
                    "Hinweis: Diese Auswahl stellt derzeit nur die App-interne Locale um " +
                        "(Datumsformate, Tastatur). Die Bildschirmtexte selbst sind noch " +
                        "durchgängig Deutsch fest hinterlegt und werden erst in einer " +
                        "späteren Iteration übersetzt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fertig") } },
    )
}

@Composable
private fun LanguageOption(
    language: AppLanguage,
    label: String,
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = language == current, onClick = { onSelect(language) }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = language == current, onClick = { onSelect(language) })
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
