package com.openplaud.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.openplaud.app.ui.detail.RecordingDetailScreen
import com.openplaud.app.ui.list.RecordingListScreen
import com.openplaud.app.ui.login.SetupScreen

object Routes {
    const val SETUP = "setup"
    const val LIST = "list"
    const val DETAIL = "detail/{recordingId}"

    fun detail(id: String) = "detail/$id"
}

@Composable
fun PlaudNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SETUP,
        modifier = modifier
    ) {
        composable(Routes.SETUP) {
            SetupScreen(
                onConfigured = {
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LIST) {
            RecordingListScreen(
                onRecordingClick = { id ->
                    navController.navigate(Routes.detail(id))
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETUP)
                }
            )
        }

        composable(Routes.DETAIL) { backStackEntry ->
            val recordingId = backStackEntry.arguments?.getString("recordingId") ?: ""
            RecordingDetailScreen(
                recordingId = recordingId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
