package com.example.ui.screens.history

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HistoryItem
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
fun HistoryScreen(
    viewModel: MainViewModel,
    onRunCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val history by viewModel.history.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val filteredList = history.filter { item ->
        val matchesQuery = item.command.contains(searchQuery, ignoreCase = true)
        val matchesFav = if (showOnlyFavorites) item.isFavorite else true
        matchesQuery && matchesFav
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search command history...", fontSize = 13.sp, color = TerminalTextDim) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TerminalTextDim) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TerminalTextDim)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TerminalCyan,
                unfocusedBorderColor = TerminalBorder
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filters row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showOnlyFavorites,
                    onClick = { showOnlyFavorites = false },
                    label = { Text("All (${history.size})", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TerminalCyan.copy(alpha = 0.2f),
                        selectedLabelColor = TerminalCyan
                    )
                )
                FilterChip(
                    selected = showOnlyFavorites,
                    onClick = { showOnlyFavorites = true },
                    label = { Text("Favorites (${history.count { it.isFavorite }})", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TerminalAmber.copy(alpha = 0.2f),
                        selectedLabelColor = TerminalAmber
                    )
                )
            }

            if (history.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearAllHistory() }) {
                    Text("Clear All", color = TerminalRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching commands found",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = TerminalTextDim
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { item ->
                    HistoryItemCard(
                        item = item,
                        onRun = { onRunCommand(item.command) },
                        onCopy = { clipboardManager.setText(AnnotatedString(item.command)) },
                        onToggleFavorite = { viewModel.toggleFavorite(item) },
                        onDelete = { viewModel.deleteHistoryItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryItem,
    onRun: () -> Unit,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = TerminalSurface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable(onClick = onRun)
            .padding(12.dp)
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
                        text = "${item.sessionType} • exit: ${item.exitCode}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TerminalTextDim
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCopy, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TerminalTextDim, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) TerminalAmber else TerminalTextDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TerminalTextDim, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onRun, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = TerminalCyan, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
