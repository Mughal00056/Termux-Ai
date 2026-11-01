package com.example.ui.screens.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AIAnalysisResult
import com.example.domain.model.AICommandPlan
import com.example.domain.model.AICommandResponse
import com.example.domain.model.AIPlanStep
import com.example.domain.model.StepStatus
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalConsoleBg
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalTextDim
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AIScreen(
    viewModel: MainViewModel,
    onExecuteInTerminal: (String) -> Unit,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf("Assistant", "Agent Mode", "Error Analyzer", "Explain")

    val aiResponse by viewModel.aiResponse.collectAsState()
    val errorAnalysis by viewModel.errorAnalysis.collectAsState()
    val agentPlan by viewModel.agentPlan.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()
    val aiConfig by viewModel.aiConfig.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var queryInput by remember { mutableStateOf("") }
    var errorCommandInput by remember { mutableStateOf("") }
    var errorOutputInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        // Tab Header
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = TerminalSurface,
            contentColor = TerminalCyan,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = TerminalCyan
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Active AI Engine Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (aiConfig.apiKey.isNotEmpty()) TerminalGreen else TerminalAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ENGINE: ${aiConfig.providerType.name} (${if (aiConfig.apiKey.isNotEmpty()) aiConfig.model else "Local Heuristics"})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalTextDim
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> AIAssistantTab(
                    response = aiResponse,
                    isLoading = isAILoading,
                    onExecute = onExecuteInTerminal,
                    onExplain = { viewModel.explainCommand(it) },
                    onCopy = { clipboardManager.setText(AnnotatedString(it)) }
                )
                1 -> AIAgentTab(
                    plan = agentPlan,
                    isLoading = isAILoading,
                    onStartPlan = { viewModel.startAgentPlan(it) },
                    onApproveStep = { viewModel.approveAgentStep(it) }
                )
                2 -> AIErrorAnalyzerTab(
                    analysis = errorAnalysis,
                    isLoading = isAILoading,
                    cmdInput = errorCommandInput,
                    onCmdChange = { errorCommandInput = it },
                    outputInput = errorOutputInput,
                    onOutputChange = { errorOutputInput = it },
                    onAnalyze = { viewModel.analyzeError(errorCommandInput, errorOutputInput) },
                    onExecute = onExecuteInTerminal,
                    onCopy = { clipboardManager.setText(AnnotatedString(it)) }
                )
                3 -> AIExplainTab(
                    response = aiResponse,
                    isLoading = isAILoading,
                    onExplain = { viewModel.explainCommand(it) },
                    onCopy = { clipboardManager.setText(AnnotatedString(it)) }
                )
            }
        }

        // Input Bar for Assistant tab
        if (selectedTab == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = queryInput,
                    onValueChange = { queryInput = it },
                    placeholder = {
                        Text(
                            text = "e.g. Git install karna hai or Install Node.js...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TerminalTextDim
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (queryInput.isNotBlank()) {
                            viewModel.askAI(queryInput)
                            queryInput = ""
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TerminalCyan,
                        unfocusedBorderColor = TerminalBorder,
                        focusedTextColor = TerminalText,
                        unfocusedTextColor = TerminalText
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (isAILoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = TerminalCyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = {
                            if (queryInput.isNotBlank()) {
                                viewModel.askAI(queryInput)
                                queryInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(TerminalCyan, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Ask AI",
                            tint = Color(0xFF00363D),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AIAssistantTab(
    response: AICommandResponse?,
    isLoading: Boolean,
    onExecute: (String) -> Unit,
    onExplain: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TerminalCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Synthesizing command with AI...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = TerminalTextDim
                        )
                    }
                }
            }
        } else if (response != null) {
            item {
                Surface(
                    color = TerminalSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TerminalCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SUGGESTED COMMAND",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TerminalCyan
                            )
                            IconButton(onClick = { onCopy(response.command) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = TerminalTextDim,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TerminalConsoleBg, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = response.command,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerminalGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = response.explanation,
                            fontSize = 13.sp,
                            color = TerminalText,
                            lineHeight = 18.sp
                        )

                        if (response.flags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "FLAGS EXPLANATION:",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TerminalTextDim
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            response.flags.forEach { (flag, meaning) ->
                                Text(
                                    text = "• $flag: $meaning",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TerminalTextDim
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onExecute(response.command) },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Execute", color = Color(0xFF00363D), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onExplain(response.command) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Explain", color = TerminalText, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Surface(
                    color = TerminalSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TerminalBorder, RoundedCornerShape(14.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = TerminalCyan, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "AI Command Assistant",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TerminalText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Type what you want in plain words below.\nAI will formulate the exact CLI command for your Termux/Linux environment.",
                            fontSize = 12.sp,
                            color = TerminalTextDim,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AIAgentTab(
    plan: AICommandPlan?,
    isLoading: Boolean,
    onStartPlan: (String) -> Unit,
    onApproveStep: (Int) -> Unit
) {
    var goalInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                color = TerminalSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TerminalAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUTONOMOUS AGENT PLANNER",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TerminalAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter a high-level goal. The agent builds a multi-step execution plan and pauses before each step for your explicit approval.",
                        fontSize = 12.sp,
                        color = TerminalTextDim
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        placeholder = { Text("e.g. Install Node and create a React app", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerminalAmber,
                            unfocusedBorderColor = TerminalBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (goalInput.isNotBlank()) onStartPlan(goalInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Plan", color = Color(0xFF432C00), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TerminalAmber)
                }
            }
        } else if (plan != null) {
            item {
                Text(
                    text = "GOAL: ${plan.goal.uppercase()}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TerminalCyan
                )
            }

            items(plan.steps.size) { index ->
                val step = plan.steps[index]
                AgentStepCard(
                    step = step,
                    isCurrent = index == plan.currentStepIndex,
                    onApprove = { onApproveStep(index) }
                )
            }

            if (plan.isPaused) {
                item {
                    Surface(
                        color = TerminalRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, TerminalRed, RoundedCornerShape(10.dp)).padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = TerminalRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = plan.pauseReason, color = TerminalRed, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgentStepCard(
    step: AIPlanStep,
    isCurrent: Boolean,
    onApprove: () -> Unit
) {
    val borderColor = when (step.status) {
        StepStatus.COMPLETED -> TerminalGreen
        StepStatus.RUNNING -> TerminalCyan
        StepStatus.FAILED -> TerminalRed
        else -> if (isCurrent) TerminalAmber else TerminalBorder
    }

    Surface(
        color = TerminalSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP ${step.stepNumber}: ${step.description}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TerminalText
                )
                Text(
                    text = step.status.name,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalConsoleBg, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "$ ${step.command}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TerminalCyan
                )
            }

            if (step.output.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = step.output.take(150),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalTextDim
                )
            }

            if (isCurrent && step.status == StepStatus.PENDING) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Approve & Execute Step", color = Color(0xFF00363D), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AIErrorAnalyzerTab(
    analysis: AIAnalysisResult?,
    isLoading: Boolean,
    cmdInput: String,
    onCmdChange: (String) -> Unit,
    outputInput: String,
    onOutputChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onExecute: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                color = TerminalSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = TerminalRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI ERROR & LOG DIAGNOSTICIAN",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TerminalRed
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cmdInput,
                        onValueChange = onCmdChange,
                        placeholder = { Text("Command that failed (e.g. ./gradlew assembleDebug)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = outputInput,
                        onValueChange = onOutputChange,
                        placeholder = { Text("Paste error output or stack trace...", fontSize = 12.sp) },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onAnalyze,
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Analyze Error with AI", color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TerminalRed)
                }
            }
        } else if (analysis != null) {
            item {
                Surface(
                    color = TerminalSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TerminalRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "DIAGNOSIS: ${analysis.summary}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TerminalRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Root Cause:\n${analysis.rootCause}",
                            fontSize = 13.sp,
                            color = TerminalText,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (analysis.fixCommand.isNotEmpty()) {
                            Text(
                                text = "RECOMMENDED FIX COMMAND:",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TerminalGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TerminalConsoleBg, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = analysis.fixCommand,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = TerminalGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onExecute(analysis.fixCommand) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Run Fix in Terminal", color = Color(0xFF00391A), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { onCopy(analysis.fixCommand) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TerminalText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIExplainTab(
    response: AICommandResponse?,
    isLoading: Boolean,
    onExplain: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    var cmdToExplain by remember { mutableStateOf("chmod -R 755 project") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                color = TerminalSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = TerminalCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMMAND EXPLAINER",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TerminalCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cmdToExplain,
                        onValueChange = { cmdToExplain = it },
                        placeholder = { Text("Command to explain (e.g. chmod -R 755 project)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { if (cmdToExplain.isNotBlank()) onExplain(cmdToExplain) },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Explain Command", color = Color(0xFF00363D), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (response != null) {
            item {
                Surface(
                    color = TerminalSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TerminalCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "BREAKDOWN FOR: ${response.command}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TerminalCyan
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = response.explanation,
                            fontSize = 13.sp,
                            color = TerminalText,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
