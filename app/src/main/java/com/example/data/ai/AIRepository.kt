package com.example.data.ai

import com.example.data.local.db.dao.AIConversationDao
import com.example.data.local.db.entities.AIConversationEntity
import com.example.domain.model.AIAnalysisResult
import com.example.domain.model.AICommandPlan
import com.example.domain.model.AICommandResponse
import com.example.domain.model.AIProviderConfig
import com.example.domain.model.AIProviderType
import com.example.security.KeystoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AIRepository(
    private val conversationDao: AIConversationDao,
    private val keystoreManager: KeystoreManager
) {
    private val offlineProvider = OfflineHeuristicProvider()

    companion object {
        const val PREF_AI_PROVIDER = "ai_provider_type"
        const val PREF_AI_MODEL = "ai_model_name"
        const val PREF_AI_BASE_URL = "ai_base_url"
        const val KEY_AI_API_KEY = "ai_api_key_secure"
    }

    suspend fun getProviderConfig(): AIProviderConfig = withContext(Dispatchers.IO) {
        val apiKey = keystoreManager.retrieveAndDecrypt(KEY_AI_API_KEY) ?: ""
        AIProviderConfig(
            providerType = if (apiKey.isNotEmpty()) AIProviderType.GEMINI else AIProviderType.OFFLINE,
            apiKey = apiKey,
            model = "gemini-2.5-flash",
            baseUrl = "https://generativelanguage.googleapis.com"
        )
    }

    suspend fun saveApiKey(apiKey: String) = withContext(Dispatchers.IO) {
        keystoreManager.encryptAndStore(KEY_AI_API_KEY, apiKey.trim())
    }

    suspend fun clearApiKey() = withContext(Dispatchers.IO) {
        keystoreManager.delete(KEY_AI_API_KEY)
    }

    fun getConversationHistory(): Flow<List<AIConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    suspend fun generateCommand(
        prompt: String,
        contextTerminal: String = "",
        config: AIProviderConfig
    ): AICommandResponse = withContext(Dispatchers.IO) {
        val response = if (config.apiKey.isNotBlank() && config.providerType == AIProviderType.GEMINI) {
            try {
                callGeminiForCommand(prompt, contextTerminal, config.apiKey, config.model)
            } catch (e: Exception) {
                // Graceful fallback to offline heuristic
                offlineProvider.generateCommand(prompt, contextTerminal)
            }
        } else {
            offlineProvider.generateCommand(prompt, contextTerminal)
        }

        // Save conversation
        conversationDao.insertConversation(
            AIConversationEntity(
                id = java.util.UUID.randomUUID().toString(),
                title = prompt.take(30),
                userPrompt = prompt,
                aiResponse = response.command + "\n" + response.explanation,
                timestamp = System.currentTimeMillis(),
                category = "COMMAND"
            )
        )

        response
    }

    suspend fun explainCommand(command: String, config: AIProviderConfig): AICommandResponse = withContext(Dispatchers.IO) {
        offlineProvider.explainCommand(command)
    }

    suspend fun analyzeError(
        command: String,
        output: String,
        exitCode: Int,
        config: AIProviderConfig
    ): AIAnalysisResult = withContext(Dispatchers.IO) {
        val result = offlineProvider.analyzeError(command, output, exitCode)
        conversationDao.insertConversation(
            AIConversationEntity(
                id = java.util.UUID.randomUUID().toString(),
                title = "Error Analysis: ${command.take(20)}",
                userPrompt = "Error with $command:\n$output",
                aiResponse = "${result.summary}\nFix: ${result.fixCommand}",
                timestamp = System.currentTimeMillis(),
                category = "ERROR"
            )
        )
        result
    }

    suspend fun generateAgentPlan(
        goal: String,
        systemInfo: String,
        config: AIProviderConfig
    ): AICommandPlan = withContext(Dispatchers.IO) {
        offlineProvider.generateAgentPlan(goal, systemInfo)
    }

    private fun callGeminiForCommand(
        prompt: String,
        contextTerminal: String,
        apiKey: String,
        model: String
    ): AICommandResponse {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 8000
        conn.readTimeout = 12000
        conn.doOutput = true

        val systemPrompt = "You are an expert Linux/Termux CLI assistant. Provide the exact shell command, brief explanation, and flags in JSON format: {\"command\": \"...\", \"explanation\": \"...\", \"flags\": {}}"
        val userContent = "User request: $prompt\nTerminal Context: $contextTerminal"

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "$systemPrompt\n\n$userContent"))
                    })
                })
            }
            put("contents", contents)
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(jsonBody.toString())
            writer.flush()
        }

        if (conn.responseCode == 200) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val respJson = JSONObject(responseText)
            val text = respJson.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            // Parse json or return raw text
            return try {
                val clean = text.replace("```json", "").replace("```", "").trim()
                val parsed = JSONObject(clean)
                AICommandResponse(
                    command = parsed.optString("command", prompt),
                    explanation = parsed.optString("explanation", "Generated by Gemini AI"),
                    flags = emptyMap()
                )
            } catch (_: Exception) {
                AICommandResponse(
                    command = text.lines().firstOrNull() ?: prompt,
                    explanation = text
                )
            }
        } else {
            throw RuntimeException("Gemini HTTP ${conn.responseCode}")
        }
    }
}
