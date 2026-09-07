package com.carstenkeller.rssnewfeed.data.notifications

import android.content.Context

/** Small, dependency-free preference store for which topics should trigger a notification. */
object NotificationPreferences {
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_WATCHED_TOPICS = "watched_topics"

    fun getWatchedTopics(context: Context): Set<String> =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_WATCHED_TOPICS, emptySet())
            ?: emptySet()

    fun setWatchedTopics(context: Context, topics: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_WATCHED_TOPICS, topics)
            .apply()
    }
}
