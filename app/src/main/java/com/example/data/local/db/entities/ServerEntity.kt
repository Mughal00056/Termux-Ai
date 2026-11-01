package com.example.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ssh_servers")
data class ServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val authType: String,
    val encryptedCredentialKey: String,
    val fingerprint: String,
    val isTrusted: Boolean,
    val lastConnected: Long
)
