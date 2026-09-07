package com.carstenkeller.rssnewfeed.ui.feedmanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carstenkeller.rssnewfeed.data.db.FeedEntity
import com.carstenkeller.rssnewfeed.data.opml.Opml
import com.carstenkeller.rssnewfeed.data.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

/** Structured status messages, resolved to localized text by the Composable layer. */
sealed interface FeedStatusMessage {
    data class LoadFailed(val detail: String?) : FeedStatusMessage
    data object NoFeedsInFile : FeedStatusMessage
    data class ImportFailed(val detail: String?) : FeedStatusMessage
    data class ImportSummary(val added: Int, val skipped: Int, val failed: Int) : FeedStatusMessage
}

data class FeedManagementUiState(
    val feeds: List<FeedEntity> = emptyList(),
    val isAdding: Boolean = false,
    val errorMessage: FeedStatusMessage? = null,
    val infoMessage: FeedStatusMessage? = null,
)

class FeedManagementViewModel(private val repository: FeedRepository) : ViewModel() {

    private val isAdding = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<FeedStatusMessage?>(null)
    private val infoMessage = MutableStateFlow<FeedStatusMessage?>(null)

    val uiState: StateFlow<FeedManagementUiState> = combine(
        repository.feeds,
        isAdding,
        errorMessage,
        infoMessage,
    ) { feeds, adding, error, info ->
        FeedManagementUiState(feeds = feeds, isAdding = adding, errorMessage = error, infoMessage = info)
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
                errorMessage.value = FeedStatusMessage.LoadFailed(e.message)
            } finally {
                isAdding.value = false
            }
        }
    }

    fun renameFeed(feed: FeedEntity, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch { repository.updateFeed(feed.copy(title = newTitle.trim())) }
    }

    fun setFeedTopics(feed: FeedEntity, topics: Set<String>) {
        viewModelScope.launch { repository.updateFeed(feed.copy(topicTags = topics.joinToString(","))) }
    }

    fun removeFeed(feed: FeedEntity) {
        viewModelScope.launch { repository.removeFeed(feed) }
    }

    fun dismissError() {
        errorMessage.value = null
    }

    fun dismissInfo() {
        infoMessage.value = null
    }

    suspend fun buildOpmlString(): String = Opml.write(repository.getAllFeeds())

    fun importOpml(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                val opmlFeeds = inputStream.use { Opml.parse(it) }
                if (opmlFeeds.isEmpty()) {
                    errorMessage.value = FeedStatusMessage.NoFeedsInFile
                    return@launch
                }
                val result = repository.importOpmlFeeds(opmlFeeds)
                infoMessage.value = FeedStatusMessage.ImportSummary(result.added, result.skipped, result.failed)
            } catch (e: Exception) {
                errorMessage.value = FeedStatusMessage.ImportFailed(e.message)
            }
        }
    }
}
