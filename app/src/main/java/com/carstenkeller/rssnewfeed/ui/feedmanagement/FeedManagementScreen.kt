package com.carstenkeller.rssnewfeed.ui.feedmanagement

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.carstenkeller.rssnewfeed.R
import com.carstenkeller.rssnewfeed.data.db.FeedEntity
import com.carstenkeller.rssnewfeed.domain.PREDEFINED_TOPICS
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedManagementScreen(
    viewModel: FeedManagementViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var newFeedUrl by remember { mutableStateOf("") }
    var feedToEdit by remember { mutableStateOf<FeedEntity?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val exportChooserTitle = stringResource(R.string.export_chooser_title)
    val noTopicLabel = stringResource(R.string.topic_none)

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { stream -> viewModel.importOpml(stream) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_manage_feeds)) },
                colors = com.carstenkeller.rssnewfeed.ui.theme.brandedTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(stringResource(R.string.opml_import_tooltip)) } },
                        state = rememberTooltipState(),
                    ) {
                        IconButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                            Icon(Icons.Filled.FileUpload, contentDescription = stringResource(R.string.opml_import_cd))
                        }
                    }
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(stringResource(R.string.opml_export_tooltip)) } },
                        state = rememberTooltipState(),
                    ) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                val opml = viewModel.buildOpmlString()
                                val dir = File(context.cacheDir, "opml").apply { mkdirs() }
                                val file = File(dir, "rss-newsfeed-feeds.opml")
                                file.writeText(opml)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/x-opml+xml"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, exportChooserTitle))
                            }
                        }) {
                            Icon(Icons.Filled.FileDownload, contentDescription = stringResource(R.string.opml_export_cd))
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = newFeedUrl,
                    onValueChange = { newFeedUrl = it },
                    label = { Text(stringResource(R.string.feed_url_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                Button(
                    onClick = {
                        viewModel.addFeed(newFeedUrl)
                        newFeedUrl = ""
                    },
                    enabled = !state.isAdding && newFeedUrl.isNotBlank(),
                ) {
                    Text(stringResource(R.string.action_add))
                }
            }

            if (state.isAdding) {
                CircularProgressIndicator(modifier = Modifier.padding(horizontal = 16.dp))
            }

            state.errorMessage?.let {
                Text(
                    text = feedStatusMessageText(it),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            state.infoMessage?.let {
                Text(
                    text = feedStatusMessageText(it),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            HorizontalDivider()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.feeds, key = { it.id }) { feed ->
                    ListItem(
                        headlineContent = { Text(feed.title) },
                        supportingContent = {
                            Column {
                                Text(feed.url, maxLines = 1)
                                AssistChip(
                                    onClick = { feedToEdit = feed },
                                    label = { Text(feed.topicTag ?: noTopicLabel) },
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { feedToEdit = feed }) {
                                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
                                }
                                IconButton(onClick = { viewModel.removeFeed(feed) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_remove))
                                }
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    feedToEdit?.let { feed ->
        EditFeedDialog(
            feed = feed,
            onDismiss = { feedToEdit = null },
            onConfirm = { newTitle, topic ->
                viewModel.renameFeed(feed, newTitle)
                viewModel.setFeedTopic(feed, topic)
                feedToEdit = null
            },
        )
    }
}

@Composable
private fun feedStatusMessageText(message: FeedStatusMessage): String = when (message) {
    is FeedStatusMessage.LoadFailed -> stringResource(R.string.feed_load_failed, message.detail ?: "")
    is FeedStatusMessage.NoFeedsInFile -> stringResource(R.string.opml_no_feeds_found)
    is FeedStatusMessage.ImportFailed -> stringResource(R.string.opml_import_failed, message.detail ?: "")
    is FeedStatusMessage.ImportSummary -> {
        val added = stringResource(R.string.opml_imported_count, message.added)
        val skipped = if (message.skipped > 0) stringResource(R.string.opml_skipped_suffix, message.skipped) else ""
        val failed = if (message.failed > 0) stringResource(R.string.opml_failed_suffix, message.failed) else ""
        "$added$skipped$failed."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditFeedDialog(
    feed: FeedEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit,
) {
    var title by remember { mutableStateOf(feed.title) }
    var topic by remember { mutableStateOf(feed.topicTag) }
    var topicMenuOpen by remember { mutableStateOf(false) }
    val noTopicLabel = stringResource(R.string.topic_none)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_feed_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.label_name)) },
                    singleLine = true,
                )
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(stringResource(R.string.label_topic), style = MaterialTheme.typography.labelMedium)
                    AssistChip(
                        onClick = { topicMenuOpen = true },
                        label = { Text(topic ?: noTopicLabel) },
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    DropdownMenu(expanded = topicMenuOpen, onDismissRequest = { topicMenuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(noTopicLabel) },
                            onClick = { topic = null; topicMenuOpen = false },
                        )
                        PREDEFINED_TOPICS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { topic = option; topicMenuOpen = false },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, topic) }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
