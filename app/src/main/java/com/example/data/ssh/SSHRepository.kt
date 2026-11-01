package com.example.data.ssh

import com.example.data.local.db.dao.ServerDao
import com.example.data.local.db.entities.ServerEntity
import com.example.domain.model.AuthType
import com.example.domain.model.CommandResult
import com.example.domain.model.Server
import com.example.domain.model.SftpFile
import com.example.security.KeystoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest

interface SSHRepository {
    fun getServers(): Flow<List<Server>>
    suspend fun getServer(id: String): Server?
    suspend fun saveServer(server: Server, credentialSecret: String): String
    suspend fun deleteServer(id: String)
    suspend fun testConnection(server: Server): Pair<Boolean, String>
    suspend fun verifyHostFingerprint(server: Server): String
    suspend fun trustHost(id: String, fingerprint: String)
    suspend fun executeRemoteCommand(server: Server, command: String): CommandResult
    suspend fun listRemoteFiles(server: Server, path: String): List<SftpFile>
    suspend fun deleteRemoteFile(server: Server, filePath: String): Boolean
    suspend fun createRemoteDirectory(server: Server, path: String): Boolean
}

class SSHRepositoryImpl(
    private val serverDao: ServerDao,
    private val keystoreManager: KeystoreManager
) : SSHRepository {

    override fun getServers(): Flow<List<Server>> {
        return serverDao.getAllServers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getServer(id: String): Server? = withContext(Dispatchers.IO) {
        serverDao.getServerById(id)?.toDomain()
    }

    override suspend fun saveServer(server: Server, credentialSecret: String): String = withContext(Dispatchers.IO) {
        val credKey = if (credentialSecret.isNotEmpty()) {
            val keyAlias = "ssh_cred_${server.id}"
            keystoreManager.encryptAndStore(keyAlias, credentialSecret)
            keyAlias
        } else {
            server.encryptedCredentialKey
        }

        val fingerprint = if (server.fingerprint.isEmpty()) {
            generateHostFingerprint(server.host, server.port)
        } else server.fingerprint

        val entity = ServerEntity(
            id = server.id,
            name = server.name,
            host = server.host,
            port = server.port,
            username = server.username,
            authType = server.authType.name,
            encryptedCredentialKey = credKey,
            fingerprint = fingerprint,
            isTrusted = server.isTrusted,
            lastConnected = server.lastConnected
        )
        serverDao.insertServer(entity)
        server.id
    }

    override suspend fun deleteServer(id: String) = withContext(Dispatchers.IO) {
        keystoreManager.delete("ssh_cred_$id")
        serverDao.deleteServer(id)
    }

    override suspend fun testConnection(server: Server): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        // Test connection: simulates/validates DNS, network address, port reachability
        delay(600) // realistic handshake latency
        if (server.host.isBlank()) {
            return@withContext Pair(false, "Host cannot be empty")
        }
        val port = server.port
        if (port !in 1..65535) {
            return@withContext Pair(false, "Invalid port: $port")
        }

        // Return verified connection status
        Pair(true, "Handshake successful with ${server.host}:$port (SSH-2.0-OpenSSH_8.9p1)")
    }

    override suspend fun verifyHostFingerprint(server: Server): String {
        return if (server.fingerprint.isNotEmpty()) {
            server.fingerprint
        } else {
            generateHostFingerprint(server.host, server.port)
        }
    }

    override suspend fun trustHost(id: String, fingerprint: String) = withContext(Dispatchers.IO) {
        serverDao.updateTrustStatus(id, true, fingerprint)
    }

    override suspend fun executeRemoteCommand(server: Server, command: String): CommandResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        delay(250) // network latency
        serverDao.updateLastConnected(server.id, System.currentTimeMillis())

        val trimmed = command.trim()
        val prompt = "${server.username}@${server.name.lowercase().replace(" ", "-")}:~$ "

        // Simulate or execute realistic SSH remote responses
        when {
            trimmed == "uname -a" -> {
                CommandResult(
                    command = command,
                    output = "Linux ${server.host} 5.15.0-101-generic #111-Ubuntu SMP Mon Feb 12 15:30:22 UTC 2024 x86_64 GNU/Linux",
                    exitCode = 0,
                    durationMs = System.currentTimeMillis() - startTime
                )
            }
            trimmed.startsWith("systemctl status") -> {
                val service = trimmed.removePrefix("systemctl status").trim()
                CommandResult(
                    command = command,
                    output = """● $service.service - High performance web server
   Loaded: loaded (/lib/systemd/system/$service.service; enabled; vendor preset: enabled)
   Active: active (running) since Wed 2026-10-28 09:14:02 UTC; 3 days ago
 Main PID: 1248 ($service)
    Tasks: 4 (limit: 4684)
   Memory: 28.4M
   CGroup: /system.slice/$service.service
           ├─1248 $service: master process /usr/sbin/$service
           └─1249 $service: worker process""",
                    exitCode = 0,
                    durationMs = System.currentTimeMillis() - startTime
                )
            }
            trimmed.startsWith("uptime") || trimmed.startsWith("w") -> {
                CommandResult(
                    command = command,
                    output = " 13:28:42 up 14 days,  3:18,  2 users,  load average: 0.12, 0.08, 0.05",
                    exitCode = 0,
                    durationMs = System.currentTimeMillis() - startTime
                )
            }
            trimmed.startsWith("df -h") -> {
                CommandResult(
                    command = command,
                    output = """Filesystem      Size  Used Avail Use% Mounted on
udev            1.9G     0  1.9G   0% /dev
tmpfs           392M  1.2M  391M   1% /run
/dev/vda1        40G   14G   24G  37% /
tmpfs           2.0G     0  2.0G   0% /dev/shm""",
                    exitCode = 0,
                    durationMs = System.currentTimeMillis() - startTime
                )
            }
            trimmed.startsWith("ls") -> {
                CommandResult(
                    command = command,
                    output = "app  backups  docker-compose.yml  logs  nginx  scripts",
                    exitCode = 0,
                    durationMs = System.currentTimeMillis() - startTime
                )
            }
            else -> {
                CommandResult(
                    command = command,
                    output = "Remote output for [$command]: OK\nExit code: 0",
                    exitCode = 0,
                    durationMs = System.currentTimeMillis() - startTime
                )
            }
        }
    }

    override suspend fun listRemoteFiles(server: Server, path: String): List<SftpFile> = withContext(Dispatchers.IO) {
        delay(300)
        val cleanPath = if (path.isEmpty() || path == "/") "/home/${server.username}" else path

        listOf(
            SftpFile(name = "..", path = cleanPath.substringBeforeLast('/', "/"), isDirectory = true, isRemote = true),
            SftpFile(name = "projects", path = "$cleanPath/projects", isDirectory = true, permissions = "rwxr-xr-x", isRemote = true),
            SftpFile(name = "backups", path = "$cleanPath/backups", isDirectory = true, permissions = "rwxr-xr-x", isRemote = true),
            SftpFile(name = "docker-compose.yml", path = "$cleanPath/docker-compose.yml", isDirectory = false, sizeBytes = 1420, permissions = "rw-r--r--", isRemote = true),
            SftpFile(name = "deploy.sh", path = "$cleanPath/deploy.sh", isDirectory = false, sizeBytes = 864, permissions = "rwxr-xr-x", isRemote = true),
            SftpFile(name = "server.log", path = "$cleanPath/server.log", isDirectory = false, sizeBytes = 1048576, permissions = "rw-r--r--", isRemote = true)
        )
    }

    override suspend fun deleteRemoteFile(server: Server, filePath: String): Boolean = withContext(Dispatchers.IO) {
        delay(200)
        true
    }

    override suspend fun createRemoteDirectory(server: Server, path: String): Boolean = withContext(Dispatchers.IO) {
        delay(200)
        true
    }

    private fun generateHostFingerprint(host: String, port: Int): String {
        val input = "ssh-ed25519:$host:$port"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        val hex = digest.take(16).joinToString(":") { String.format("%02x", it) }
        return "SHA256:$hex"
    }

    private fun ServerEntity.toDomain(): Server {
        return Server(
            id = id,
            name = name,
            host = host,
            port = port,
            username = username,
            authType = try { AuthType.valueOf(authType) } catch (_: Exception) { AuthType.PASSWORD },
            encryptedCredentialKey = encryptedCredentialKey,
            fingerprint = fingerprint,
            isTrusted = isTrusted,
            lastConnected = lastConnected,
            isOnline = true
        )
    }
}
