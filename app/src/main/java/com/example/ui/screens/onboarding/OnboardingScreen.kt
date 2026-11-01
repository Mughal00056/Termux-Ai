package com.example.ui.screens.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalTextDim

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val codePreview: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var stepIndex by remember { mutableIntStateOf(0) }

    val steps = listOf(
        OnboardingStep(
            title = "AI TERMUX",
            subtitle = "Your intelligent Android terminal and development workstation.",
            icon = Icons.Default.Terminal,
            codePreview = "$ pkg update\n$ python -v\nPython 3.11.8"
        ),
        OnboardingStep(
            title = "AI COMMAND ASSISTANT",
            subtitle = "Describe what you want in plain words. AI generates the exact command safely.",
            icon = Icons.Default.AutoAwesome,
            codePreview = "You: Install Git\nAI: pkg install git\n[Run] [Explain] [Copy]"
        ),
        OnboardingStep(
            title = "SSH & SFTP MANAGER",
            subtitle = "Connect to your remote Linux servers and manage remote files securely.",
            icon = Icons.Default.Dns,
            codePreview = "● Ubuntu VPS (22)\nuser@server:~$ systemctl status nginx\nActive: active (running)"
        ),
        OnboardingStep(
            title = "READY TO LAUNCH",
            subtitle = "Configure Termux integration or start in local shell mode immediately.",
            icon = Icons.Default.CheckCircle,
            codePreview = "● Termux Bridge: Standby\n● Keystore Vault: Armed\n● Offline AI Core: Active"
        )
    )

    val currentStep = steps[stepIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skip header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinish) {
                Text("Skip", color = TerminalTextDim, fontFamily = FontFamily.Monospace)
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(TerminalCyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = currentStep.icon,
                    contentDescription = null,
                    tint = TerminalCyan,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = currentStep.title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TerminalText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = currentStep.subtitle,
                fontSize = 14.sp,
                color = TerminalTextDim,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Code Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurface, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = currentStep.codePreview,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TerminalGreen,
                    lineHeight = 18.sp
                )
            }
        }

        // Bottom Controls & Dots
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == stepIndex) 20.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (index == stepIndex) TerminalCyan else TerminalTextDim.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (stepIndex > 0) {
                    OutlinedButton(
                        onClick = { stepIndex-- },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Back", color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                Button(
                    onClick = {
                        if (stepIndex < steps.size - 1) {
                            stepIndex++
                        } else {
                            onFinish()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan)
                ) {
                    Text(
                        text = if (stepIndex == steps.size - 1) "Get Started" else "Continue",
                        color = Color(0xFF00363D),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
