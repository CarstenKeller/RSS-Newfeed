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
)

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

    fun dismissArticle(articleId: Long) {
        viewModelScope.launch { repository.dismiss(articleId) }
    }
}
