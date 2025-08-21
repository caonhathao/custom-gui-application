package com.example.customui.ui.layout

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material.icons.sharp.Menu
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.customui.ui.screens.actioncenter.ActionCenter
import com.example.customui.ui.screens.actioncenter.ActionCenterDetail
import com.example.customui.ui.screens.wallpaper.WallpaperDetail
import com.example.customui.ui.screens.wallpaper.WallpaperScreen
import com.example.customui.ui.screens.widgets.Widgets
import com.example.customui.utils.GradientText
import compose.icons.FeatherIcons
import compose.icons.feathericons.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    onOpenDetail: (String) -> Unit,
    onToggleTheme: () -> Unit = {}
) {
    val navController = rememberNavController()
    var selectedIndex by remember { mutableIntStateOf(0) }
    val routes = listOf("wallpapers", "actioncenter", "widgets")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    GradientText(
                        text = "WALLPAPER WORLD",
                        colors = listOf(
                            Color(0xFF9D6BFF), // tím
                            Color(0xFF00D1FF), // xanh
                            Color(0xFFFF57B9)  // hồng
                        ),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = Color.DarkGray
                ),
                actions = {
                    IconButton(
                        onClick = {},
                        colors = IconButtonColors(
                            containerColor = Color.Transparent, // Màu xanh
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.Transparent
                        )
                    ) {
                        Icon(Icons.Sharp.Info, contentDescription = "Thông tin")
                    }
                    IconButton(
                        onClick = {},
                        colors = IconButtonColors(
                            containerColor = Color.Transparent, // Màu xanh
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Góp ý")
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    var items = listOf("Wallpapers", "Action Center", "Widgets")
                    var icons = listOf(FeatherIcons.Image, Icons.Sharp.Menu, Icons.Sharp.Settings)
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        items.forEachIndexed { index, label ->
                            val isSelected = selectedIndex == index
                            Button(
                                onClick = {
                                    selectedIndex = index
                                    navController.navigate(routes[index]) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = if (!isSelected) BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.secondary
                                ) else null,
                                colors = if (isSelected) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.secondary
                                    )
                                },
                                modifier = Modifier
                                    .animateContentSize()
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = icons[index],
                                    contentDescription = label
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(label)
                                }
                            }
                        }
                    }
                },
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "wallpapers",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("wallpapers") {
                WallpaperScreen() { imageUrl ->
                    onOpenDetail(imageUrl)                }
            }

            composable("actioncenter") {
                ActionCenter() { imageUrl ->
                    onOpenDetail(imageUrl)
                }
            }

            composable("widgets") {
                Widgets()
            }
        }
    }
}