package com.carstenkeller.rssnewfeed.ui.articlelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carstenkeller.rssnewfeed.data.db.ArticleListItem
import com.carstenkeller.rssnewfeed.data.db.FeedEntity
import com.carstenkeller.rssnewfeed.data.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ReadFilter { ALLE, UNGELESEN, GELESEN }

data class ArticleFilterState(
    val feedId: Long? = null,
    val readFilter: ReadFilter = ReadFilter.ALLE,
    val includedTopics: Set<String> = emptySet(),
    val excludedTopics: Set<String> = emptySet(),
    val language: String? = null,
    val dateFromMillis: Long? = null,
    val dateToMillis: Long? = null,
) {
    val isDefault: Boolean
        get() = feedId == null && readFilter == ReadFilter.ALLE && includedTopics.isEmpty() &&
            excludedTopics.isEmpty() && language == null && dateFromMillis == null && dateToMillis == null
}

data class ArticleListUiState(
    val articles: List<ArticleListItem> = emptyList(),
    val feeds: List<FeedEntity> = emptyList(),
    val filter: ArticleFilterState = ArticleFilterState(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

class ArticleListViewModel(private val repository: FeedRepository) : ViewModel() {

    private val filterState = MutableStateFlow(ArticleFilterState())
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ArticleListUiState> = combine(
        repository.visibleArticles,
        repository.feeds,
        filterState,
        isRefreshing,
    ) { articles, feeds, filter, refreshing ->
        val filtered = articles
            .filter { filter.feedId == null || it.feedId == filter.feedId }
            .filter {
                when (filter.readFilter) {
                    ReadFilter.ALLE -> true
                    ReadFilter.UNGELESEN -> !it.isRead
                    ReadFilter.GELESEN -> it.isRead
                }
            }
            .filter { filter.includedTopics.isEmpty() || matchesAnyTopic(it, filter.includedTopics) }
            .filter { filter.excludedTopics.isEmpty() || !matchesAnyTopic(it, filter.excludedTopics) }
            .filter { filter.language == null || it.publisherLanguage == filter.language }
            .filter { filter.dateFromMillis == null || it.publishedAt >= filter.dateFromMillis }
            .filter { filter.dateToMillis == null || it.publishedAt <= filter.dateToMillis }
        ArticleListUiState(
            articles = filtered,
            feeds = feeds,
            filter = filter,
            isRefreshing = refreshing,
            errorMessage = errorMessage.value,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArticleListUiState())

    init {
        refresh()
    }

    /** A topic matches if it's the feed's assigned topic, or one of the item's own `<category>` tags. */
    private fun matchesAnyTopic(item: ArticleListItem, topics: Set<String>): Boolean {
        if (item.publisherTopicTag != null && item.publisherTopicTag in topics) return true
        if (item.categories.isBlank()) return false
        val categories = item.categories.split(",")
        return topics.any { topic -> categories.any { it.contains(topic, ignoreCase = true) } }
    }

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

    fun setFeedFilter(feedId: Long?) {
        filterState.value = filterState.value.copy(feedId = feedId)
    }

    fun setReadFilter(readFilter: ReadFilter) {
        filterState.value = filterState.value.copy(readFilter = readFilter)
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

    fun resetFilters() {
        filterState.value = ArticleFilterState()
    }

    fun dismissArticle(articleId: Long) {
        viewModelScope.launch { repository.dismiss(articleId) }
    }
}
