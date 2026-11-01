package com.example.data.repository

import android.content.Context
import com.example.data.executor.CommandExecutor
import com.example.data.executor.LocalProcessExecutor
import com.example.data.executor.SystemTelemetry
import com.example.data.executor.TermuxBridge
import com.example.data.local.db.dao.FavoriteDao
import com.example.data.local.db.dao.HistoryDao
import com.example.data.local.db.entities.FavoriteEntity
import com.example.data.local.db.entities.HistoryEntity
import com.example.data.ssh.SSHRepository
import com.example.domain.model.CommandResult
import com.example.domain.model.HistoryItem
import com.example.domain.model.SafetyCheckResult
import com.example.domain.model.Server
import com.example.domain.model.SessionType
import com.example.domain.model.SftpFile
import com.example.domain.model.TerminalLine
import com.example.domain.model.TerminalSession
import com.example.security.SafetyInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class TerminalRepository(
    private val context: Context,
    private val historyDao: HistoryDao,
    private val favoriteDao: FavoriteDao,
    private val commandExecutor: CommandExecutor,
    val termuxBridge: TermuxBridge,
    val sshRepository: SSHRepository
) {
    private val _sessions = MutableStateFlow<List<TerminalSession>>(
        listOf(
            TerminalSession(
                id = "local-1",
                title = "Local 1",
                type = SessionType.LOCAL,
                workingDir = termuxBridge.getTelemetry().homeDir,
                lines = listOf(
                    TerminalLine(text = "AI Termux Console v1.0 [Ready]"),
                    TerminalLine(text = "Type 'help' or press ✦ AI for assistant."),
                    TerminalLine(text = "Working dir: ${termuxBridge.getTelemetry().homeDir}")
                )
            )
        )
    )
    val sessions: StateFlow<List<TerminalSession>> = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow("local-1")
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

    fun getTelemetry(): SystemTelemetry = termuxBridge.getTelemetry()

    fun getHistory(): Flow<List<HistoryItem>> {
        return historyDao.getAllHistory().map { list ->
            list.map {
                HistoryItem(
                    id = it.id,
                    command = it.command,
                    timestamp = it.timestamp,
                    exitCode = it.exitCode,
                    isFavorite = it.isFavorite,
                    sessionType = it.sessionType
                )
            }
        }
    }

    suspend fun toggleFavoriteHistory(item: HistoryItem) = withContext(Dispatchers.IO) {
        val newFav = !item.isFavorite
        historyDao.updateFavorite(item.id, newFav)
        if (newFav) {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    id = item.id,
                    title = item.command.take(24),
                    command = item.command,
                    category = "Commands",
                    createdTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            favoriteDao.deleteFavorite(item.id)
        }
    }

    suspend fun deleteHistoryItem(id: String) = withContext(Dispatchers.IO) {
        historyDao.deleteHistory(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAllHistory()
    }

    fun checkCommandSafety(command: String): SafetyCheckResult {
        return SafetyInterceptor.checkCommand(command)
    }

    fun createNewSession(type: SessionType = SessionType.LOCAL, server: Server? = null): String {
        val nextIndex = _sessions.value.size + 1
        val title = when (type) {
            SessionType.LOCAL -> "Local $nextIndex"
            SessionType.TERMUX -> "Termux $nextIndex"
            SessionType.SSH -> server?.name ?: "SSH $nextIndex"
        }
        val id = java.util.UUID.randomUUID().toString()
        val newSession = TerminalSession(
            id = id,
            title = title,
            type = type,
            workingDir = if (type == SessionType.SSH) "/home/${server?.username ?: "root"}" else termuxBridge.getTelemetry().homeDir,
            lines = listOf(
                TerminalLine(text = "--- Session: $title (${type.name}) ---"),
                if (type == SessionType.SSH) TerminalLine(text = "Connected to ${server?.host}:${server?.port}") else TerminalLine(text = "Ready")
            ),
            targetServerId = server?.id
        )
        _sessions.value = _sessions.value + newSession
        _activeSessionId.value = id
        return id
    }

    fun switchSession(sessionId: String) {
        _activeSessionId.value = sessionId
    }

    fun closeSession(sessionId: String) {
        val currentList = _sessions.value
        if (currentList.size <= 1) return // Keep at least one session
        val updated = currentList.filter { it.id != sessionId }
        _sessions.value = updated
        if (_activeSessionId.value == sessionId) {
            _activeSessionId.value = updated.first().id
        }
    }

    fun clearSessionOutput(sessionId: String) {
        _sessions.value = _sessions.value.map { session ->
            if (session.id == sessionId) {
                session.copy(lines = emptyList())
            } else session
        }
    }

    suspend fun executeCommand(sessionId: String, command: String): CommandResult = withContext(Dispatchers.IO) {
        val session = _sessions.value.find { it.id == sessionId }
            ?: return@withContext CommandResult(command, "", "Session not found", 1)

        // Append user command line
        val cmdLine = TerminalLine(
            text = "$ $command",
            isCommand = true
        )
        updateSessionLines(sessionId) { it + cmdLine }
        setSessionExecuting(sessionId, true)

        val result = if (session.type == SessionType.SSH && session.targetServerId != null) {
            val server = sshRepository.getServer(session.targetServerId)
            if (server != null) {
                sshRepository.executeRemoteCommand(server, command)
            } else {
                commandExecutor.execute(command, session.workingDir)
            }
        } else {
            commandExecutor.execute(command, session.workingDir)
        }

        // Add output lines
        val newLines = mutableListOf<TerminalLine>()
        if (result.output.isNotEmpty()) {
            result.output.lines().forEach { line ->
                newLines.add(TerminalLine(text = line, isCommand = false))
            }
        }
        if (result.error.isNotEmpty()) {
            result.error.lines().forEach { line ->
                newLines.add(TerminalLine(text = line, isError = true))
            }
        }

        val statusLine = TerminalLine(
            text = "[Process completed with exit code: ${result.exitCode}]",
            exitCode = result.exitCode
        )
        newLines.add(statusLine)

        updateSessionLines(sessionId) { it + newLines }
        setSessionExecuting(sessionId, false)

        // Save to Room history
        historyDao.insertHistory(
            HistoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                command = command,
                timestamp = System.currentTimeMillis(),
                exitCode = result.exitCode,
                isFavorite = false,
                sessionType = session.type.name
            )
        )

        result
    }

    private fun updateSessionLines(sessionId: String, transform: (List<TerminalLine>) -> List<TerminalLine>) {
        _sessions.value = _sessions.value.map { session ->
            if (session.id == sessionId) {
                session.copy(lines = transform(session.lines))
            } else session
        }
    }

    private fun setSessionExecuting(sessionId: String, isExecuting: Boolean) {
        _sessions.value = _sessions.value.map { session ->
            if (session.id == sessionId) {
                session.copy(isExecuting = isExecuting)
            } else session
        }
    }

    // Local file manager
    suspend fun listLocalFiles(path: String): List<SftpFile> = withContext(Dispatchers.IO) {
        val targetDir = if (path.isEmpty()) context.filesDir else File(path)
        if (!targetDir.exists() || !targetDir.isDirectory) {
            return@withContext emptyList()
        }

        val list = mutableListOf<SftpFile>()
        val parent = targetDir.parentFile
        if (parent != null && parent.canRead()) {
            list.add(SftpFile(name = "..", path = parent.absolutePath, isDirectory = true, isRemote = false))
        }

        val files = targetDir.listFiles() ?: emptyArray()
        files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })).forEach { f ->
            list.add(
                SftpFile(
                    name = f.name,
                    path = f.absolutePath,
                    isDirectory = f.isDirectory,
                    sizeBytes = if (f.isFile) f.length() else 0L,
                    permissions = if (f.canExecute()) "rwxr-xr-x" else "rw-r--r--",
                    lastModified = f.lastModified(),
                    isRemote = false
                )
            )
        }
        list
    }

    suspend fun createLocalFolder(parentPath: String, name: String): Boolean = withContext(Dispatchers.IO) {
        val dir = File(parentPath, name)
        dir.mkdirs()
    }

    suspend fun deleteLocalFile(path: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.isDirectory) file.deleteRecursively() else file.delete()
    }
}
