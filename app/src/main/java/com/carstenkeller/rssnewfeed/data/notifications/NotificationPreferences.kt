package com.carstenkeller.rssnewfeed.data.notifications

import android.content.Context

/**
 * Small, dependency-free preference store for what should trigger a notification: any
 * combination of watched topics (manual feed tagging), watched search terms (free text in
 * title/summary/category), or saved filter presets - same criteria as the Filter-Sheet.
 */
object NotificationPreferences {
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_WATCHED_TOPICS = "watched_topics"
    private const val KEY_WATCHED_PRESET_IDS = "watched_preset_ids"
    private const val KEY_WATCHED_SEARCH_TERMS = "watched_search_terms"

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

    fun getWatchedSearchTerms(context: Context): Set<String> =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_WATCHED_SEARCH_TERMS, emptySet())
            ?: emptySet()

    fun setWatchedSearchTerms(context: Context, terms: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_WATCHED_SEARCH_TERMS, terms)
            .apply()
    }
}
