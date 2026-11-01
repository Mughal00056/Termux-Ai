package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalTextDim

@Composable
fun SplashScreen(
    onFinish: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(400))
        progress.animateTo(1f, tween(1200, easing = LinearEasing))
        onFinish()
    }

    val percent = (progress.value * 100).toInt()
    val statusText = when {
        percent < 20 -> "Logo fade in..."
        percent < 40 -> "Initializing system..."
        percent < 60 -> "Checking Termux environment..."
        percent < 80 -> "Loading secure configuration..."
        else -> "Ready"
    }

    // ASCII Progress Bar
    val totalBlocks = 12
    val filledBlocks = (progress.value * totalBlocks).toInt()
    val emptyBlocks = totalBlocks - filledBlocks
    val progressBarAscii = "█".repeat(filledBlocks) + "░".repeat(emptyBlocks)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(Color(0x2200E5FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "◉",
                    fontSize = 32.sp,
                    color = TerminalCyan
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "AI TERMUX",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                letterSpacing = 2.sp,
                color = TerminalText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Intelligent Android Terminal & SSH",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TerminalCyan
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = statusText,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = TerminalTextDim
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$progressBarAscii $percent%",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TerminalGreen
            )
        }
    }
}
