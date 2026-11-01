package com.example.domain.model

enum class AIProviderType {
    GEMINI,
    OPENAI,
    ANTHROPIC,
    CUSTOM,
    OFFLINE
}

data class AIProviderConfig(
    val providerType: AIProviderType = AIProviderType.OFFLINE,
    val apiKey: String = "",
    val model: String = "gemini-2.5-flash",
    val baseUrl: String = "",
    val temperature: Float = 0.2f,
    val maxTokens: Int = 1024,
    val agentConfirmationRequired: Boolean = true
)

data class AICommandResponse(
    val command: String,
    val explanation: String,
    val flags: Map<String, String> = emptyMap(),
    val warnings: List<String> = emptyList(),
    val alternativeCommands: List<String> = emptyList()
)

data class AIAnalysisResult(
    val summary: String,
    val rootCause: String,
    val suggestedFix: String,
    val fixCommand: String = "",
    val explanation: String = ""
)

enum class StepStatus {
    PENDING,
    APPROVED,
    RUNNING,
    COMPLETED,
    FAILED,
    SKIPPED
}

data class AIPlanStep(
    val stepNumber: Int,
    val description: String,
    val command: String,
    val status: StepStatus = StepStatus.PENDING,
    val isDangerous: Boolean = false,
    val output: String = "",
    val exitCode: Int? = null
)

data class AICommandPlan(
    val id: String = java.util.UUID.randomUUID().toString(),
    val goal: String,
    val steps: List<AIPlanStep>,
    val currentStepIndex: Int = 0,
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false,
    val pauseReason: String = ""
)
