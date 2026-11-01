package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val activeIcon: ImageVector? = null,
    val inactiveIcon: ImageVector? = null
) {
    object Splash : Screen("splash", "Splash")
    object Onboarding : Screen("onboarding", "Onboarding")

    // Main Bottom Navigation Tabs
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Terminal : Screen("terminal", "Terminal", Icons.Filled.Terminal, Icons.Outlined.Terminal)
    object Servers : Screen("servers", "Servers", Icons.Filled.Dns, Icons.Outlined.Dns)
    object AI : Screen("ai", "AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Files : Screen("files", "Files", Icons.Filled.Folder, Icons.Outlined.Folder)

    // Secondary screens
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object SSHTerminal : Screen("ssh_terminal/{serverId}", "SSH Terminal") {
        fun createRoute(serverId: String) = "ssh_terminal/$serverId"
    }
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Terminal,
    Screen.Servers,
    Screen.AI,
    Screen.Files
)
