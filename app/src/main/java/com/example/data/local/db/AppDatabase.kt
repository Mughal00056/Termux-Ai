package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.db.dao.AIConversationDao
import com.example.data.local.db.dao.FavoriteDao
import com.example.data.local.db.dao.HistoryDao
import com.example.data.local.db.dao.ServerDao
import com.example.data.local.db.entities.AIConversationEntity
import com.example.data.local.db.entities.FavoriteEntity
import com.example.data.local.db.entities.HistoryEntity
import com.example.data.local.db.entities.ServerEntity

@Database(
    entities = [
        HistoryEntity::class,
        ServerEntity::class,
        FavoriteEntity::class,
        AIConversationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun serverDao(): ServerDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun aiConversationDao(): AIConversationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_termux_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
