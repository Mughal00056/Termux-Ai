package com.example.ui.screens.files

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
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.SftpFile
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
fun FilesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val localFiles by viewModel.localFiles.collectAsState()
    val remoteFiles by viewModel.remoteFiles.collectAsState()
    val currentLocalPath by viewModel.currentLocalPath.collectAsState()
    val currentRemotePath by viewModel.currentRemotePath.collectAsState()
    val servers by viewModel.servers.collectAsState()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<SftpFile?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = TerminalSurface,
            contentColor = TerminalCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = TerminalCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("LOCAL FILES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    servers.firstOrNull()?.let { s -> viewModel.loadRemoteFiles(s, currentRemotePath) }
                },
                text = { Text("SFTP REMOTE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }
            )
        }

        // Path Breadcrumb & Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedTab == 0) currentLocalPath.takeLast(35) else currentRemotePath,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TerminalCyan,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { showCreateFolderDialog = true }, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "Create Folder", tint = TerminalCyan)
            }
        }

        val activeList = if (selectedTab == 0) localFiles else remoteFiles

        if (activeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == 0) "Directory is empty" else "No remote files loaded (Connect SSH server)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = TerminalTextDim
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(activeList) { file ->
                    FileRowItem(
                        file = file,
                        onClick = {
                            if (file.isDirectory) {
                                if (selectedTab == 0) {
                                    viewModel.loadLocalFiles(file.path)
                                } else {
                                    servers.firstOrNull()?.let { s -> viewModel.loadRemoteFiles(s, file.path) }
                                }
                            }
                        },
                        onDelete = {
                            if (file.name != "..") {
                                fileToDelete = file
                            }
                        }
                    )
                }
            }
        }
    }

    // Safe Delete Modal
    fileToDelete?.let { file ->
        Dialog(onDismissRequest = { fileToDelete = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TerminalSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, TerminalRed, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = TerminalRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DELETE CONFIRMATION",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TerminalRed
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Are you sure you want to delete '${file.name}'? This action may be irreversible.",
                        fontSize = 13.sp,
                        color = TerminalText
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { fileToDelete = null }) { Text("Cancel", color = TerminalTextDim) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.deleteLocalFile(file.path)
                                fileToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalRed)
                        ) {
                            Text("Delete", color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showCreateFolderDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TerminalSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, TerminalBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "CREATE NEW FOLDER",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TerminalCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateFolderDialog = false }) { Text("Cancel", color = TerminalTextDim) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (folderName.isNotBlank()) {
                                    viewModel.createLocalFolder(folderName)
                                    showCreateFolderDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan)
                        ) {
                            Text("Create", color = Color(0xFF00363D), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileRowItem(
    file: SftpFile,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = TerminalSurface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
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
                Icon(
                    imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (file.isDirectory) TerminalCyan else TerminalTextDim,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = file.name,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TerminalText
                    )
                    Text(
                        text = "${file.permissions} • ${file.humanSize}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TerminalTextDim
                    )
                }
            }

            if (file.name != "..") {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TerminalTextDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
