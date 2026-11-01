package com.example.ui.screens.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SessionType
import com.example.domain.model.TerminalSession
import com.example.ui.components.FloatingAIButton
import com.example.ui.components.TerminalOutputView
import com.example.ui.components.VirtualKeyBar
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
fun TerminalScreen(
    viewModel: MainViewModel,
    onOpenAITool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val activeSession = sessions.find { it.id == activeSessionId } ?: sessions.firstOrNull()

    var inputCommand by remember { mutableStateOf("") }
    val tabScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalConsoleBg)
    ) {
        // Multi-Session Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface)
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .horizontalScroll(tabScrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sessions.forEach { session ->
                SessionTabChip(
                    session = session,
                    isActive = session.id == activeSessionId,
                    onSelect = { viewModel.switchSession(session.id) },
                    onClose = { viewModel.closeSession(session.id) }
                )
            }

            // New Tab button [+]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E2838))
                    .clickable { viewModel.createNewSession(SessionType.LOCAL) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Session",
                    tint = TerminalCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Terminal Output Screen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TerminalOutputView(
                lines = activeSession?.lines ?: emptyList(),
                onAnalyzeError = { errText ->
                    viewModel.analyzeError(activeSession?.lines?.lastOrNull { it.isCommand }?.text?.removePrefix("$ ") ?: "command", errText)
                    onOpenAITool("analyze_error")
                }
            )

            // Floating AI Button in bottom right
            FloatingAIButton(
                onToolSelect = onOpenAITool,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            )
        }

        // Virtual Key Bar
        VirtualKeyBar(
            onKeyPress = { key ->
                when (key) {
                    "CLEAR" -> {
                        activeSession?.id?.let { viewModel.clearSessionOutput(it) }
                    }
                    "CTRL-C" -> {
                        inputCommand = ""
                        activeSession?.id?.let { viewModel.requestExecuteCommand("^C", it) }
                    }
                    "TAB" -> {
                        inputCommand += "\t"
                    }
                    "ESC" -> {
                        // clear or no-op
                    }
                    "↑", "↓" -> {
                        // pull last history command
                        val historyList = viewModel.history.value
                        if (historyList.isNotEmpty()) {
                            inputCommand = historyList.first().command
                        }
                    }
                    else -> {
                        inputCommand += key
                    }
                }
            }
        )

        // Command Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TerminalCyan,
                modifier = Modifier.padding(start = 6.dp, end = 4.dp)
            )

            OutlinedTextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                placeholder = {
                    Text(
                        text = "Enter command...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = TerminalTextDim
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputCommand.isNotBlank()) {
                        viewModel.requestExecuteCommand(inputCommand, activeSession?.id)
                        inputCommand = ""
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TerminalText,
                    unfocusedTextColor = TerminalText,
                    cursorColor = TerminalCyan
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                modifier = Modifier.weight(1f)
            )

            if (activeSession?.isExecuting == true) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 6.dp),
                    color = TerminalCyan,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = {
                        if (inputCommand.isNotBlank()) {
                            viewModel.requestExecuteCommand(inputCommand, activeSession?.id)
                            inputCommand = ""
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Run Command",
                        tint = if (inputCommand.isNotBlank()) TerminalCyan else TerminalTextDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SessionTabChip(
    session: TerminalSession,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val indicatorColor = when (session.type) {
        SessionType.LOCAL -> TerminalGreen
        SessionType.TERMUX -> TerminalCyan
        SessionType.SSH -> Color(0xFFB388FF)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0xFF1E2838) else Color(0xFF0F1520))
            .border(
                1.dp,
                if (isActive) TerminalCyan.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(3.5.dp))
                    .background(indicatorColor)
            )
            Text(
                text = session.title,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) TerminalText else TerminalTextDim
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Tab",
                    tint = TerminalTextDim,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
