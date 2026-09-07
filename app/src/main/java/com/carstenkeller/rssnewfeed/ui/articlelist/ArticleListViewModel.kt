package com.carstenkeller.rssnewfeed.ui.articlelist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carstenkeller.rssnewfeed.data.db.ArticleListItem
import com.carstenkeller.rssnewfeed.data.db.FeedEntity
import com.carstenkeller.rssnewfeed.data.filterpresets.FilterPreset
import com.carstenkeller.rssnewfeed.data.filterpresets.FilterPresetsStore
import com.carstenkeller.rssnewfeed.data.repository.FeedRepository
import com.carstenkeller.rssnewfeed.domain.TopicMatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchScope { ALLE, UNGELESEN, GELESEN }

/** Whether an article links out to a full original article, or is a short, source-less note. */
enum class SourceLinkFilter { ALLE, MIT_LINK, NUR_KURZNACHRICHT }

data class ArticleFilterState(
    val feedIds: Set<Long> = emptySet(),
    // Everything new starts unread; unread is therefore the implicit default view and
    // needs no filter option of its own. This just toggles into the "Gelesen" archive.
    val showRead: Boolean = false,
    val includedTopics: Set<String> = emptySet(),
    val excludedTopics: Set<String> = emptySet(),
    val language: String? = null,
    val dateFromMillis: Long? = null,
    val dateToMillis: Long? = null,
    val sourceLinkFilter: SourceLinkFilter = SourceLinkFilter.ALLE,
) {
    val isDefault: Boolean
        get() = feedIds.isEmpty() && !showRead && includedTopics.isEmpty() &&
            excludedTopics.isEmpty() && language == null && dateFromMillis == null &&
            dateToMillis == null && sourceLinkFilter == SourceLinkFilter.ALLE
}

data class ArticleListUiState(
    val articles: List<ArticleListItem> = emptyList(),
    val feeds: List<FeedEntity> = emptyList(),
    val filter: ArticleFilterState = ArticleFilterState(),
    val searchQuery: String = "",
    val searchScope: SearchScope = SearchScope.ALLE,
    val presets: List<FilterPreset> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

class ArticleListViewModel(
    private val repository: FeedRepository,
    private val appContext: Context,
) : ViewModel() {

    private val filterState = MutableStateFlow(ArticleFilterState())
    private val searchQuery = MutableStateFlow("")
    private val searchScope = MutableStateFlow(SearchScope.ALLE)
    private val presets = MutableStateFlow(FilterPresetsStore.getAll(appContext))
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ArticleListUiState> = combine(
        repository.visibleArticles,
        repository.feeds,
        filterState,
        combine(searchQuery, searchScope, presets, ::Triple),
        isRefreshing,
    ) { articles, feeds, filter, searchAndPresets, refreshing ->
        val (query, scope, presetList) = searchAndPresets
        val isSearching = query.isNotBlank()
        val filtered = articles
            .filter { filter.feedIds.isEmpty() || it.feedId in filter.feedIds }
            .filter {
                if (isSearching) {
                    when (scope) {
                        SearchScope.ALLE -> true
                        SearchScope.UNGELESEN -> !it.isRead
                        SearchScope.GELESEN -> it.isRead
                    }
                } else {
                    it.isRead == filter.showRead
                }
            }
            .filter { filter.includedTopics.isEmpty() || matchesAnyTopic(it, filter.includedTopics) }
            .filter { filter.excludedTopics.isEmpty() || !matchesAnyTopic(it, filter.excludedTopics) }
            .filter { filter.language == null || it.publisherLanguage == filter.language }
            .filter { filter.dateFromMillis == null || it.publishedAt >= filter.dateFromMillis }
            .filter { filter.dateToMillis == null || it.publishedAt <= filter.dateToMillis }
            .filter {
                when (filter.sourceLinkFilter) {
                    SourceLinkFilter.ALLE -> true
                    SourceLinkFilter.MIT_LINK -> it.link.isNotBlank()
                    SourceLinkFilter.NUR_KURZNACHRICHT -> it.link.isBlank()
                }
            }
            .filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.summary.contains(query, ignoreCase = true)
            }
        ArticleListUiState(
            articles = filtered,
            feeds = feeds,
            filter = filter,
            searchQuery = query,
            searchScope = scope,
            presets = presetList,
            isRefreshing = refreshing,
            errorMessage = errorMessage.value,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArticleListUiState())

    init {
        refresh()
    }

    private fun matchesAnyTopic(item: ArticleListItem, topics: Set<String>): Boolean = TopicMatching.matches(
        feedTopicTag = item.publisherTopicTag,
        categoriesCsv = item.categories,
        title = item.title,
        summary = item.summary,
        watchedTopics = topics,
    )

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                repository.refreshAll()
                errorMessage.value = null
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun toggleFeedFilter(feedId: Long) {
        val current = filterState.value.feedIds
        filterState.value = filterState.value.copy(
            feedIds = if (feedId in current) current - feedId else current + feedId,
        )
    }

    fun clearFeedFilter() {
        filterState.value = filterState.value.copy(feedIds = emptySet())
    }

    fun setShowRead(showRead: Boolean) {
        filterState.value = filterState.value.copy(showRead = showRead)
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSearchScope(scope: SearchScope) {
        searchScope.value = scope
    }

    fun toggleIncludedTopic(topic: String) {
        val current = filterState.value.includedTopics
        filterState.value = filterState.value.copy(
            includedTopics = if (topic in current) current - topic else current + topic,
        )
    }

    fun toggleExcludedTopic(topic: String) {
        val current = filterState.value.excludedTopics
        filterState.value = filterState.value.copy(
            excludedTopics = if (topic in current) current - topic else current + topic,
        )
    }

    fun setLanguageFilter(language: String?) {
        filterState.value = filterState.value.copy(language = language)
    }

    fun setDateRange(fromMillis: Long?, toMillis: Long?) {
        filterState.value = filterState.value.copy(dateFromMillis = fromMillis, dateToMillis = toMillis)
    }

    fun setSourceLinkFilter(sourceLinkFilter: SourceLinkFilter) {
        filterState.value = filterState.value.copy(sourceLinkFilter = sourceLinkFilter)
    }

    fun resetFilters() {
        filterState.value = ArticleFilterState()
    }

    fun savePreset(name: String) {
        val current = filterState.value
        FilterPresetsStore.add(
            appContext,
            name = name,
            feedIds = current.feedIds,
            includedTopics = current.includedTopics,
            excludedTopics = current.excludedTopics,
            language = current.language,
        )
        presets.value = FilterPresetsStore.getAll(appContext)
    }

    /** Applies a preset's topic/publisher/language selection; read state and date range are untouched. */
    fun applyPreset(preset: FilterPreset) {
        filterState.value = filterState.value.copy(
            feedIds = preset.feedIds,
            includedTopics = preset.includedTopics,
            excludedTopics = preset.excludedTopics,
            language = preset.language,
        )
    }

    fun deletePreset(id: String) {
        FilterPresetsStore.delete(appContext, id)
        presets.value = FilterPresetsStore.getAll(appContext)
    }

    /** Swiping an article away marks it read (moves it to "Gelesen") rather than deleting it. */
    fun swipeToRead(articleId: Long) {
        viewModelScope.launch { repository.markRead(articleId) }
    }
}
