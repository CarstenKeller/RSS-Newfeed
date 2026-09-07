package com.carstenkeller.rssnewfeed.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeds")
data class FeedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val language: String? = null,
    val topicTag: String? = null,
    val addedAt: Long,
    val lastFetchedAt: Long? = null,
    val lastFetchError: String? = null,
)
