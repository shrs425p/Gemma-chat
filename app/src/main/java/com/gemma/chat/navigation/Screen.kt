package com.gemma.chat.navigation

sealed class Screen(val route: String) {
    data object Setup : Screen("setup")
    data object History : Screen("history")
    data object Chat : Screen("chat/{sessionId}") {
        fun createRoute(sessionId: Long) = "chat/$sessionId"
    }
    data object Settings : Screen("settings")
}
