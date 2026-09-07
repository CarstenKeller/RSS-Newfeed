package com.carstenkeller.rssnewfeed.ui.feedmanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carstenkeller.rssnewfeed.data.db.FeedEntity
import com.carstenkeller.rssnewfeed.data.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FeedManagementUiState(
    val feeds: List<FeedEntity> = emptyList(),
    val isAdding: Boolean = false,
    val errorMessage: String? = null,
)

class FeedManagementViewModel(private val repository: FeedRepository) : ViewModel() {

    private val isAdding = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FeedManagementUiState> = combine(
        repository.feeds,
        isAdding,
        errorMessage,
    ) { feeds, adding, error ->
        FeedManagementUiState(feeds = feeds, isAdding = adding, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeedManagementUiState())

    fun addFeed(url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            isAdding.value = true
            errorMessage.value = null
            try {
                repository.addFeed(trimmed)
            } catch (e: Exception) {
                errorMessage.value = "Feed konnte nicht geladen werden: ${e.message}"
            } finally {
                isAdding.value = false
            }
        }
    }

    fun renameFeed(feed: FeedEntity, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch { repository.updateFeed(feed.copy(title = newTitle.trim())) }
    }

    fun setFeedTopic(feed: FeedEntity, topic: String?) {
        viewModelScope.launch { repository.updateFeed(feed.copy(topicTag = topic)) }
    }

    fun removeFeed(feed: FeedEntity) {
        viewModelScope.launch { repository.removeFeed(feed) }
    }

    fun dismissError() {
        errorMessage.value = null
    }
}
