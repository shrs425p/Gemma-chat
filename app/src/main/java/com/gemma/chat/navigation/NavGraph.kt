package com.gemma.chat.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gemma.chat.ui.chat.ChatScreen
import com.gemma.chat.ui.history.HistoryScreen
import com.gemma.chat.ui.settings.SettingsScreen
import com.gemma.chat.ui.setup.SetupScreen
import com.gemma.chat.ui.setup.SetupViewModel

@Composable
fun GemmaChatNavGraph() {
    val navController = rememberNavController()
    val setupViewModel: SetupViewModel = hiltViewModel()
    val isModelReady by setupViewModel.isModelReady.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isModelReady) Screen.History.route else Screen.Setup.route
    ) {
        composable(Screen.Setup.route) {
            SetupScreen(
                viewModel = setupViewModel,
                onModelReady = {
                    navController.navigate(Screen.History.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNewChat = { sessionId ->
                    navController.navigate(Screen.Chat.createRoute(sessionId))
                },
                onChatSelected = { sessionId ->
                    navController.navigate(Screen.Chat.createRoute(sessionId))
                },
                onSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
            ChatScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onChangeModel = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
