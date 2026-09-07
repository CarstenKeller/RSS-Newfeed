package com.carstenkeller.rssnewfeed.ui.articlelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carstenkeller.rssnewfeed.domain.PREDEFINED_TOPICS
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    state: ArticleListUiState,
    onDismiss: () -> Unit,
    onSelectFeed: (Long?) -> Unit,
    onToggleIncludedTopic: (String) -> Unit,
    onToggleExcludedTopic: (String) -> Unit,
    onSelectLanguage: (String?) -> Unit,
    onSetDateRange: (Long?, Long?) -> Unit,
    onReset: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val languages = state.feeds.mapNotNull { it.language }.distinct().sorted()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Filter", style = MaterialTheme.typography.titleLarge)

            SectionLabel("Herausgeber")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.filter.feedId == null,
                        onClick = { onSelectFeed(null) },
                        label = { Text("Alle") },
                    )
                }
                items(state.feeds) { feed ->
                    FilterChip(
                        selected = state.filter.feedId == feed.id,
                        onClick = { onSelectFeed(feed.id) },
                        label = { Text(feed.title) },
                    )
                }
            }

            SectionLabel("Themen anzeigen (leer = alle)")
            TopicChipRow(
                selected = state.filter.includedTopics,
                onToggle = onToggleIncludedTopic,
            )

            SectionLabel("Themen ausschließen")
            TopicChipRow(
                selected = state.filter.excludedTopics,
                onToggle = onToggleExcludedTopic,
            )

            if (languages.isNotEmpty()) {
                SectionLabel("Sprache")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.filter.language == null,
                            onClick = { onSelectLanguage(null) },
                            label = { Text("Alle") },
                        )
                    }
                    items(languages) { language ->
                        FilterChip(
                            selected = state.filter.language == language,
                            onClick = { onSelectLanguage(language) },
                            label = { Text(language) },
                        )
                    }
                }
            }

            SectionLabel("Zeitraum")
            DateRangeRow(
                fromMillis = state.filter.dateFromMillis,
                toMillis = state.filter.dateToMillis,
                onSetDateRange = onSetDateRange,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onReset) { Text("Zurücksetzen") }
                Button(onClick = onDismiss) { Text("Fertig") }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun TopicChipRow(selected: Set<String>, onToggle: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(PREDEFINED_TOPICS) { topic ->
            FilterChip(
                selected = topic in selected,
                onClick = { onToggle(topic) },
                label = { Text(topic) },
            )
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dateFormatter)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeRow(
    fromMillis: Long?,
    toMillis: Long?,
    onSetDateRange: (Long?, Long?) -> Unit,
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { showFromPicker = true }) {
            Text(fromMillis?.let { "Von: ${formatDate(it)}" } ?: "Von")
        }
        OutlinedButton(onClick = { showToPicker = true }) {
            Text(toMillis?.let { "Bis: ${formatDate(it)}" } ?: "Bis")
        }
        if (fromMillis != null || toMillis != null) {
            TextButton(onClick = { onSetDateRange(null, null) }) { Text("Zeitraum löschen") }
        }
    }

    if (showFromPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = fromMillis)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onSetDateRange(pickerState.selectedDateMillis, toMillis)
                    showFromPicker = false
                }) { Text("Übernehmen") }
            },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Abbrechen") } },
        ) { DatePicker(state = pickerState) }
    }

    if (showToPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = toMillis)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onSetDateRange(fromMillis, pickerState.selectedDateMillis)
                    showToPicker = false
                }) { Text("Übernehmen") }
            },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Abbrechen") } },
        ) { DatePicker(state = pickerState) }
    }
}
