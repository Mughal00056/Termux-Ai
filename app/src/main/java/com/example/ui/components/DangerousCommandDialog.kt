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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.SafetyCheckResult
import com.example.domain.model.SafetyLevel
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalTextDim

@Composable
fun DangerousCommandDialog(
    safetyResult: SafetyCheckResult,
    onCancel: () -> Unit,
    onReview: () -> Unit,
    onRunAnyway: () -> Unit
) {
    val isDangerous = safetyResult.level == SafetyLevel.DANGEROUS || safetyResult.level == SafetyLevel.BLOCKED
    val bannerColor = if (isDangerous) TerminalRed else TerminalAmber
    val titleText = if (isDangerous) "⚠ DANGEROUS COMMAND" else "⚡ CAUTION REQUIRED"

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TerminalSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, bannerColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(bannerColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = bannerColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = titleText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = bannerColor,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = safetyResult.reason.ifEmpty { "This command may permanently modify or delete data." },
                    fontSize = 13.sp,
                    color = TerminalText,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "COMMAND:",
                    fontSize = 11.sp,
                    color = TerminalTextDim,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070B10), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = safetyResult.command,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = bannerColor
                    )
                }

                if (safetyResult.suggestedAlternative.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Safer alternative: ${safetyResult.suggestedAlternative}",
                        fontSize = 12.sp,
                        color = TerminalTextDim
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = TerminalTextDim)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onReview) {
                        Text("Review", color = TerminalText)
                    }
                    if (safetyResult.level != SafetyLevel.BLOCKED) {
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.material3.Button(
                            onClick = onRunAnyway,
                            colors = ButtonDefaults.buttonColors(containerColor = bannerColor)
                        ) {
                            Text("Run Anyway", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
