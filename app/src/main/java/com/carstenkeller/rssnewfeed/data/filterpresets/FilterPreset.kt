package com.carstenkeller.rssnewfeed.data.filterpresets

/**
 * A saved combination of topic/publisher/search-term/language filters the user can jump
 * back to quickly (e.g. "Politik & Wirtschaft", "Wissenschaft"). Deliberately excludes the
 * read/unread state (that stays a single, unfiltered "Gelesen" pool per user request)
 * and the date range (a saved date quickly goes stale, so it's a session-only filter).
 */
data class FilterPreset(
    val id: String,
    val name: String,
    val feedIds: Set<Long>,
    val includedTopics: Set<String>,
    val excludedTopics: Set<String>,
    val includedSearchTerms: Set<String> = emptySet(),
    val excludedSearchTerms: Set<String> = emptySet(),
    val language: String?,
)
