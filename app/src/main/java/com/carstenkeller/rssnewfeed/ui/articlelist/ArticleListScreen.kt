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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.carstenkeller.rssnewfeed.BuildConfig
import com.carstenkeller.rssnewfeed.R
import com.carstenkeller.rssnewfeed.data.db.ArticleListItem
import com.carstenkeller.rssnewfeed.data.filterpresets.FilterPreset
import com.carstenkeller.rssnewfeed.data.locale.LanguagePreferences
import com.carstenkeller.rssnewfeed.ui.notifications.NotificationSettingsDialog
import com.carstenkeller.rssnewfeed.ui.settings.LanguageDialog
import com.carstenkeller.rssnewfeed.ui.settings.ThemeModeDialog
import com.carstenkeller.rssnewfeed.ui.theme.AppearancePreferences
import com.carstenkeller.rssnewfeed.ui.theme.readHighlightColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    viewModel: ArticleListViewModel,
    onOpenArticle: (Long) -> Unit,
    onOpenFeedManagement: () -> Unit,
    onOpenInfo: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var filterSheetOpen by remember { mutableStateOf(false) }
    var searchExpanded by remember { mutableStateOf(false) }
    var notificationSettingsOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var appearanceDialogOpen by remember { mutableStateOf(false) }
    var languageDialogOpen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Scaffold(
        topBar = {
            Column {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                        )
                        if (!searchExpanded) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.menu_cd))
                                    }
                                    if (!state.filter.isDefault) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .align(Alignment.TopEnd)
                                                .background(MaterialTheme.colorScheme.error, CircleShape),
                                        )
                                    }
                                    MainMenu(
                                        expanded = menuExpanded,
                                        onDismiss = { menuExpanded = false },
                                        filterActive = !state.filter.isDefault,
                                        onSearch = { searchExpanded = true },
                                        onFilter = { filterSheetOpen = true },
                                        onNotifications = { notificationSettingsOpen = true },
                                        onManageFeeds = onOpenFeedManagement,
                                        onAppearance = { appearanceDialogOpen = true },
                                        onLanguage = { languageDialogOpen = true },
                                        onInfo = onOpenInfo,
                                    )
                                }
                                FilterChip(
                                    selected = state.filter.showRead,
                                    onClick = { viewModel.setShowRead(!state.filter.showRead) },
                                    label = {
                                        Text(
                                            stringResource(
                                                if (state.filter.showRead) R.string.toggle_show_unread else R.string.toggle_show_read,
                                            ),
                                        )
                                    },
                                )
                                if (showScrollToTop) {
                                    AssistChip(
                                        onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                                        leadingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null) },
                                        label = { Text(stringResource(R.string.scroll_to_top)) },
                                    )
                                }
                            }
                        }
                    }
                }
                if (searchExpanded) {
                    SearchPanel(
                        query = state.searchQuery,
                        onQueryChange = viewModel::setSearchQuery,
                        scope = state.searchScope,
                        onSelectScope = viewModel::setSearchScope,
                        onClose = {
                            searchExpanded = false
                            viewModel.setSearchQuery("")
                        },
                    )
                } else if (state.presets.isNotEmpty()) {
                    PresetQuickRow(presets = state.presets, onApply = viewModel::applyPreset)
                }
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
                            state.feeds.isEmpty() -> stringResource(R.string.empty_no_feeds)
                            state.searchQuery.isNotBlank() ->
                                stringResource(R.string.empty_no_search_results, state.searchQuery)
                            state.filter.showRead -> stringResource(R.string.empty_no_read_articles)
                            else -> stringResource(R.string.empty_all_read)
                        },
                    )
                }
            }
            LazyColumn(
                state = listState,
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
            onToggleFeed = viewModel::toggleFeedFilter,
            onClearFeeds = viewModel::clearFeedFilter,
            onToggleIncludedTopic = viewModel::toggleIncludedTopic,
            onToggleExcludedTopic = viewModel::toggleExcludedTopic,
            onSelectLanguage = viewModel::setLanguageFilter,
            onSetDateRange = viewModel::setDateRange,
            onSelectSourceLinkFilter = viewModel::setSourceLinkFilter,
            onReset = viewModel::resetFilters,
            onApplyPreset = viewModel::applyPreset,
            onSavePreset = viewModel::savePreset,
            onDeletePreset = viewModel::deletePreset,
        )
    }

    if (notificationSettingsOpen) {
        NotificationSettingsDialog(onDismiss = { notificationSettingsOpen = false })
    }

    if (appearanceDialogOpen) {
        val themeMode by AppearancePreferences.themeMode.collectAsState()
        ThemeModeDialog(
            current = themeMode,
            onSelect = { AppearancePreferences.setThemeMode(context, it) },
            onDismiss = { appearanceDialogOpen = false },
        )
    }

    if (languageDialogOpen) {
        LanguageDialog(
            current = LanguagePreferences.getLanguage(),
            onSelect = { LanguagePreferences.setLanguage(it) },
            onDismiss = { languageDialogOpen = false },
        )
    }
}

@Composable
private fun MainMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    filterActive: Boolean,
    onSearch: () -> Unit,
    onFilter: () -> Unit,
    onNotifications: () -> Unit,
    onManageFeeds: () -> Unit,
    onAppearance: () -> Unit,
    onLanguage: () -> Unit,
    onInfo: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            onClick = { onDismiss(); onSearch() },
        )
        DropdownMenuItem(
            text = { Text(stringResource(if (filterActive) R.string.menu_filter_active else R.string.menu_filter)) },
            leadingIcon = {
                Icon(
                    Icons.Filled.FilterList,
                    contentDescription = null,
                    tint = if (filterActive) MaterialTheme.colorScheme.error else LocalContentColor.current,
                )
            },
            onClick = { onDismiss(); onFilter() },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_notifications)) },
            leadingIcon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
            onClick = { onDismiss(); onNotifications() },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_manage_feeds)) },
            leadingIcon = { Icon(Icons.Filled.RssFeed, contentDescription = null) },
            onClick = { onDismiss(); onManageFeeds() },
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_appearance)) },
            leadingIcon = { Icon(Icons.Filled.DarkMode, contentDescription = null) },
            onClick = { onDismiss(); onAppearance() },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_language)) },
            leadingIcon = { Icon(Icons.Filled.Language, contentDescription = null) },
            onClick = { onDismiss(); onLanguage() },
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.version_build_label, BuildConfig.VERSION_NAME, BuildConfig.BUILD_NUMBER.toString()))
            },
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
            onClick = { onDismiss(); onInfo() },
        )
    }
}

@Composable
private fun SearchPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    scope: SearchScope,
    onSelectScope: (SearchScope) -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(stringResource(R.string.search_placeholder), maxLines = 1) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.search_close_cd))
                }
            }
            SearchScopeRow(scope = scope, onSelect = onSelectScope)
        }
    }
}

@Composable
private fun SearchScopeRow(scope: SearchScope, onSelect: (SearchScope) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = scope == SearchScope.ALLE,
            onClick = { onSelect(SearchScope.ALLE) },
            label = { Text(stringResource(R.string.label_all)) },
        )
        FilterChip(
            selected = scope == SearchScope.UNGELESEN,
            onClick = { onSelect(SearchScope.UNGELESEN) },
            label = { Text(stringResource(R.string.filter_unread)) },
        )
        FilterChip(
            selected = scope == SearchScope.GELESEN,
            onClick = { onSelect(SearchScope.GELESEN) },
            label = { Text(stringResource(R.string.filter_read)) },
        )
    }
}

@Composable
private fun PresetQuickRow(
    presets: List<FilterPreset>,
    onApply: (FilterPreset) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        presets.forEach { preset ->
            AssistChip(onClick = { onApply(preset) }, label = { Text(preset.name) })
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
                        contentDescription = stringResource(R.string.mark_read_cd),
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
                        text = stringResource(R.string.source_label, article.publisherName),
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
                        Text(
                            stringResource(R.string.categories_count, categories.size),
                            style = MaterialTheme.typography.labelSmall,
                        )
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
