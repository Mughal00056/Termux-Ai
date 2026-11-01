package com.example.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_commands")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val command: String,
    val category: String,
    val createdTimestamp: Long
)
