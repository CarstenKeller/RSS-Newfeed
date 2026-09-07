package com.carstenkeller.rssnewfeed.domain

import com.carstenkeller.rssnewfeed.data.filterpresets.FilterPreset

/**
 * Whether a new article would show up under a saved filter preset - same criteria as the
 * Filter-Sheet (Herausgeber/Themen/Suchbegriffe/Sprache), minus read-state and date range,
 * which presets deliberately don't carry (see FilterPreset).
 */
object PresetMatching {
    fun matches(
        preset: FilterPreset,
        feedId: Long,
        feedTopicTags: String,
        categoriesCsv: String,
        title: String,
        summary: String,
        feedLanguage: String?,
    ): Boolean {
        if (preset.feedIds.isNotEmpty() && feedId !in preset.feedIds) return false
        if (preset.language != null && preset.language != feedLanguage) return false
        if (preset.includedTopics.isNotEmpty() && !TopicMatching.matches(feedTopicTags, preset.includedTopics)) {
            return false
        }
        if (preset.excludedTopics.isNotEmpty() && TopicMatching.matches(feedTopicTags, preset.excludedTopics)) {
            return false
        }
        if (preset.includedSearchTerms.isNotEmpty() &&
            !SearchTermMatching.matchesAny(preset.includedSearchTerms, title, summary, categoriesCsv)
        ) {
            return false
        }
        if (preset.excludedSearchTerms.isNotEmpty() &&
            SearchTermMatching.matchesAny(preset.excludedSearchTerms, title, summary, categoriesCsv)
        ) {
            return false
        }
        return true
    }
}
