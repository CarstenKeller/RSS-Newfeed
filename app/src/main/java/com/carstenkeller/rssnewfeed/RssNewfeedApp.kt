package com.carstenkeller.rssnewfeed

import android.app.Application
import com.carstenkeller.rssnewfeed.data.db.AppDatabase
import com.carstenkeller.rssnewfeed.data.notifications.NewArticlesNotifier
import com.carstenkeller.rssnewfeed.data.repository.FeedRepository
import com.carstenkeller.rssnewfeed.work.FeedRefreshWorker

class RssNewfeedApp : Application() {
    val repository: FeedRepository by lazy {
        val db = AppDatabase.getInstance(this)
        FeedRepository(db.feedDao(), db.articleDao())
    }

    override fun onCreate() {
        super.onCreate()
        NewArticlesNotifier.ensureChannel(this)
        FeedRefreshWorker.schedulePeriodic(this)
    }
}
