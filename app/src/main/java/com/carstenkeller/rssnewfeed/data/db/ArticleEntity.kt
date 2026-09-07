package com.carstenkeller.rssnewfeed.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "articles",
    foreignKeys = [
        ForeignKey(
            entity = FeedEntity::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["feedId", "guid"], unique = true),
        Index(value = ["feedId"]),
    ],
)
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val feedId: Long,
    val guid: String,
    val title: String,
    val summary: String,
    val contentHtml: String?,
    val imageUrl: String?,
    val link: String,
    val publishedAt: Long,
    val fetchedAt: Long,
    /** Comma-joined `<category>` tags from the feed item, used as a secondary topic filter signal. */
    val categories: String = "",
    val isRead: Boolean = false,
    val isDismissed: Boolean = false,
)
