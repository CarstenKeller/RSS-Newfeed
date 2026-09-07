package com.carstenkeller.rssnewfeed.domain

/**
 * "Themen" are a purely manual classification: whatever topic(s) the user assigned to a
 * feed in Feed-Verwaltung. No text or `<category>`-tag heuristics here anymore - those are
 * handled by the separate, user-editable "Suchbegriffe" (see SearchTermMatching), since
 * `<category>` tags are just as free-form/unstandardized as article text and don't deserve
 * special-cased treatment.
 */
object TopicMatching {
    fun matches(feedTopicTags: String, watchedTopics: Set<String>): Boolean {
        if (feedTopicTags.isBlank() || watchedTopics.isEmpty()) return false
        val feedTopics = feedTopicTags.split(",").map { it.trim() }
        return feedTopics.any { it in watchedTopics }
    }
}
