package com.example.data.ai

import com.example.domain.model.AIAnalysisResult
import com.example.domain.model.AICommandPlan
import com.example.domain.model.AICommandResponse

interface AIProvider {
    suspend fun generateCommand(prompt: String, terminalContext: String = ""): AICommandResponse
    suspend fun explainCommand(command: String): AICommandResponse
    suspend fun analyzeError(command: String, output: String, exitCode: Int): AIAnalysisResult
    suspend fun generateAgentPlan(goal: String, systemInfo: String): AICommandPlan
}
