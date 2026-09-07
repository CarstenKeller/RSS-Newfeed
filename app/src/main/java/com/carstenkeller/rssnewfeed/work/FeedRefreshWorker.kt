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
import com.carstenkeller.rssnewfeed.data.notifications.NewArticlesNotifier
import com.carstenkeller.rssnewfeed.data.notifications.NotificationPreferences
import com.carstenkeller.rssnewfeed.data.repository.FeedRepository
import com.carstenkeller.rssnewfeed.domain.TopicMatching
import java.util.concurrent.TimeUnit

class FeedRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val repository = FeedRepository(db.feedDao(), db.articleDao())
        return try {
            val newArticles = repository.refreshAll()
            notifyIfMatchingWatchedTopics(newArticles)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun notifyIfMatchingWatchedTopics(newArticles: List<com.carstenkeller.rssnewfeed.data.repository.NewArticleInfo>) {
        val watchedTopics = NotificationPreferences.getWatchedTopics(applicationContext)
        if (watchedTopics.isEmpty()) return
        val matches = newArticles.filter {
            TopicMatching.matches(it.feedTopicTag, it.categories, it.title, it.summary, watchedTopics)
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
