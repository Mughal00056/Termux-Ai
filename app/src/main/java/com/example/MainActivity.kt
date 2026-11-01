package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.DangerousCommandDialog
import com.example.ui.navigation.BottomNavItems
import com.example.ui.navigation.Screen
import com.example.ui.screens.ai.AIScreen
import com.example.ui.screens.files.FilesScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.ssh.ServersScreen
import com.example.ui.screens.terminal.TerminalScreen
import com.example.ui.theme.AITermuxTheme
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalTextDim
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentAccent by remember { mutableStateOf(TerminalCyan) }

            AITermuxTheme(accentColor = currentAccent) {
                MainAppContent(
                    viewModel = viewModel,
                    onAccentChange = { currentAccent = it }
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    onAccentChange: (Color) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val pendingSafetyCheck by viewModel.pendingSafetyCheck.collectAsState()

    val showBottomBar = currentRoute in BottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF090D12),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = TerminalSurface,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .border(1.dp, TerminalBorder.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    BottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val icon = if (selected) screen.activeIcon else screen.inactiveIcon
                                icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00363D),
                                selectedTextColor = TerminalCyan,
                                indicatorColor = TerminalCyan,
                                unselectedIconColor = TerminalTextDim,
                                unselectedTextColor = TerminalTextDim
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onFinish = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinish = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTerminal = { navController.navigate(Screen.Terminal.route) },
                        onNavigateToAI = { navController.navigate(Screen.AI.route) },
                        onNavigateToSSH = { navController.navigate(Screen.Servers.route) },
                        onNavigateToFiles = { navController.navigate(Screen.Files.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }

                composable(Screen.Terminal.route) {
                    TerminalScreen(
                        viewModel = viewModel,
                        onOpenAITool = { tool ->
                            navController.navigate(Screen.AI.route)
                        }
                    )
                }

                composable(Screen.Servers.route) {
                    ServersScreen(
                        viewModel = viewModel,
                        onConnectServer = { server ->
                            navController.navigate(Screen.Terminal.route)
                        }
                    )
                }

                composable(Screen.AI.route) {
                    AIScreen(
                        viewModel = viewModel,
                        onExecuteInTerminal = { command ->
                            viewModel.requestExecuteCommand(command)
                            navController.navigate(Screen.Terminal.route)
                        }
                    )
                }

                composable(Screen.Files.route) {
                    FilesScreen(
                        viewModel = viewModel
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel = viewModel,
                        onRunCommand = { command ->
                            viewModel.requestExecuteCommand(command)
                            navController.navigate(Screen.Terminal.route)
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onAccentChange = onAccentChange
                    )
                }
            }

            // Global Dangerous Command Confirmation Dialog
            pendingSafetyCheck?.let { safety ->
                DangerousCommandDialog(
                    safetyResult = safety,
                    onCancel = { viewModel.cancelDangerousExecution() },
                    onReview = {
                        viewModel.cancelDangerousExecution()
                        navController.navigate(Screen.Terminal.route)
                    },
                    onRunAnyway = { viewModel.confirmDangerousExecution() }
                )
            }
        }
    }
}
