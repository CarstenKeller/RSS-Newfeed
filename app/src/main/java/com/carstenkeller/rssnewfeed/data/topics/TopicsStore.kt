package com.carstenkeller.rssnewfeed.data.topics

import android.content.Context
import com.carstenkeller.rssnewfeed.domain.DEFAULT_TOPICS
import org.json.JSONArray

/**
 * User-editable topic list (Feed-Zuordnung, Filter, Benachrichtigungen all read from this).
 * Starts out as DEFAULT_TOPICS; the user can add or remove entries (e.g. add an English
 * "Cars" alongside "Auto" for English-language feeds) via the "Themen verwalten" screen.
 */
object TopicsStore {
    private const val PREFS_NAME = "topics_prefs"
    private const val KEY_TOPICS = "topics"

    fun getTopics(context: Context): List<String> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOPICS, null) ?: return DEFAULT_TOPICS
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { array.getString(it) }
        } catch (e: Exception) {
            DEFAULT_TOPICS
        }
    }

    fun addTopic(context: Context, topic: String): List<String> {
        val trimmed = topic.trim()
        val current = getTopics(context)
        if (trimmed.isBlank() || trimmed in current) return current
        val updated = current + trimmed
        saveTopics(context, updated)
        return updated
    }

    fun removeTopic(context: Context, topic: String): List<String> {
        val updated = getTopics(context) - topic
        saveTopics(context, updated)
        return updated
    }

    private fun saveTopics(context: Context, topics: List<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOPICS, JSONArray(topics).toString())
            .apply()
    }
}
