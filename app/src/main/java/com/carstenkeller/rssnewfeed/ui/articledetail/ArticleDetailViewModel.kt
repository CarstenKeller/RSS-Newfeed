package com.carstenkeller.rssnewfeed.ui.articledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carstenkeller.rssnewfeed.data.db.ArticleEntity
import com.carstenkeller.rssnewfeed.data.repository.FeedRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArticleDetailViewModel(
    private val repository: FeedRepository,
    private val articleId: Long,
) : ViewModel() {

    val article: StateFlow<ArticleEntity?> = repository.observeArticle(articleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var alreadyMarkedRead = false

    fun markAsRead() {
        if (alreadyMarkedRead) return
        alreadyMarkedRead = true
        viewModelScope.launch { repository.markRead(articleId) }
    }
}
