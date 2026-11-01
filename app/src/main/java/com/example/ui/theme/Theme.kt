package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TerminalColorScheme = darkColorScheme(
    primary = TerminalCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9CF0FF),
    secondary = TerminalGreen,
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF005328),
    onSecondaryContainer = Color(0xFF86F8AC),
    tertiary = TerminalAmber,
    onTertiary = Color(0xFF432C00),
    tertiaryContainer = Color(0xFF614000),
    onTertiaryContainer = Color(0xFFFFDF9E),
    error = TerminalRed,
    onError = Color(0xFF41000C),
    errorContainer = Color(0xFF600016),
    onErrorContainer = Color(0xFFFFD9DD),
    background = TerminalBg,
    onBackground = TerminalText,
    surface = TerminalSurface,
    onSurface = TerminalText,
    surfaceVariant = TerminalSurfaceVariant,
    onSurfaceVariant = TerminalTextDim,
    outline = TerminalBorder
)

@Composable
fun AITermuxTheme(
    accentColor: Color = TerminalCyan,
    content: @Composable () -> Unit
) {
    val dynamicScheme = TerminalColorScheme.copy(
        primary = accentColor
    )
    MaterialTheme(
        colorScheme = dynamicScheme,
        typography = Typography,
        content = content
    )
}
