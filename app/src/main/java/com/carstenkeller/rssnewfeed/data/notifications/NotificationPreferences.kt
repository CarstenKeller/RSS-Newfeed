package com.carstenkeller.rssnewfeed.data.notifications

import android.content.Context

/**
 * Small, dependency-free preference store for what should trigger a notification: either
 * (or both) a set of watched topics, or a set of saved filter presets (matched against a
 * new article's publisher/topic/language, same criteria as the Filter-Sheet).
 */
object NotificationPreferences {
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_WATCHED_TOPICS = "watched_topics"
    private const val KEY_WATCHED_PRESET_IDS = "watched_preset_ids"

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

    fun getWatchedPresetIds(context: Context): Set<String> =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_WATCHED_PRESET_IDS, emptySet())
            ?: emptySet()

    fun setWatchedPresetIds(context: Context, presetIds: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_WATCHED_PRESET_IDS, presetIds)
            .apply()
    }
}
