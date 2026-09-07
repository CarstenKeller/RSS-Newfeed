package com.carstenkeller.rssnewfeed.data.repository

import com.carstenkeller.rssnewfeed.data.db.ArticleDao
import com.carstenkeller.rssnewfeed.data.db.ArticleEntity
import com.carstenkeller.rssnewfeed.data.db.ArticleListItem
import com.carstenkeller.rssnewfeed.data.db.FeedDao
import com.carstenkeller.rssnewfeed.data.db.FeedEntity
import com.carstenkeller.rssnewfeed.data.network.FeedFetcher
import com.carstenkeller.rssnewfeed.data.opml.OpmlFeed
import kotlinx.coroutines.flow.Flow

data class OpmlImportResult(val added: Int, val skipped: Int, val failed: Int)

class FeedRepository(
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val fetcher: FeedFetcher = FeedFetcher(),
) {
    val feeds: Flow<List<FeedEntity>> = feedDao.observeAll()
    val visibleArticles: Flow<List<ArticleListItem>> = articleDao.observeVisibleArticles()

    suspend fun getAllFeeds(): List<FeedEntity> = feedDao.getAll()

    fun observeArticle(id: Long): Flow<ArticleEntity?> = articleDao.observeArticle(id)

    /** Adds a feed by URL, using the feed's own title, then does an initial refresh. */
    suspend fun addFeed(url: String, topicTag: String? = null) {
        val parsed = fetcher.fetchAndParse(url)
        val feed = FeedEntity(
            url = url,
            title = parsed.title.ifBlank { url },
            language = parsed.language,
            topicTag = topicTag,
            addedAt = System.currentTimeMillis(),
        )
        val feedId = feedDao.insert(feed)
        storeItems(feedId, parsed.items)
        feedDao.update(feed.copy(id = feedId, lastFetchedAt = System.currentTimeMillis()))
    }

    suspend fun updateFeed(feed: FeedEntity) = feedDao.update(feed)

    suspend fun removeFeed(feed: FeedEntity) = feedDao.delete(feed)

    /** Imports an OPML feed list, skipping URLs already subscribed to; one failure doesn't abort the rest. */
    suspend fun importOpmlFeeds(opmlFeeds: List<OpmlFeed>): OpmlImportResult {
        val existingUrls = feedDao.getAll().map { it.url }.toSet()
        var added = 0
        var skipped = 0
        var failed = 0
        for (opmlFeed in opmlFeeds) {
            if (opmlFeed.url in existingUrls) {
                skipped++
                continue
            }
            try {
                addFeed(opmlFeed.url, opmlFeed.topicTag)
                added++
            } catch (e: Exception) {
                failed++
            }
        }
        return OpmlImportResult(added, skipped, failed)
    }

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
                categories = item.categories.joinToString(","),
            )
        }
        articleDao.insertAll(entities)
    }

    suspend fun markRead(articleId: Long) = articleDao.markRead(articleId)
}
