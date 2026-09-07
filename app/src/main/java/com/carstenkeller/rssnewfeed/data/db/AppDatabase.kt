package com.carstenkeller.rssnewfeed.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FeedEntity::class, ArticleEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun articleDao(): ArticleDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rss-newfeed.db",
                )
                    // Pre-release app, no schema migrations shipped yet: dropping and
                    // recreating on a version bump is an accepted, explicit trade-off.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
