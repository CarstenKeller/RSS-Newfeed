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

    fun add(
        context: Context,
        name: String,
        feedIds: Set<Long>,
        includedTopics: Set<String>,
        excludedTopics: Set<String>,
        includedSearchTerms: Set<String>,
        excludedSearchTerms: Set<String>,
        language: String?,
    ) {
        val preset = FilterPreset(
            id = UUID.randomUUID().toString(),
            name = name,
            feedIds = feedIds,
            includedTopics = includedTopics,
            excludedTopics = excludedTopics,
            includedSearchTerms = includedSearchTerms,
            excludedSearchTerms = excludedSearchTerms,
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
        put("feedIds", JSONArray(feedIds.toList()))
        put("includedTopics", JSONArray(includedTopics.toList()))
        put("excludedTopics", JSONArray(excludedTopics.toList()))
        put("includedSearchTerms", JSONArray(includedSearchTerms.toList()))
        put("excludedSearchTerms", JSONArray(excludedSearchTerms.toList()))
        put("language", language ?: JSONObject.NULL)
    }

    private fun JSONObject.toPreset(): FilterPreset = FilterPreset(
        id = getString("id"),
        name = getString("name"),
        feedIds = when {
            has("feedIds") -> getJSONArray("feedIds").toLongSet()
            // Migrate presets saved before multi-select ("feedId": Long? or null).
            !isNull("feedId") -> setOf(getLong("feedId"))
            else -> emptySet()
        },
        includedTopics = getJSONArray("includedTopics").toStringSet(),
        excludedTopics = getJSONArray("excludedTopics").toStringSet(),
        includedSearchTerms = if (has("includedSearchTerms")) getJSONArray("includedSearchTerms").toStringSet() else emptySet(),
        excludedSearchTerms = if (has("excludedSearchTerms")) getJSONArray("excludedSearchTerms").toStringSet() else emptySet(),
        language = if (isNull("language")) null else getString("language"),
    )

    private fun JSONArray.toStringSet(): Set<String> =
        (0 until length()).map { getString(it) }.toSet()

    private fun JSONArray.toLongSet(): Set<Long> =
        (0 until length()).map { getLong(it) }.toSet()
}
