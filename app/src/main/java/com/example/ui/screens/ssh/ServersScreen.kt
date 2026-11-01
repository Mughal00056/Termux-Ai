package com.example.ui.screens.ssh

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.AuthType
import com.example.domain.model.Server
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
import kotlinx.coroutines.launch

@Composable
fun ServersScreen(
    viewModel: MainViewModel,
    onConnectServer: (Server) -> Unit,
    modifier: Modifier = Modifier
) {
    val servers by viewModel.servers.collectAsState()
    val hostVerificationServer by viewModel.hostVerificationServer.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var testResultBanner by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SSH SERVERS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TerminalText,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${servers.size} configured",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalTextDim
                    )
                }
            }

            if (testResultBanner != null) {
                item {
                    Surface(
                        color = Color(0xFF00391A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = TerminalGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = testResultBanner ?: "", fontSize = 12.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            if (servers.isEmpty()) {
                item {
                    Surface(
                        color = TerminalSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Dns, contentDescription = null, tint = TerminalCyan, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "No SSH servers configured", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TerminalText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Tap + below to add your Linux server or VPS.", fontSize = 12.sp, color = TerminalTextDim)
                        }
                    }
                }
            } else {
                items(servers) { server ->
                    ServerCard(
                        server = server,
                        onConnect = {
                            viewModel.connectSSH(server)
                            onConnectServer(server)
                        },
                        onTest = {
                            scope.launch {
                                val res = viewModel.sshRepository.testConnection(server)
                                testResultBanner = res.second
                            }
                        },
                        onDelete = {
                            viewModel.deleteSSHServer(server.id)
                        },
                        onDuplicate = {
                            viewModel.saveSSHServer(
                                server.copy(
                                    id = java.util.UUID.randomUUID().toString(),
                                    name = "${server.name} (Copy)"
                                ),
                                credentialSecret = ""
                            )
                        }
                    )
                }
            }
        }

        // Add Floating Action Button [+]
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = TerminalCyan,
            contentColor = Color(0xFF00363D),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Server", modifier = Modifier.size(24.dp))
        }

        // Add Server Dialog
        if (showAddDialog) {
            AddServerDialog(
                onDismiss = { showAddDialog = false },
                onSave = { newServer, secret ->
                    viewModel.saveSSHServer(newServer, secret)
                    showAddDialog = false
                }
            )
        }

        // Host Verification Dialog
        hostVerificationServer?.let { server ->
            HostVerificationDialog(
                server = server,
                onCancel = { viewModel.dismissHostVerification() },
                onTrustAndConnect = {
                    viewModel.trustAndConnectHost(server)
                    onConnectServer(server)
                }
            )
        }
    }
}

@Composable
fun ServerCard(
    server: Server,
    onConnect: () -> Unit,
    onTest: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (server.isOnline) TerminalGreen else TerminalAmber)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = server.name,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TerminalText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTest, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = "Test connection", tint = TerminalCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = TerminalTextDim, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TerminalRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${server.username}@${server.host}:${server.port}",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = TerminalTextDim
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Auth: ${server.authType.name}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalCyan
                )
                if (server.isTrusted) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "✓ Trusted Host",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Connect Terminal", color = Color(0xFF00363D), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddServerDialog(
    onDismiss: () -> Unit,
    onSave: (Server, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var portText by remember { mutableStateOf("22") }
    var username by remember { mutableStateOf("root") }
    var authType by remember { mutableStateOf(AuthType.PASSWORD) }
    var credentialSecret by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TerminalSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "ADD SSH SERVER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TerminalCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Server Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Host / IP") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = portText,
                        onValueChange = { portText = it },
                        label = { Text("Port") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "AUTHENTICATION", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TerminalTextDim)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = authType == AuthType.PASSWORD,
                        onClick = { authType = AuthType.PASSWORD },
                        colors = RadioButtonDefaults.colors(selectedColor = TerminalCyan)
                    )
                    Text("Password", fontSize = 13.sp, color = TerminalText)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = authType == AuthType.PRIVATE_KEY,
                        onClick = { authType = AuthType.PRIVATE_KEY },
                        colors = RadioButtonDefaults.colors(selectedColor = TerminalCyan)
                    )
                    Text("Private Key", fontSize = 13.sp, color = TerminalText)
                }

                OutlinedTextField(
                    value = credentialSecret,
                    onValueChange = { credentialSecret = it },
                    label = { Text(if (authType == AuthType.PASSWORD) "Password" else "Private Key (RSA/Ed25519)") },
                    singleLine = authType == AuthType.PASSWORD,
                    maxLines = if (authType == AuthType.PASSWORD) 1 else 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TerminalTextDim) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val port = portText.toIntOrNull() ?: 22
                            val server = Server(
                                name = name.ifBlank { host },
                                host = host,
                                port = port,
                                username = username,
                                authType = authType
                            )
                            onSave(server, credentialSecret)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan)
                    ) {
                        Text("Save Server", color = Color(0xFF00363D), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HostVerificationDialog(
    server: Server,
    onCancel: () -> Unit,
    onTrustAndConnect: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TerminalSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TerminalAmber.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = TerminalAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "HOST VERIFICATION",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TerminalAmber
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "The authenticity of host '${server.host}' cannot be established automatically.",
                    fontSize = 13.sp,
                    color = TerminalText
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "ED25519 FINGERPRINT:", fontSize = 10.sp, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070B10), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = server.fingerprint.ifEmpty { "SHA256:4a:88:c2:91:f0:33:1e:9b" },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TerminalCyan
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text("Cancel", color = TerminalTextDim) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onTrustAndConnect,
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan)
                    ) {
                        Text("Trust & Connect", color = Color(0xFF00363D), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
