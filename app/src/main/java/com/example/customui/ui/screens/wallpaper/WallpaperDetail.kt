package com.example.customui.ui.screens.wallpaper

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.customui.data.type.WallpaperDetailResponse
import com.example.customui.data.type.WallpaperDetailType
import com.example.customui.ui.components.loading.LoadingScreen
import com.example.customui.ui.components.set.setWallpaperFromUrl
import com.example.customui.utils.GradientText
import com.example.customui.utils.LinearGradientBrush
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

fun loadWallpaperDetailFromAssets(
    context: Context,
    fileName: String,
    targetId: String
): WallpaperDetailType? {
    val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    val response = Json.decodeFromString<WallpaperDetailResponse>(jsonString)
    return response.data.find { it.id == targetId }
}


@Composable
fun WallpaperDetail(cardID: String) {
    val context = LocalContext.current
    var isApplying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var data by remember { mutableStateOf<WallpaperDetailType?>(null) }

    // Khi composable được gọi, bắt đầu load data
    LaunchedEffect(Unit) {
        delay(2000)
        data = loadWallpaperDetailFromAssets(
            context,
            "wallpaper-detail-fake-data.json",
            targetId = cardID // id cần tìm
        )
        isLoading = false
    }

    if (isLoading) {
        LoadingScreen()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = data?.WallpaperImage[0]?.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(250.dp)
                        .padding(22.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                GradientText(
                    text = data?.name,
                    colors = listOf(
                        Color(0xFF9D6BFF), // tím
                        Color(0xFF00D1FF), // xanh
                        Color(0xFFFF57B9)  // hồng
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        2.dp, LinearGradientBrush(
                            colors = listOf(
                                Color(0xFF9D6BFF), // tím
                                Color(0xFF00D1FF), // xanh
                                Color(0xFFFF57B9)  // hồng
                            )
                        ), RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GradientText(
                    text = "Artist: ${data?.WallpaperDetail?.artist}",
                    colors = listOf(
                        Color(0xFF9D6BFF), // tím
                        Color(0xFF00D1FF), // xanh
                        Color(0xFFFF57B9)  // hồng
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                GradientText(
                    text = "Created at: ${data?.WallpaperDetail?.createdAt}",
                    colors = listOf(
                        Color(0xFF9D6BFF), // tím
                        Color(0xFF00D1FF), // xanh
                        Color(0xFFFF57B9)  // hồng
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                GradientText(
                    text = "Min solution: ${data?.WallpaperDetail?.minSolution}",
                    colors = listOf(
                        Color(0xFF9D6BFF), // tím
                        Color(0xFF00D1FF), // xanh
                        Color(0xFFFF57B9)  // hồng
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                GradientText(
                    text = "Max solution: ${data?.WallpaperDetail?.maxSolution}",
                    colors = listOf(
                        Color(0xFF9D6BFF), // tím
                        Color(0xFF00D1FF), // xanh
                        Color(0xFFFF57B9)  // hồng
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            var result =
                                setWallpaperFromUrl(context, data?.WallpaperImage[0]?.image)
                            withContext(Dispatchers.Main) {
                                isApplying = true
                                Toast.makeText(context, "Wallpaper applied!", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            if (result) isApplying = false
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isApplying = false
                                Toast.makeText(
                                    context,
                                    "Failed to apply wallpaper",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                },
                enabled = !isApplying,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                colors =
                    ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.primary
                    )
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Applying...")
                } else
                    GradientText(
                        text = "Apply",
                        colors = listOf(
                            Color(0xFFFFA5A5),
                            Color(0xFF00D1FF),
                            Color(0xFFFF57B9)
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
            }
        }
    }
}