package com.example.domain.model

data class HistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val command: String,
    val timestamp: Long = System.currentTimeMillis(),
    val exitCode: Int = 0,
    val isFavorite: Boolean = false,
    val sessionType: String = "LOCAL" // LOCAL, TERMUX, SSH
)
