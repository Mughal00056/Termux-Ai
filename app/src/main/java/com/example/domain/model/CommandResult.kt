package com.example.domain.model

data class CommandResult(
    val command: String,
    val output: String,
    val error: String = "",
    val exitCode: Int = 0,
    val durationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isSuccess: Boolean get() = exitCode == 0
}
