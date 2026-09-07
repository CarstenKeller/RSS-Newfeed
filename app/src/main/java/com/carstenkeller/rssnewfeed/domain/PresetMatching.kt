package com.carstenkeller.rssnewfeed.domain

import com.carstenkeller.rssnewfeed.data.filterpresets.FilterPreset

/**
 * Whether a new article would show up under a saved filter preset - same criteria as the
 * Filter-Sheet (Herausgeber/Themen/Sprache), minus read-state and date range, which presets
 * deliberately don't carry (see FilterPreset). Used to let "Themen-Benachrichtigungen" also
 * fire for a saved preset, not just raw watched topics.
 */
object PresetMatching {
    fun matches(
        preset: FilterPreset,
        feedId: Long,
        feedTopicTag: String?,
        categoriesCsv: String,
        title: String,
        summary: String,
        feedLanguage: String?,
    ): Boolean {
        if (preset.feedIds.isNotEmpty() && feedId !in preset.feedIds) return false
        if (preset.language != null && preset.language != feedLanguage) return false
        if (preset.includedTopics.isNotEmpty() &&
            !TopicMatching.matches(feedTopicTag, categoriesCsv, title, summary, preset.includedTopics)
        ) {
            return false
        }
        if (preset.excludedTopics.isNotEmpty() &&
            TopicMatching.matches(feedTopicTag, categoriesCsv, title, summary, preset.excludedTopics)
        ) {
            return false
        }
        return true
    }
}
