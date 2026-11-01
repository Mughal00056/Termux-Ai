package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HistoryItem
import com.example.ui.components.SystemTelemetryCard
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalTextDim
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTerminal: () -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToSSH: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val history by viewModel.history.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AI TERMUX",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TerminalText,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "v1.0",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TerminalCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TerminalTextDim
                    )
                }
            }
        }

        // Live System Telemetry Card
        item {
            SystemTelemetryCard(
                telemetry = telemetry,
                onOpenTermux = { viewModel.openTermux() }
            )
        }

        // Quick Actions Grid
        item {
            Text(
                text = "QUICK ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TerminalTextDim,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Terminal",
                    desc = "Local & Termux",
                    icon = Icons.Default.Terminal,
                    accentColor = TerminalCyan,
                    onClick = onNavigateToTerminal,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "AI Assistant",
                    desc = "Generate & Debug",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = TerminalAmber,
                    onClick = onNavigateToAI,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "SSH Servers",
                    desc = "Linux Remote VPS",
                    icon = Icons.Default.Dns,
                    accentColor = TerminalGreen,
                    onClick = onNavigateToSSH,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Files",
                    desc = "Local & SFTP",
                    icon = Icons.Default.Folder,
                    accentColor = Color(0xFFB388FF),
                    onClick = onNavigateToFiles,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Preset Commands
        item {
            Text(
                text = "COMMON PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TerminalTextDim,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            val presets = listOf(
                "pkg update" to "Refresh repository lists",
                "pkg install git" to "Install Git CLI",
                "pkg install python" to "Install Python 3",
                "pkg install nodejs" to "Install Node.js LTS",
                "df -h" to "Check disk storage",
                "free -m" to "Check RAM metrics"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.take(3).forEach { (cmd, _) ->
                    PresetChip(command = cmd) {
                        viewModel.requestExecuteCommand(cmd)
                        onNavigateToTerminal()
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.drop(3).forEach { (cmd, _) ->
                    PresetChip(command = cmd) {
                        viewModel.requestExecuteCommand(cmd)
                        onNavigateToTerminal()
                    }
                }
            }
        }

        // Recent Commands History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT COMMANDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerminalTextDim,
                    fontFamily = FontFamily.Monospace
                )
                if (history.isNotEmpty()) {
                    Text(
                        text = "${history.size} logged",
                        fontSize = 11.sp,
                        color = TerminalTextDim,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (history.isEmpty()) {
            item {
                Surface(
                    color = TerminalSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No command history yet",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = TerminalTextDim
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Commands executed in terminal or AI will appear here.",
                            fontSize = 11.sp,
                            color = TerminalTextDim
                        )
                    }
                }
            }
        } else {
            items(history.take(6)) { item ->
                RecentCommandRow(
                    item = item,
                    onRun = {
                        viewModel.requestExecuteCommand(item.command)
                        onNavigateToTerminal()
                    },
                    onToggleFavorite = {
                        viewModel.toggleFavorite(item)
                    }
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, TerminalBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TerminalText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = TerminalTextDim
            )
        }
    }
}

@Composable
fun PresetChip(
    command: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$ ",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalCyan
            )
            Text(
                text = command,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RecentCommandRow(
    item: HistoryItem,
    onRun: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        color = TerminalSurface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable(onClick = onRun)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (item.exitCode == 0) TerminalGreen else TerminalRed)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.command,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TerminalText
                    )
                    Text(
                        text = "${item.sessionType} • code ${item.exitCode}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TerminalTextDim
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) TerminalAmber else TerminalTextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onRun, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Re-run",
                        tint = TerminalCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
