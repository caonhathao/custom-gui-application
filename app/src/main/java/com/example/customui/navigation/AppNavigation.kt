package com.example.customui.navigation


import com.example.customui.ui.layout.MainLayout
import com.example.customui.ui.screens.WelcomeScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.customui.ui.screens.wallpaper.WallpaperDetail

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome",
        enterTransition = { fadeIn(animationSpec = tween(500)) },
        exitTransition = { fadeOut(animationSpec = tween(500)) },
        popEnterTransition = { fadeIn(animationSpec = tween(500)) },
        popExitTransition = { fadeOut(animationSpec = tween(500)) })
    {
        composable("welcome") {
            WelcomeScreen(
                onContinue = {
                    navController.navigate("main") {
                        popUpTo("welcome") {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable("main") {
            MainLayout(
                onToggleTheme = {}
            )
        }
        composable(
            route = "detail/{imageUrl}",
            arguments = listOf(navArgument("imageUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            WallpaperDetail(imageUrl)
        }
    }
}
