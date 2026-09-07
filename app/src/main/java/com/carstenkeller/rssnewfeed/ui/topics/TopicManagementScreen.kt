package com.carstenkeller.rssnewfeed.ui.topics

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carstenkeller.rssnewfeed.R
import com.carstenkeller.rssnewfeed.data.topics.TopicsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicManagementScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var topics by remember { mutableStateOf(TopicsStore.getTopics(context)) }
    var newTopic by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_manage_topics)) },
                colors = com.carstenkeller.rssnewfeed.ui.theme.brandedTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                stringResource(R.string.topics_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = newTopic,
                    onValueChange = { newTopic = it },
                    label = { Text(stringResource(R.string.topic_name_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                Button(
                    onClick = {
                        topics = TopicsStore.addTopic(context, newTopic)
                        newTopic = ""
                    },
                    enabled = newTopic.isNotBlank(),
                ) {
                    Text(stringResource(R.string.action_add))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(topics, key = { it }) { topic ->
                    ListItem(
                        headlineContent = { Text(topic) },
                        trailingContent = {
                            IconButton(onClick = { topics = TopicsStore.removeTopic(context, topic) }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_remove))
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
