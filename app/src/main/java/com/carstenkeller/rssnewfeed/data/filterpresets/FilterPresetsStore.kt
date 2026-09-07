package com.carstenkeller.rssnewfeed.data.filterpresets

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Small, dependency-free JSON store for saved filter presets (see FilterPreset). */
object FilterPresetsStore {
    private const val PREFS_NAME = "filter_presets"
    private const val KEY_PRESETS = "presets_json"

    fun getAll(context: Context): List<FilterPreset> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PRESETS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { index -> array.getJSONObject(index).toPreset() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, name: String, feedId: Long?, includedTopics: Set<String>, excludedTopics: Set<String>, language: String?) {
        val preset = FilterPreset(
            id = UUID.randomUUID().toString(),
            name = name,
            feedId = feedId,
            includedTopics = includedTopics,
            excludedTopics = excludedTopics,
            language = language,
        )
        saveAll(context, getAll(context) + preset)
    }

    fun delete(context: Context, id: String) {
        saveAll(context, getAll(context).filterNot { it.id == id })
    }

    private fun saveAll(context: Context, presets: List<FilterPreset>) {
        val array = JSONArray()
        presets.forEach { array.put(it.toJson()) }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PRESETS, array.toString())
            .apply()
    }

    private fun FilterPreset.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("feedId", feedId ?: JSONObject.NULL)
        put("includedTopics", JSONArray(includedTopics.toList()))
        put("excludedTopics", JSONArray(excludedTopics.toList()))
        put("language", language ?: JSONObject.NULL)
    }

    private fun JSONObject.toPreset(): FilterPreset = FilterPreset(
        id = getString("id"),
        name = getString("name"),
        feedId = if (isNull("feedId")) null else getLong("feedId"),
        includedTopics = getJSONArray("includedTopics").toStringSet(),
        excludedTopics = getJSONArray("excludedTopics").toStringSet(),
        language = if (isNull("language")) null else getString("language"),
    )

    private fun JSONArray.toStringSet(): Set<String> =
        (0 until length()).map { getString(it) }.toSet()
}
