package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.db.entities.AIConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AIConversationDao {
    @Query("SELECT * FROM ai_conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<AIConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(item: AIConversationEntity)

    @Query("DELETE FROM ai_conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("DELETE FROM ai_conversations")
    suspend fun clearAllConversations()
}
