package com.carstenkeller.rssnewfeed.ui.articlelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.carstenkeller.rssnewfeed.data.db.ArticleListItem
import com.carstenkeller.rssnewfeed.ui.theme.readHighlightColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    viewModel: ArticleListViewModel,
    onOpenArticle: (Long) -> Unit,
    onOpenFeedManagement: () -> Unit,
    onOpenInfo: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var filterSheetOpen by remember { mutableStateOf(false) }
    var searchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (searchExpanded) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::setSearchQuery,
                                placeholder = { Text("Suche in Titel/Zusammenfassung") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            Text("RSS Newsfeed")
                        }
                    },
                    colors = com.carstenkeller.rssnewfeed.ui.theme.brandedTopAppBarColors(),
                    actions = {
                        if (searchExpanded) {
                            IconButton(onClick = {
                                searchExpanded = false
                                viewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Suche schließen")
                            }
                        } else {
                            IconButton(onClick = { searchExpanded = true }) {
                                Icon(Icons.Filled.Search, contentDescription = "Suchen")
                            }
                            IconButton(onClick = { filterSheetOpen = true }) {
                                Icon(
                                    Icons.Filled.FilterList,
                                    contentDescription = "Filter",
                                    tint = if (state.filter.isDefault) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                )
                            }
                            IconButton(onClick = onOpenFeedManagement) {
                                Icon(Icons.Filled.RssFeed, contentDescription = "Feeds verwalten")
                            }
                            IconButton(onClick = onOpenInfo) {
                                Icon(Icons.Filled.Info, contentDescription = "App-Info")
                            }
                        }
                    },
                )
                ReadFilterRow(
                    showRead = state.filter.showRead,
                    onToggle = viewModel::setShowRead,
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
                        when {
                            state.feeds.isEmpty() -> "Noch keine Feeds hinzugefügt. Tippe oben auf das Feed-Symbol."
                            state.searchQuery.isNotBlank() -> "Keine Treffer für \"${state.searchQuery}\"."
                            state.filter.showRead -> "Noch keine gelesenen Artikel."
                            else -> "Alles gelesen — keine ungelesenen Artikel."
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
                        onDismiss = { viewModel.swipeToRead(article.id) },
                    )
                }
            }
        }
    }

    if (filterSheetOpen) {
        FilterSheet(
            state = state,
            onDismiss = { filterSheetOpen = false },
            onSelectFeed = viewModel::setFeedFilter,
            onToggleIncludedTopic = viewModel::toggleIncludedTopic,
            onToggleExcludedTopic = viewModel::toggleExcludedTopic,
            onSelectLanguage = viewModel::setLanguageFilter,
            onSetDateRange = viewModel::setDateRange,
            onReset = viewModel::resetFilters,
        )
    }
}

@Composable
private fun ReadFilterRow(showRead: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = showRead,
            onClick = { onToggle(!showRead) },
            label = { Text(if (showRead) "Ungelesen anzeigen" else "Gelesen anzeigen") },
        )
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

    // Both backgroundContent and the foreground content must share exactly the same
    // bounds, or the background peeks out permanently at rest instead of only while
    // dragging. So the outer margin lives here, on the whole swipe box, never on the
    // Card alone.
    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromEndToStart = false,
            enableDismissFromStartToEnd = true,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(
                        Icons.Filled.Done,
                        contentDescription = "Als gelesen markieren",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            },
        ) {
            ArticleRow(article = article, onClick = onClick)
        }
    }
}

@Composable
private fun ArticleRow(article: ArticleListItem, onClick: () -> Unit) {
    var categoriesExpanded by remember(article.id) { mutableStateOf(false) }
    val categories = remember(article.categories) {
        article.categories.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (article.isRead) readHighlightColor() else MaterialTheme.colorScheme.surface,
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Quelle: ${article.publisherName}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatTimestamp(article.publishedAt),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
                if (categories.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clickable { categoriesExpanded = !categoriesExpanded }
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (categoriesExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text("Kategorien (${categories.size})", style = MaterialTheme.typography.labelSmall)
                    }
                    if (categoriesExpanded) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            categories.forEach { category ->
                                AssistChip(onClick = {}, label = { Text(category) })
                            }
                        }
                    }
                }
            }
        }
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

private fun formatTimestamp(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(timeFormatter)
