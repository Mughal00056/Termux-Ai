package com.example.domain.model

enum class SessionType {
    LOCAL,
    TERMUX,
    SSH
}

data class TerminalLine(
    val text: String,
    val isCommand: Boolean = false,
    val isError: Boolean = false,
    val exitCode: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class TerminalSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val type: SessionType = SessionType.LOCAL,
    val workingDir: String = "/data/data/com.termux/files/home",
    val lines: List<TerminalLine> = emptyList(),
    val isConnected: Boolean = true,
    val isExecuting: Boolean = false,
    val targetServerId: String? = null
)
