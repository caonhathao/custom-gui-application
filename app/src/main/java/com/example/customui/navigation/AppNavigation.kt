package com.example.customui.navigation


import android.net.Uri
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
import com.example.customui.ui.screens.actioncenter.ActionCenterDetail
import com.example.customui.ui.screens.wallpaper.WallpaperDetail

@Composable
fun AppNavigation(
    onToggleTheme: () -> Unit = {}
) {
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
                onOpenDetail = {type, imageUrl ->
                    navController.navigate("detail/${type}/${Uri.encode(imageUrl)}")
                }
            )
        }
        composable(
            route = "detail/{type}/{cardID}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("cardID") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            val cardID = backStackEntry.arguments?.getString("cardID") ?: ""

            when (type) {
                "wallpaper" -> WallpaperDetail(cardID)
                "action-center" -> ActionCenterDetail(cardID)
            }
        }

    }
}
