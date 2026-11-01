package com.example.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val command: String,
    val timestamp: Long,
    val exitCode: Int,
    val isFavorite: Boolean,
    val sessionType: String
)
