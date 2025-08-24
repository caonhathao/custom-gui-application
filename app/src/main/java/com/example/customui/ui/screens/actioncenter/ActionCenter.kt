package com.example.customui.ui.screens.actioncenter

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.customui.data.type.ActionCenterData
import com.example.customui.data.type.WallpaperData
import com.example.customui.ui.components.modules.elements.ImageCard
import com.example.customui.ui.components.loading.LoadingScreen
import com.example.customui.ui.screens.wallpaper.loadWallpapersFromAssets
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

fun loadActionCentersFromAssets(context: Context, fileName: String): ActionCenterData {
    val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    val response = Json.decodeFromString<ActionCenterData>(jsonString)
    return response
}

@Composable
fun ActionCenter(onCardClick: (String) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.coerceAtLeast(1) // tránh 0
    val columnCount = maxOf(1, screenWidth / 180) // mỗi ô rộng ~180dp

    //Log.d("HomeScreen", "screenWidthDp = $screenWidth, columnCount = $columnCount")

    var isLoading by remember { mutableStateOf(true) }
    var actionCenters by remember { mutableStateOf<ActionCenterData?>(null) }
    val context= LocalContext.current

    // Khi composable được gọi, bắt đầu load data
    LaunchedEffect(Unit) {
        // Giả lập gọi API
        delay(2000)
        actionCenters = loadActionCentersFromAssets(context, "actioncenter-fake-data.json")

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
            actionCenters?.let { data ->
                items(data.data) { item ->
                    ImageCard(
                        imageName = item.name,
                        imageLink = item.WallpaperImage[0].image
                    ) {
                        onCardClick(item.id)
                    }
                }
            }
        }
    }
}