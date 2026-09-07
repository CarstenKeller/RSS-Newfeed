package com.carstenkeller.rssnewfeed.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds ORDER BY title ASC")
    fun observeAll(): Flow<List<FeedEntity>>

    @Query("SELECT * FROM feeds ORDER BY title ASC")
    suspend fun getAll(): List<FeedEntity>

    @Query("SELECT * FROM feeds WHERE id = :id")
    suspend fun getById(id: Long): FeedEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(feed: FeedEntity): Long

    @Update
    suspend fun update(feed: FeedEntity)

    @Delete
    suspend fun delete(feed: FeedEntity)
}
