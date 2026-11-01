package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.db.entities.ServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM ssh_servers ORDER BY lastConnected DESC, name ASC")
    fun getAllServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM ssh_servers WHERE id = :id LIMIT 1")
    suspend fun getServerById(id: String): ServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerEntity)

    @Query("UPDATE ssh_servers SET lastConnected = :timestamp WHERE id = :id")
    suspend fun updateLastConnected(id: String, timestamp: Long)

    @Query("UPDATE ssh_servers SET isTrusted = :trusted, fingerprint = :fingerprint WHERE id = :id")
    suspend fun updateTrustStatus(id: String, trusted: Boolean, fingerprint: String)

    @Query("DELETE FROM ssh_servers WHERE id = :id")
    suspend fun deleteServer(id: String)
}
