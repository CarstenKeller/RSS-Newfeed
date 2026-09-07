package com.carstenkeller.rssnewfeed.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.Constraints
import com.carstenkeller.rssnewfeed.data.db.AppDatabase
import com.carstenkeller.rssnewfeed.data.filterpresets.FilterPresetsStore
import com.carstenkeller.rssnewfeed.data.notifications.NewArticlesNotifier
import com.carstenkeller.rssnewfeed.data.notifications.NotificationPreferences
import com.carstenkeller.rssnewfeed.data.repository.FeedRepository
import com.carstenkeller.rssnewfeed.data.repository.NewArticleInfo
import com.carstenkeller.rssnewfeed.domain.PresetMatching
import com.carstenkeller.rssnewfeed.domain.TopicMatching
import java.util.concurrent.TimeUnit

class FeedRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val repository = FeedRepository(db.feedDao(), db.articleDao())
        return try {
            val newArticles = repository.refreshAll()
            notifyIfMatchingWatchedTopicsOrPresets(newArticles)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun notifyIfMatchingWatchedTopicsOrPresets(newArticles: List<NewArticleInfo>) {
        val watchedTopics = NotificationPreferences.getWatchedTopics(applicationContext)
        val watchedPresetIds = NotificationPreferences.getWatchedPresetIds(applicationContext)
        val watchedPresets = if (watchedPresetIds.isEmpty()) {
            emptyList()
        } else {
            FilterPresetsStore.getAll(applicationContext).filter { it.id in watchedPresetIds }
        }
        if (watchedTopics.isEmpty() && watchedPresets.isEmpty()) return

        val matches = newArticles.filter { article ->
            (watchedTopics.isNotEmpty() &&
                TopicMatching.matches(article.feedTopicTags, article.categories, article.title, article.summary, watchedTopics)) ||
                watchedPresets.any { preset ->
                    PresetMatching.matches(
                        preset = preset,
                        feedId = article.feedId,
                        feedTopicTags = article.feedTopicTags,
                        categoriesCsv = article.categories,
                        title = article.title,
                        summary = article.summary,
                        feedLanguage = article.feedLanguage,
                    )
                }
        }
        if (matches.isNotEmpty()) {
            NewArticlesNotifier.notify(applicationContext, matches.size, matches.first().title)
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "feed_refresh_periodic"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<FeedRefreshWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
