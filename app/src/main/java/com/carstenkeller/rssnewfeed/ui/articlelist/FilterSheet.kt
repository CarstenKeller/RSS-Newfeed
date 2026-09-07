@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.carstenkeller.rssnewfeed.ui.articlelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carstenkeller.rssnewfeed.R
import com.carstenkeller.rssnewfeed.data.filterpresets.FilterPreset
import com.carstenkeller.rssnewfeed.domain.PREDEFINED_TOPICS
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    state: ArticleListUiState,
    onDismiss: () -> Unit,
    onToggleFeed: (Long) -> Unit,
    onClearFeeds: () -> Unit,
    onToggleIncludedTopic: (String) -> Unit,
    onToggleExcludedTopic: (String) -> Unit,
    onSelectLanguage: (String?) -> Unit,
    onSetDateRange: (Long?, Long?) -> Unit,
    onSelectSourceLinkFilter: (SourceLinkFilter) -> Unit,
    onReset: () -> Unit,
    onApplyPreset: (FilterPreset) -> Unit,
    onSavePreset: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val languages = state.feeds.mapNotNull { it.language }.distinct().sorted()
    var saveDialogOpen by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.filter_sheet_title), style = MaterialTheme.typography.titleLarge)

            SectionLabel(stringResource(R.string.presets_section_title))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.presets.forEach { preset ->
                    PresetChip(
                        preset = preset,
                        onApply = { onApplyPreset(preset) },
                        onDelete = { onDeletePreset(preset.id) },
                    )
                }
                TextButton(onClick = { saveDialogOpen = true }) { Text(stringResource(R.string.preset_save_button)) }
            }
            if (state.presets.isEmpty()) {
                Text(
                    stringResource(R.string.presets_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionLabel(stringResource(R.string.section_publisher))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.filter.feedIds.isEmpty(),
                    onClick = onClearFeeds,
                    label = { Text(stringResource(R.string.label_all)) },
                )
                state.feeds.forEach { feed ->
                    FilterChip(
                        selected = feed.id in state.filter.feedIds,
                        onClick = { onToggleFeed(feed.id) },
                        label = { Text(feed.title) },
                    )
                }
            }

            SectionLabel(stringResource(R.string.section_topics_include))
            TopicChipRow(
                selected = state.filter.includedTopics,
                onToggle = onToggleIncludedTopic,
            )

            SectionLabel(stringResource(R.string.section_topics_exclude))
            TopicChipRow(
                selected = state.filter.excludedTopics,
                onToggle = onToggleExcludedTopic,
            )

            if (languages.isNotEmpty()) {
                SectionLabel(stringResource(R.string.section_language))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.filter.language == null,
                        onClick = { onSelectLanguage(null) },
                        label = { Text(stringResource(R.string.label_all)) },
                    )
                    languages.forEach { language ->
                        FilterChip(
                            selected = state.filter.language == language,
                            onClick = { onSelectLanguage(language) },
                            label = { Text(language) },
                        )
                    }
                }
            }

            SectionLabel(stringResource(R.string.section_date_range))
            DateRangeRow(
                fromMillis = state.filter.dateFromMillis,
                toMillis = state.filter.dateToMillis,
                onSetDateRange = onSetDateRange,
            )

            SectionLabel(stringResource(R.string.section_source_type))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.filter.sourceLinkFilter == SourceLinkFilter.ALLE,
                    onClick = { onSelectSourceLinkFilter(SourceLinkFilter.ALLE) },
                    label = { Text(stringResource(R.string.label_all)) },
                )
                FilterChip(
                    selected = state.filter.sourceLinkFilter == SourceLinkFilter.MIT_LINK,
                    onClick = { onSelectSourceLinkFilter(SourceLinkFilter.MIT_LINK) },
                    label = { Text(stringResource(R.string.source_type_with_link)) },
                )
                FilterChip(
                    selected = state.filter.sourceLinkFilter == SourceLinkFilter.NUR_KURZNACHRICHT,
                    onClick = { onSelectSourceLinkFilter(SourceLinkFilter.NUR_KURZNACHRICHT) },
                    label = { Text(stringResource(R.string.source_type_short_note)) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onReset) { Text(stringResource(R.string.action_reset)) }
                Button(onClick = onDismiss) { Text(stringResource(R.string.action_done)) }
            }
        }
    }

    if (saveDialogOpen) {
        SavePresetDialog(
            onDismiss = { saveDialogOpen = false },
            onConfirm = { name ->
                onSavePreset(name)
                saveDialogOpen = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetChip(preset: FilterPreset, onApply: () -> Unit, onDelete: () -> Unit) {
    InputChip(
        selected = false,
        onClick = onApply,
        label = { Text(preset.name) },
        trailingIcon = {
            // A nested clickable intercepts the tap before it reaches the chip's own
            // onClick, so the icon can delete while the rest of the chip applies.
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.preset_delete_cd, preset.name),
                modifier = Modifier
                    .size(InputChipDefaults.IconSize)
                    .clickable(onClick = onDelete),
            )
        },
    )
}

@Composable
private fun SavePresetDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.save_preset_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.save_preset_name_placeholder)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
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
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PREDEFINED_TOPICS.forEach { topic ->
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

    val fromEmptyLabel = stringResource(R.string.date_from_empty)
    val toEmptyLabel = stringResource(R.string.date_to_empty)
    val fromLabel = fromMillis?.let { stringResource(R.string.date_from_with_value, formatDate(it)) } ?: fromEmptyLabel
    val toLabel = toMillis?.let { stringResource(R.string.date_to_with_value, formatDate(it)) } ?: toEmptyLabel

    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        DateFieldWithClear(
            label = fromLabel,
            showClear = fromMillis != null,
            onClick = { showFromPicker = true },
            onClear = { onSetDateRange(null, toMillis) },
        )
        DateFieldWithClear(
            label = toLabel,
            showClear = toMillis != null,
            onClick = { showToPicker = true },
            onClear = { onSetDateRange(fromMillis, null) },
        )
    }

    if (showFromPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = fromMillis)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onSetDateRange(pickerState.selectedDateMillis, toMillis)
                    showFromPicker = false
                }) { Text(stringResource(R.string.action_apply)) }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
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
                }) { Text(stringResource(R.string.action_apply)) }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun DateFieldWithClear(
    label: String,
    showClear: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        OutlinedButton(onClick = onClick) { Text(label) }
        if (showClear) {
            IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.clear_field_cd, label))
            }
        }
    }
}
