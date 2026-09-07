package com.carstenkeller.rssnewfeed.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Build 2 added `feeds.topicTag` and `articles.categories` but shipped with
 * fallbackToDestructiveMigration(), which silently wiped every saved feed on
 * update instead of migrating. Never do that again: schema changes from here
 * on get an explicit Migration that preserves existing data.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE feeds ADD COLUMN topicTag TEXT")
        db.execSQL("ALTER TABLE articles ADD COLUMN categories TEXT NOT NULL DEFAULT ''")
    }
}

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
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
    }
}
