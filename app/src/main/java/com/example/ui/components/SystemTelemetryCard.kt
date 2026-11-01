package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.executor.SystemTelemetry
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalTextDim

@Composable
fun SystemTelemetryCard(
    telemetry: SystemTelemetry,
    onOpenTermux: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp)),
        color = TerminalSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (telemetry.isTermuxInstalled) TerminalGreen else TerminalAmber)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (telemetry.isTermuxInstalled) "● TERMUX CONNECTED" else "○ TERMUX NOT CONFIGURED",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (telemetry.isTermuxInstalled) TerminalGreen else TerminalAmber
                    )
                }

                OutlinedButton(
                    onClick = onOpenTermux,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TerminalCyan
                    ),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = if (telemetry.isTermuxInstalled) "Open" else "Setup",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "ARCH", fontSize = 10.sp, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                    Text(text = telemetry.cpuArch, fontSize = 12.sp, color = TerminalText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text(text = "SHELL", fontSize = 10.sp, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                    Text(text = telemetry.shellPath.substringAfterLast('/'), fontSize = 12.sp, color = TerminalText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text(text = "BATTERY", fontSize = 10.sp, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${telemetry.batteryPercent}%", fontSize = 12.sp, color = TerminalText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                        if (telemetry.isCharging) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = "Charging", tint = TerminalAmber, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // RAM Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = TerminalCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "RAM", fontSize = 11.sp, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = "${telemetry.totalRamMb - telemetry.freeRamMb}MB / ${telemetry.totalRamMb}MB (${telemetry.ramUsedPercent}%)",
                        fontSize = 11.sp,
                        color = TerminalText,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { telemetry.ramUsedPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (telemetry.ramUsedPercent > 85) TerminalRed else TerminalCyan,
                    trackColor = Color(0xFF1E2838)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Storage Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = TerminalGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "STORAGE", fontSize = 11.sp, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = "${telemetry.totalStorageGb - telemetry.freeStorageGb}GB / ${telemetry.totalStorageGb}GB (${telemetry.storageUsedPercent}%)",
                        fontSize = 11.sp,
                        color = TerminalText,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { telemetry.storageUsedPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TerminalGreen,
                    trackColor = Color(0xFF1E2838)
                )
            }
        }
    }
}
