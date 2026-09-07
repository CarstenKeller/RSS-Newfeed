package com.carstenkeller.rssnewfeed.ui.articlelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.carstenkeller.rssnewfeed.data.db.ArticleListItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ReadHighlight = Color(0xFFDDF5DD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    viewModel: ArticleListViewModel,
    onOpenArticle: (Long) -> Unit,
    onOpenFeedManagement: () -> Unit,
    onOpenInfo: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("RSS Newfeed") },
                    actions = {
                        var filterMenuOpen by remember { mutableStateOf(false) }
                        IconButton(onClick = { filterMenuOpen = true }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                        }
                        FeedFilterMenu(
                            expanded = filterMenuOpen,
                            onDismiss = { filterMenuOpen = false },
                            feeds = state.feeds,
                            selectedFeedId = state.filter.feedId,
                            onSelectFeed = { viewModel.setFeedFilter(it); filterMenuOpen = false },
                        )
                        IconButton(onClick = onOpenFeedManagement) {
                            Icon(Icons.Filled.RssFeed, contentDescription = "Feeds verwalten")
                        }
                        IconButton(onClick = onOpenInfo) {
                            Icon(Icons.Filled.Info, contentDescription = "App-Info")
                        }
                    },
                )
                ReadFilterRow(
                    selected = state.filter.readFilter,
                    onSelect = viewModel::setReadFilter,
                )
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.articles.isEmpty() && !state.isRefreshing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (state.feeds.isEmpty()) {
                            "Noch keine Feeds hinzugefügt. Tippe oben auf das Feed-Symbol."
                        } else {
                            "Keine Artikel für diesen Filter."
                        },
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(state.articles, key = { it.id }) { article ->
                    DismissibleArticleRow(
                        article = article,
                        onClick = { onOpenArticle(article.id) },
                        onDismiss = { viewModel.dismissArticle(article.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedFilterMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    feeds: List<com.carstenkeller.rssnewfeed.data.db.FeedEntity>,
    selectedFeedId: Long?,
    onSelectFeed: (Long?) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(text = { Text("Alle Herausgeber") }, onClick = { onSelectFeed(null) })
        feeds.forEach { feed ->
            DropdownMenuItem(text = { Text(feed.title) }, onClick = { onSelectFeed(feed.id) })
        }
    }
}

@Composable
private fun ReadFilterRow(selected: ReadFilter, onSelect: (ReadFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReadFilter.entries.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissibleArticleRow(
    article: ArticleListItem,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onDismiss()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = false,
        enableDismissFromStartToEnd = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Ausblenden")
            }
        },
    ) {
        ArticleRow(article = article, onClick = onClick)
    }
}

@Composable
private fun ArticleRow(article: ArticleListItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (article.isRead) ReadHighlight else MaterialTheme.colorScheme.surface,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (article.imageUrl != null) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                )
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = article.publisherName, style = MaterialTheme.typography.labelSmall)
                    Text(text = formatTimestamp(article.publishedAt), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

private fun formatTimestamp(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(timeFormatter)
