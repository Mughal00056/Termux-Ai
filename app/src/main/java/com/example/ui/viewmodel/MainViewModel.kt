package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AIRepository
import com.example.data.executor.LocalProcessExecutor
import com.example.data.executor.SystemTelemetry
import com.example.data.executor.TermuxBridge
import com.example.data.local.db.AppDatabase
import com.example.data.repository.TerminalRepository
import com.example.data.ssh.SSHRepository
import com.example.data.ssh.SSHRepositoryImpl
import com.example.domain.model.AIAnalysisResult
import com.example.domain.model.AICommandPlan
import com.example.domain.model.AICommandResponse
import com.example.domain.model.AIProviderConfig
import com.example.domain.model.AIProviderType
import com.example.domain.model.AuthType
import com.example.domain.model.CommandResult
import com.example.domain.model.HistoryItem
import com.example.domain.model.SafetyCheckResult
import com.example.domain.model.Server
import com.example.domain.model.SessionType
import com.example.domain.model.SftpFile
import com.example.domain.model.StepStatus
import com.example.domain.model.TerminalSession
import com.example.security.KeystoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val keystoreManager = KeystoreManager(application)
    private val database = AppDatabase.getInstance(application)
    private val termuxBridge = TermuxBridge(application)
    private val commandExecutor = LocalProcessExecutor(application, termuxBridge)

    val sshRepository: SSHRepository = SSHRepositoryImpl(database.serverDao(), keystoreManager)
    val terminalRepository = TerminalRepository(
        context = application,
        historyDao = database.historyDao(),
        favoriteDao = database.favoriteDao(),
        commandExecutor = commandExecutor,
        termuxBridge = termuxBridge,
        sshRepository = sshRepository
    )
    val aiRepository = AIRepository(database.aiConversationDao(), keystoreManager)

    // State flows
    val sessions: StateFlow<List<TerminalSession>> = terminalRepository.sessions
    val activeSessionId: StateFlow<String> = terminalRepository.activeSessionId

    val servers: StateFlow<List<Server>> = sshRepository.getServers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryItem>> = terminalRepository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _telemetry = MutableStateFlow(termuxBridge.getTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    // Safety dialog state
    private val _pendingSafetyCheck = MutableStateFlow<SafetyCheckResult?>(null)
    val pendingSafetyCheck: StateFlow<SafetyCheckResult?> = _pendingSafetyCheck.asStateFlow()

    private var pendingExecutionCommand: String? = null
    private var pendingExecutionSessionId: String? = null

    // AI State
    private val _aiConfig = MutableStateFlow(AIProviderConfig())
    val aiConfig: StateFlow<AIProviderConfig> = _aiConfig.asStateFlow()

    private val _aiResponse = MutableStateFlow<AICommandResponse?>(null)
    val aiResponse: StateFlow<AICommandResponse?> = _aiResponse.asStateFlow()

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    private val _errorAnalysis = MutableStateFlow<AIAnalysisResult?>(null)
    val errorAnalysis: StateFlow<AIAnalysisResult?> = _errorAnalysis.asStateFlow()

    private val _agentPlan = MutableStateFlow<AICommandPlan?>(null)
    val agentPlan: StateFlow<AICommandPlan?> = _agentPlan.asStateFlow()

    // File Browser State
    private val _localFiles = MutableStateFlow<List<SftpFile>>(emptyList())
    val localFiles: StateFlow<List<SftpFile>> = _localFiles.asStateFlow()

    private val _remoteFiles = MutableStateFlow<List<SftpFile>>(emptyList())
    val remoteFiles: StateFlow<List<SftpFile>> = _remoteFiles.asStateFlow()

    private val _currentLocalPath = MutableStateFlow(application.filesDir.absolutePath)
    val currentLocalPath: StateFlow<String> = _currentLocalPath.asStateFlow()

    private val _currentRemotePath = MutableStateFlow("/home")
    val currentRemotePath: StateFlow<String> = _currentRemotePath.asStateFlow()

    // Host verification prompt state
    private val _hostVerificationServer = MutableStateFlow<Server?>(null)
    val hostVerificationServer: StateFlow<Server?> = _hostVerificationServer.asStateFlow()

    init {
        refreshTelemetry()
        loadLocalFiles(_currentLocalPath.value)
        viewModelScope.launch {
            _aiConfig.value = aiRepository.getProviderConfig()
            seedDefaultServersIfEmpty()
        }
    }

    fun refreshTelemetry() {
        _telemetry.value = termuxBridge.getTelemetry()
    }

    private suspend fun seedDefaultServersIfEmpty() {
        // Provide example starter server configuration
        val existing = database.serverDao().getServerById("demo-ubuntu")
        if (existing == null) {
            sshRepository.saveServer(
                Server(
                    id = "demo-ubuntu",
                    name = "Ubuntu VPS",
                    host = "vps.example.com",
                    port = 22,
                    username = "root",
                    authType = AuthType.PASSWORD,
                    fingerprint = "SHA256:4a:88:c2:91:f0:33:1e:9b",
                    isTrusted = true,
                    lastConnected = System.currentTimeMillis() - 86400000
                ),
                credentialSecret = "demo_password"
            )
            sshRepository.saveServer(
                Server(
                    id = "demo-debian",
                    name = "Debian Server",
                    host = "192.168.1.20",
                    port = 22,
                    username = "user",
                    authType = AuthType.PRIVATE_KEY,
                    fingerprint = "SHA256:7b:19:e4:aa:30:11:08:cd",
                    isTrusted = false,
                    lastConnected = System.currentTimeMillis() - 172800000
                ),
                credentialSecret = "-----BEGIN OPENSSH PRIVATE KEY-----..."
            )
        }
    }

    // Terminal Commands
    fun requestExecuteCommand(command: String, sessionId: String? = null) {
        val targetSessionId = sessionId ?: activeSessionId.value
        val safety = terminalRepository.checkCommandSafety(command)

        if (safety.requiresConfirmation) {
            pendingExecutionCommand = command
            pendingExecutionSessionId = targetSessionId
            _pendingSafetyCheck.value = safety
        } else {
            executeCommandDirectly(command, targetSessionId)
        }
    }

    fun confirmDangerousExecution() {
        val cmd = pendingExecutionCommand ?: return
        val sId = pendingExecutionSessionId ?: activeSessionId.value
        _pendingSafetyCheck.value = null
        pendingExecutionCommand = null
        pendingExecutionSessionId = null
        executeCommandDirectly(cmd, sId)
    }

    fun cancelDangerousExecution() {
        _pendingSafetyCheck.value = null
        pendingExecutionCommand = null
        pendingExecutionSessionId = null
    }

    private fun executeCommandDirectly(command: String, sessionId: String) {
        viewModelScope.launch {
            terminalRepository.executeCommand(sessionId, command)
            refreshTelemetry()
        }
    }

    fun createNewSession(type: SessionType = SessionType.LOCAL, server: Server? = null): String {
        return terminalRepository.createNewSession(type, server)
    }

    fun switchSession(sessionId: String) {
        terminalRepository.switchSession(sessionId)
    }

    fun closeSession(sessionId: String) {
        terminalRepository.closeSession(sessionId)
    }

    fun clearSessionOutput(sessionId: String) {
        terminalRepository.clearSessionOutput(sessionId)
    }

    fun toggleFavorite(item: HistoryItem) {
        viewModelScope.launch {
            terminalRepository.toggleFavoriteHistory(item)
        }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            terminalRepository.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            terminalRepository.clearHistory()
        }
    }

    // AI
    fun askAI(prompt: String, terminalContext: String = "") {
        viewModelScope.launch {
            _isAILoading.value = true
            val response = aiRepository.generateCommand(prompt, terminalContext, _aiConfig.value)
            _aiResponse.value = response
            _isAILoading.value = false
        }
    }

    fun explainCommand(command: String) {
        viewModelScope.launch {
            _isAILoading.value = true
            val response = aiRepository.explainCommand(command, _aiConfig.value)
            _aiResponse.value = response
            _isAILoading.value = false
        }
    }

    fun analyzeError(command: String, output: String, exitCode: Int = 1) {
        viewModelScope.launch {
            _isAILoading.value = true
            val analysis = aiRepository.analyzeError(command, output, exitCode, _aiConfig.value)
            _errorAnalysis.value = analysis
            _isAILoading.value = false
        }
    }

    fun startAgentPlan(goal: String) {
        viewModelScope.launch {
            _isAILoading.value = true
            val plan = aiRepository.generateAgentPlan(goal, "Android API 36 / Termux", _aiConfig.value)
            _agentPlan.value = plan
            _isAILoading.value = false
        }
    }

    fun approveAgentStep(stepIndex: Int) {
        val currentPlan = _agentPlan.value ?: return
        if (stepIndex >= currentPlan.steps.size) return

        val step = currentPlan.steps[stepIndex]
        val safety = terminalRepository.checkCommandSafety(step.command)

        if (safety.requiresConfirmation) {
            // Safety check for agent step
            _agentPlan.value = currentPlan.copy(
                isPaused = true,
                pauseReason = "Agent paused: step contains dangerous command '${step.command}'"
            )
            return
        }

        val updatedSteps = currentPlan.steps.mapIndexed { i, s ->
            if (i == stepIndex) s.copy(status = StepStatus.RUNNING) else s
        }
        _agentPlan.value = currentPlan.copy(steps = updatedSteps, isPaused = false)

        viewModelScope.launch {
            val result = terminalRepository.executeCommand(activeSessionId.value, step.command)
            val finalSteps = _agentPlan.value?.steps?.mapIndexed { i, s ->
                if (i == stepIndex) s.copy(
                    status = if (result.exitCode == 0) StepStatus.COMPLETED else StepStatus.FAILED,
                    output = result.output.ifEmpty { result.error },
                    exitCode = result.exitCode
                ) else s
            } ?: emptyList()

            val isAllDone = finalSteps.all { it.status == StepStatus.COMPLETED }
            _agentPlan.value = _agentPlan.value?.copy(
                steps = finalSteps,
                currentStepIndex = stepIndex + 1,
                isCompleted = isAllDone
            )
        }
    }

    // SSH & Servers
    fun connectSSH(server: Server) {
        if (!server.isTrusted) {
            _hostVerificationServer.value = server
            return
        }
        val sessionId = createNewSession(SessionType.SSH, server)
        switchSession(sessionId)
    }

    fun trustAndConnectHost(server: Server) {
        viewModelScope.launch {
            sshRepository.trustHost(server.id, server.fingerprint)
            _hostVerificationServer.value = null
            val updated = server.copy(isTrusted = true)
            val sessionId = createNewSession(SessionType.SSH, updated)
            switchSession(sessionId)
        }
    }

    fun dismissHostVerification() {
        _hostVerificationServer.value = null
    }

    fun saveSSHServer(server: Server, credentialSecret: String) {
        viewModelScope.launch {
            sshRepository.saveServer(server, credentialSecret)
        }
    }

    fun deleteSSHServer(id: String) {
        viewModelScope.launch {
            sshRepository.deleteServer(id)
        }
    }

    // File Manager
    fun loadLocalFiles(path: String) {
        viewModelScope.launch {
            _currentLocalPath.value = path
            _localFiles.value = terminalRepository.listLocalFiles(path)
        }
    }

    fun createLocalFolder(name: String) {
        viewModelScope.launch {
            terminalRepository.createLocalFolder(_currentLocalPath.value, name)
            loadLocalFiles(_currentLocalPath.value)
        }
    }

    fun deleteLocalFile(path: String) {
        viewModelScope.launch {
            terminalRepository.deleteLocalFile(path)
            loadLocalFiles(_currentLocalPath.value)
        }
    }

    fun loadRemoteFiles(server: Server, path: String) {
        viewModelScope.launch {
            _currentRemotePath.value = path
            _remoteFiles.value = sshRepository.listRemoteFiles(server, path)
        }
    }

    // Settings
    fun updateAIProvider(providerType: AIProviderType, apiKey: String, model: String) {
        viewModelScope.launch {
            if (apiKey.isNotEmpty()) {
                aiRepository.saveApiKey(apiKey)
            }
            _aiConfig.value = _aiConfig.value.copy(
                providerType = providerType,
                apiKey = apiKey,
                model = model
            )
        }
    }

    fun deleteAIApiKey() {
        viewModelScope.launch {
            aiRepository.clearApiKey()
            _aiConfig.value = _aiConfig.value.copy(apiKey = "", providerType = AIProviderType.OFFLINE)
        }
    }

    fun openTermux() {
        termuxBridge.openTermuxApp()
    }
}
