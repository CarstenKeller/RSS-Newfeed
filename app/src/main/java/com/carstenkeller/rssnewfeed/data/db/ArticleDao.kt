package com.carstenkeller.rssnewfeed.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query(
        """
        SELECT a.id AS id, a.feedId AS feedId, a.title AS title, a.summary AS summary,
               a.imageUrl AS imageUrl, a.link AS link, a.publishedAt AS publishedAt,
               a.isRead AS isRead, f.title AS publisherName
        FROM articles a
        INNER JOIN feeds f ON f.id = a.feedId
        WHERE a.isDismissed = 0
        ORDER BY a.publishedAt DESC
        """
    )
    fun observeVisibleArticles(): Flow<List<ArticleListItem>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun observeArticle(id: Long): Flow<ArticleEntity?>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getById(id: Long): ArticleEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(articles: List<ArticleEntity>): List<Long>

    @Query("UPDATE articles SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE articles SET isDismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: Long)
}
