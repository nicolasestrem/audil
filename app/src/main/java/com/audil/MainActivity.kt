package com.audil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.audil.presentation.history.HistoryScreen
import com.audil.presentation.home.HomeScreen
import com.audil.presentation.recording.RecordingScreen
import com.audil.presentation.summary.SummaryScreen
import com.audil.ui.components.AudilScaffold
import com.audil.ui.theme.AudilTheme
import dagger.hilt.android.AndroidEntryPoint
import com.audil.presentation.settings.SettingsScreen
import com.audil.presentation.settings.ModelSelectionScreen
import androidx.compose.material.icons.filled.Settings

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudilTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    AudilScaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            // Only show bottom bar on main screens
            if (currentRoute in listOf("home", "recording", "history", "settings")) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Mic, contentDescription = "Record") },
                        label = { Text("Record") },
                        selected = currentRoute == "recording",
                        onClick = {
                            navController.navigate("recording") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = currentRoute == "history",
                        onClick = {
                            navController.navigate("history") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == "settings",
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onRecordClick = { navController.navigate("recording") },
                    onHistoryClick = { navController.navigate("history") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("recording") {
                RecordingScreen()
            }
            composable("history") {
                HistoryScreen(
                    onMeetingClick = { meeting ->
                        navController.navigate("detail/${meeting.id}")
                    }
                )
            }
            composable(
                "detail/{meetingId}",
                arguments = listOf(navArgument("meetingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 0L
                com.audil.presentation.detail.MeetingDetailScreen(
                    meetingId = meetingId,
                    onBack = { navController.popBackStack() },
                    onGenerateSummary = { id ->
                        navController.navigate("summary/$id")
                    }
                )
            }
            composable(
                "summary/{meetingId}",
                arguments = listOf(navArgument("meetingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 0L
                SummaryScreen(
                    meetingId = meetingId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToModelSelection = { navController.navigate("model_selection") }
                )
            }
            composable("model_selection") {
                ModelSelectionScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

