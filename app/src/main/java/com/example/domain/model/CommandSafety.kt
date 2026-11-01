package com.example.domain.model

enum class SafetyLevel {
    SAFE,
    WARNING,
    DANGEROUS,
    BLOCKED
}

data class SafetyCheckResult(
    val level: SafetyLevel,
    val command: String,
    val matchedPattern: String = "",
    val reason: String = "",
    val suggestedAlternative: String = ""
) {
    val requiresConfirmation: Boolean get() = level == SafetyLevel.WARNING || level == SafetyLevel.DANGEROUS || level == SafetyLevel.BLOCKED
}
