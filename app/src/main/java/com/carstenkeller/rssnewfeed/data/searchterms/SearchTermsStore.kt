package com.carstenkeller.rssnewfeed.data.searchterms

import android.content.Context
import org.json.JSONArray

/**
 * User-editable list of free-text search terms (e.g. "Trump"), matched against article
 * title/summary/category tags - see SearchTermMatching. Mirrors TopicsStore's shape.
 */
object SearchTermsStore {
    private const val PREFS_NAME = "search_terms_prefs"
    private const val KEY_TERMS = "terms"

    fun getTerms(context: Context): List<String> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TERMS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { array.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addTerm(context: Context, term: String): List<String> {
        val trimmed = term.trim()
        val current = getTerms(context)
        if (trimmed.isBlank() || trimmed in current) return current
        val updated = current + trimmed
        saveTerms(context, updated)
        return updated
    }

    fun removeTerm(context: Context, term: String): List<String> {
        val updated = getTerms(context) - term
        saveTerms(context, updated)
        return updated
    }

    private fun saveTerms(context: Context, terms: List<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TERMS, JSONArray(terms).toString())
            .apply()
    }
}
