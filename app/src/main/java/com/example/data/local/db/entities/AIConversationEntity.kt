package com.example.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_conversations")
data class AIConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val userPrompt: String,
    val aiResponse: String,
    val timestamp: Long,
    val category: String = "COMMAND"
)
