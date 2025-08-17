package com.example.customui.ui.screens.actioncenter

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.customui.ui.components.modules.elements.ImageCard
import com.example.customui.ui.components.loading.LoadingScreen
import kotlinx.coroutines.delay

@Composable
fun ActionCenter(onCardClick: (String) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.coerceAtLeast(1) // tránh 0
    val columnCount = maxOf(1, screenWidth / 180) // mỗi ô rộng ~180dp

    Log.d("HomeScreen", "screenWidthDp = $screenWidth, columnCount = $columnCount")

    var isLoading by remember { mutableStateOf(true) }
    var wallpapers by remember { mutableStateOf(listOf<String>()) }

    // Khi composable được gọi, bắt đầu load data
    LaunchedEffect(Unit) {
        // Giả lập gọi API
        delay(2000)
        wallpapers = listOf(
            "https://i.pinimg.com/736x/9a/0a/73/9a0a736bde08c6e488eb640428802d97.jpg",
            "https://i.pinimg.com/736x/9a/0a/73/9a0a736bde08c6e488eb640428802d97.jpg"
        )
        isLoading = false
    }

    if (isLoading) {
        LoadingScreen()
    } else {

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(wallpapers) { imageUrl ->
                ImageCard(
                    "https://i.pinimg.com/736x/9a/0a/73/9a0a736bde08c6e488eb640428802d97.jpg",
                    "Action center"
                ) {
                    onCardClick(imageUrl)
                }
            }
        }
    }
}