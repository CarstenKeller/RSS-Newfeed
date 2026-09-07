package com.carstenkeller.rssnewfeed.data.db

/** Flattened article + publisher name, as read by the list screen (avoids a manual join in UI code). */
data class ArticleListItem(
    val id: Long,
    val feedId: Long,
    val title: String,
    val summary: String,
    val imageUrl: String?,
    val link: String,
    val publishedAt: Long,
    val isRead: Boolean,
    val publisherName: String,
    val publisherTopicTag: String?,
    val publisherLanguage: String?,
    val categories: String,
)
