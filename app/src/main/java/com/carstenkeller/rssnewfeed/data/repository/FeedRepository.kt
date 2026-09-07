package com.carstenkeller.rssnewfeed.data.repository

import com.carstenkeller.rssnewfeed.data.db.ArticleDao
import com.carstenkeller.rssnewfeed.data.db.ArticleEntity
import com.carstenkeller.rssnewfeed.data.db.ArticleListItem
import com.carstenkeller.rssnewfeed.data.db.FeedDao
import com.carstenkeller.rssnewfeed.data.db.FeedEntity
import com.carstenkeller.rssnewfeed.data.network.FeedFetcher
import kotlinx.coroutines.flow.Flow

class FeedRepository(
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val fetcher: FeedFetcher = FeedFetcher(),
) {
    val feeds: Flow<List<FeedEntity>> = feedDao.observeAll()
    val visibleArticles: Flow<List<ArticleListItem>> = articleDao.observeVisibleArticles()

    fun observeArticle(id: Long): Flow<ArticleEntity?> = articleDao.observeArticle(id)

    /** Adds a feed by URL, using the feed's own title, then does an initial refresh. */
    suspend fun addFeed(url: String) {
        val parsed = fetcher.fetchAndParse(url)
        val feed = FeedEntity(
            url = url,
            title = parsed.title.ifBlank { url },
            language = parsed.language,
            addedAt = System.currentTimeMillis(),
        )
        val feedId = feedDao.insert(feed)
        storeItems(feedId, parsed.items)
        feedDao.update(feed.copy(id = feedId, lastFetchedAt = System.currentTimeMillis()))
    }

    suspend fun updateFeed(feed: FeedEntity) = feedDao.update(feed)

    suspend fun removeFeed(feed: FeedEntity) = feedDao.delete(feed)

    /** Refreshes every known feed; failures on one feed don't abort the others. */
    suspend fun refreshAll() {
        for (feed in feedDao.getAll()) {
            refreshFeed(feed)
        }
    }

    suspend fun refreshFeed(feed: FeedEntity) {
        try {
            val parsed = fetcher.fetchAndParse(feed.url)
            storeItems(feed.id, parsed.items)
            feedDao.update(feed.copy(lastFetchedAt = System.currentTimeMillis(), lastFetchError = null))
        } catch (e: Exception) {
            feedDao.update(feed.copy(lastFetchError = e.message ?: "Unbekannter Fehler"))
        }
    }

    private suspend fun storeItems(feedId: Long, items: List<com.carstenkeller.rssnewfeed.data.network.ParsedItem>) {
        val now = System.currentTimeMillis()
        val entities = items.map { item ->
            ArticleEntity(
                feedId = feedId,
                guid = item.guid,
                title = item.title,
                summary = item.summary,
                contentHtml = item.contentHtml,
                imageUrl = item.imageUrl,
                link = item.link,
                publishedAt = item.publishedAt ?: now,
                fetchedAt = now,
            )
        }
        articleDao.insertAll(entities)
    }

    suspend fun markRead(articleId: Long) = articleDao.markRead(articleId)

    suspend fun dismiss(articleId: Long) = articleDao.dismiss(articleId)
}
