package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalText

@Composable
fun VirtualKeyBar(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val keys = listOf(
        "ESC", "TAB", "CTRL-C", "ALT",
        "|", "/", "-", "~", "$",
        "↑", "↓", "&", ">", ";", "CLEAR"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0D121B))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        keys.forEach { key ->
            VirtualKeyButton(
                label = key,
                onClick = { onKeyPress(key) }
            )
        }
    }
}

@Composable
fun VirtualKeyButton(
    label: String,
    onClick: () -> Unit
) {
    val isAccent = label == "CTRL-C" || label == "ESC" || label == "CLEAR"
    Box(
        modifier = Modifier
            .heightIn(min = 36.dp)
            .widthIn(min = 40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isAccent) Color(0x3300E5FF) else TerminalSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = if (isAccent) TerminalCyan else TerminalText
        )
    }
}
