package com.carstenkeller.rssnewfeed.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeds")
data class FeedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val language: String? = null,
    // No longer written or read: superseded by topicTags (comma-joined, supports multiple
    // topics per feed). Kept in the schema to avoid another migration for a single unused column.
    val topicTag: String? = null,
    val topicTags: String = "",
    val addedAt: Long,
    val lastFetchedAt: Long? = null,
    val lastFetchError: String? = null,
)
