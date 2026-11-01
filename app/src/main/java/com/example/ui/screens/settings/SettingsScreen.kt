package com.example.ui.screens.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AIProviderType
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onAccentChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val aiConfig by viewModel.aiConfig.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    var showKey by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    var providerExpanded by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf(aiConfig.providerType) }

    var hostVerificationEnabled by remember { mutableStateOf(true) }
    var biometricLockEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SETTINGS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TerminalText,
                letterSpacing = 1.sp
            )
        }

        // Section: AI Configuration
        item {
            SettingsCard(title = "AI PROVIDER & CREDENTIALS", icon = Icons.Default.AutoAwesome, accent = TerminalCyan) {
                // Provider Selector
                Text(text = "Active Provider:", fontSize = 11.sp, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = providerExpanded,
                    onExpandedChange = { providerExpanded = !providerExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProvider.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerminalCyan,
                            unfocusedBorderColor = TerminalBorder
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = providerExpanded,
                        onDismissRequest = { providerExpanded = false },
                        modifier = Modifier.background(TerminalSurface)
                    ) {
                        AIProviderType.values().forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider.name, fontFamily = FontFamily.Monospace) },
                                onClick = {
                                    selectedProvider = provider
                                    viewModel.updateAIProvider(provider, aiConfig.apiKey, aiConfig.model)
                                    providerExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // API Key with Keystore Management
                Text(text = "API Key (Stored in Android Keystore):", fontSize = 11.sp, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = if (apiKeyInput.isNotEmpty()) apiKeyInput else if (aiConfig.apiKey.isNotEmpty()) (if (showKey) aiConfig.apiKey else "••••••••••••••••••••") else "",
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text("Enter Gemini / OpenAI API key...", fontSize = 12.sp) },
                    visualTransformation = if (showKey || apiKeyInput.isNotEmpty()) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (aiConfig.apiKey.isNotEmpty()) {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(
                                        imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle view",
                                        tint = TerminalTextDim
                                    )
                                }
                                IconButton(onClick = {
                                    viewModel.deleteAIApiKey()
                                    apiKeyInput = ""
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete key", tint = TerminalRed)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (apiKeyInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.updateAIProvider(selectedProvider, apiKeyInput, aiConfig.model)
                            apiKeyInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Key to Keystore", color = Color(0xFF00363D), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "🔒 Keys are encrypted with hardware-backed AES-256-GCM. Never stored as plain text.",
                    fontSize = 11.sp,
                    color = TerminalTextDim
                )
            }
        }

        // Section: Appearance & Accents
        item {
            SettingsCard(title = "APPEARANCE & THEME", icon = Icons.Default.ColorLens, accent = TerminalGreen) {
                Text(text = "Accent Color:", fontSize = 11.sp, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))

                val accents = listOf(
                    "Cyan" to TerminalCyan,
                    "Green" to TerminalGreen,
                    "Amber" to TerminalAmber,
                    "Purple" to Color(0xFFB388FF),
                    "Red" to TerminalRed
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    accents.forEach { (name, color) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onAccentChange(color) },
                            contentAlignment = Alignment.Center
                        ) {
                            // color circle
                        }
                    }
                }
            }
        }

        // Section: SSH & Network
        item {
            SettingsCard(title = "SSH & HOST SECURITY", icon = Icons.Default.Dns, accent = Color(0xFFB388FF)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Host Fingerprint Verification", fontSize = 13.sp, color = TerminalText, fontWeight = FontWeight.Medium)
                        Text(text = "Alerts and blocks connection if server ED25519 key changes", fontSize = 11.sp, color = TerminalTextDim)
                    }
                    Switch(
                        checked = hostVerificationEnabled,
                        onCheckedChange = { hostVerificationEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TerminalCyan, checkedTrackColor = Color(0xFF004F58))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Keep-Alive Interval", fontSize = 13.sp, color = TerminalText, fontWeight = FontWeight.Medium)
                        Text(text = "Sends heartbeat every 60s to prevent session disconnects", fontSize = 11.sp, color = TerminalTextDim)
                    }
                    Text(text = "60s", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = TerminalCyan)
                }
            }
        }

        // Section: Security & Privacy
        item {
            SettingsCard(title = "SECURITY & DATA PRIVACY", icon = Icons.Default.Security, accent = TerminalAmber) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Biometric App Unlock", fontSize = 13.sp, color = TerminalText, fontWeight = FontWeight.Medium)
                        Text(text = "Require fingerprint / face auth to open AI Termux", fontSize = 11.sp, color = TerminalTextDim)
                    }
                    Switch(
                        checked = biometricLockEnabled,
                        onCheckedChange = { biometricLockEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TerminalAmber)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        viewModel.deleteAIApiKey()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear All Sensitive Data & Keys", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        }

        // Section: Termux Integration Guide
        item {
            SettingsCard(title = "TERMUX INTEGRATION STATUS", icon = Icons.Default.Terminal, accent = TerminalCyan) {
                Text(
                    text = telemetry.termuxStatusText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (telemetry.isTermuxInstalled) TerminalGreen else TerminalAmber
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Termux Package: com.termux\nPreferred Shell: ${telemetry.shellPath}\nArchitecture: ${telemetry.cpuArch}\nAndroid: ${telemetry.androidVersion}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TerminalTextDim,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.openTermux() },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (telemetry.isTermuxInstalled) "Launch Termux App" else "Install / Setup Termux",
                        color = Color(0xFF00363D),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Section: About
        item {
            SettingsCard(title = "ABOUT AI TERMUX", icon = Icons.Default.Info, accent = TerminalTextDim) {
                Text(text = "AI Termux - Android Terminal & SSH Client", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TerminalText)
                Text(text = "Version 1.0 (Build 36)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TerminalCyan)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A modern workstation combining local Termux integration, autonomous AI command generation, multi-session management, SSH, and SFTP file exploration.",
                    fontSize = 12.sp,
                    color = TerminalTextDim,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = accent
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
