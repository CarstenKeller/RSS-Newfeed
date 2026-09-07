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

/** Minimal info about a newly-inserted article, enough to check it against watched topics/presets. */
data class NewArticleInfo(
    val title: String,
    val summary: String,
    val categories: String,
    val feedId: Long,
    val feedTopicTags: String,
    val feedLanguage: String?,
)

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
    suspend fun addFeed(url: String, topicTags: String = "") {
        val parsed = fetcher.fetchAndParse(url)
        val feed = FeedEntity(
            url = url,
            title = parsed.title.ifBlank { url },
            language = parsed.language,
            topicTags = topicTags,
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
                addFeed(opmlFeed.url, opmlFeed.topicTags)
                added++
            } catch (e: Exception) {
                failed++
            }
        }
        return OpmlImportResult(added, skipped, failed)
    }

    /**
     * Refreshes every known feed; failures on one feed don't abort the others.
     * Returns info about articles that were actually new (not already stored), so
     * callers (the background worker) can check them against watched-topic notifications.
     */
    suspend fun refreshAll(): List<NewArticleInfo> {
        val newArticles = mutableListOf<NewArticleInfo>()
        for (feed in feedDao.getAll()) {
            newArticles += refreshFeed(feed)
        }
        return newArticles
    }

    suspend fun refreshFeed(feed: FeedEntity): List<NewArticleInfo> {
        return try {
            val parsed = fetcher.fetchAndParse(feed.url)
            val inserted = storeItems(feed.id, parsed.items)
            feedDao.update(feed.copy(lastFetchedAt = System.currentTimeMillis(), lastFetchError = null))
            inserted.map { NewArticleInfo(it.title, it.summary, it.categories, feed.id, feed.topicTags, feed.language) }
        } catch (e: Exception) {
            feedDao.update(feed.copy(lastFetchError = e.message ?: "Unbekannter Fehler"))
            emptyList()
        }
    }

    /** Returns only the articles that were actually inserted (IGNORE-conflict rows come back as id -1). */
    private suspend fun storeItems(
        feedId: Long,
        items: List<com.carstenkeller.rssnewfeed.data.network.ParsedItem>,
    ): List<ArticleEntity> {
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
        val ids = articleDao.insertAll(entities)
        return entities.zip(ids).filter { (_, id) -> id != -1L }.map { (entity, _) -> entity }
    }

    suspend fun markRead(articleId: Long) = articleDao.markRead(articleId)
}
