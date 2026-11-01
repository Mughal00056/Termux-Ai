package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TerminalLine
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalConsoleBg
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalTextDim
import kotlinx.coroutines.launch

@Composable
fun TerminalOutputView(
    lines: List<TerminalLine>,
    onAnalyzeError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var autoScrollEnabled by remember { mutableStateOf(true) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(lines.size) {
        if (autoScrollEnabled && lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalConsoleBg)
    ) {
        if (lines.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$ _ [No active output]",
                    fontFamily = FontFamily.Monospace,
                    color = TerminalTextDim,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentPadding = PaddingValues(bottom = 48.dp)
            ) {
                itemsIndexed(lines) { index, line ->
                    TerminalLineRow(
                        line = line,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(line.text))
                        },
                        onAnalyzeError = {
                            onAnalyzeError(line.text)
                        }
                    )
                }
            }
        }

        // Floating Auto-scroll & Copy controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = {
                    val allText = lines.joinToString("\n") { it.text }
                    clipboardManager.setText(AnnotatedString(allText))
                },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0x99161B22),
                    contentColor = TerminalTextDim
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy terminal output",
                    modifier = Modifier.size(18.dp)
                )
            }

            FilledTonalIconButton(
                onClick = {
                    autoScrollEnabled = true
                    if (lines.isNotEmpty()) {
                        scope.launch { listState.animateScrollToItem(lines.size - 1) }
                    }
                },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (autoScrollEnabled) TerminalCyan.copy(alpha = 0.2f) else Color(0x99161B22),
                    contentColor = if (autoScrollEnabled) TerminalCyan else TerminalTextDim
                )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowDown,
                    contentDescription = "Scroll to bottom",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun TerminalLineRow(
    line: TerminalLine,
    onCopy: () -> Unit,
    onAnalyzeError: () -> Unit
) {
    val textColor = when {
        line.isError -> TerminalRed
        line.isCommand -> TerminalCyan
        line.exitCode != null && line.exitCode != 0 -> TerminalRed
        line.exitCode == 0 -> TerminalGreen
        line.text.startsWith("---") -> TerminalAmber
        else -> TerminalText
    }

    val parsedText = remember(line.text) { parseAnsiCodes(line.text, textColor) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp)
            .clickable(onClick = onCopy)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (line.isCommand) {
                Text(
                    text = "▶ ",
                    color = TerminalCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = parsedText,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = textColor
            )
        }

        // Show AI Analyze Chip for errors
        if (line.isError || (line.exitCode != null && line.exitCode != 0)) {
            Spacer(modifier = Modifier.height(4.dp))
            AssistChip(
                onClick = onAnalyzeError,
                label = {
                    Text(
                        text = "✦ Analyze Error with AI",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Analyze",
                        modifier = Modifier.size(14.dp),
                        tint = TerminalAmber
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0x33FFB300),
                    labelColor = TerminalAmber
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

fun parseAnsiCodes(text: String, defaultColor: Color): AnnotatedString {
    val cleanText = text.replace(Regex("\u001b\\[[0-9;]*[a-zA-Z]"), "")
    return buildAnnotatedString {
        append(cleanText)
        addStyle(SpanStyle(color = defaultColor), 0, cleanText.length)
    }
}
