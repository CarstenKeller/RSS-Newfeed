package com.carstenkeller.rssnewfeed.ui.feedmanagement

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carstenkeller.rssnewfeed.data.db.FeedEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedManagementScreen(
    viewModel: FeedManagementViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var newFeedUrl by remember { mutableStateOf("") }
    var feedToRename by remember { mutableStateOf<FeedEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feeds verwalten") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
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
                    label = { Text("RSS-Feed-URL") },
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
                    Text("Hinzufügen")
                }
            }

            if (state.isAdding) {
                CircularProgressIndicator(modifier = Modifier.padding(horizontal = 16.dp))
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            HorizontalDivider()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.feeds, key = { it.id }) { feed ->
                    ListItem(
                        headlineContent = { Text(feed.title) },
                        supportingContent = { Text(feed.url) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { feedToRename = feed }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Umbenennen")
                                }
                                IconButton(onClick = { viewModel.removeFeed(feed) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Entfernen")
                                }
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    feedToRename?.let { feed ->
        RenameFeedDialog(
            feed = feed,
            onDismiss = { feedToRename = null },
            onConfirm = { newTitle ->
                viewModel.renameFeed(feed, newTitle)
                feedToRename = null
            },
        )
    }
}

@Composable
private fun RenameFeedDialog(
    feed: FeedEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var title by remember { mutableStateOf(feed.title) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Feed umbenennen") },
        text = {
            OutlinedTextField(value = title, onValueChange = { title = it }, singleLine = true)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title) }) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}
