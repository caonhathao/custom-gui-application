package com.example.customui.ui.screens.actioncenter

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.customui.ui.components.modules.assistant_menu.modules.ListFeatures
import com.example.customui.ui.components.set.setActionCenterTheme
import com.example.customui.utils.GradientText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ActionCenterDetail(imageLink: String) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var isApplying by remember { mutableStateOf(false) }

    var listFeatures = ListFeatures()

    var services: MutableList<String> = mutableListOf()
    services = (services + "Screenshot") as MutableList<String>

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = imageLink),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(250.dp)
                    .padding(22.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            GradientText(
                text = "Wallpaper",
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
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GradientText(
                text = "Features",
                colors = listOf(
                    Color(0xFF9D6BFF),
                    Color(0xFF00D1FF),
                    Color(0xFFFF57B9)
                ),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // 4 cột cố định
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        2.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF9D6BFF), // tím
                                Color(0xFF00D1FF), // xanh
                                Color(0xFFFF57B9)  // hồng
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                val data = listFeatures.getFeatures()

                items(data.size) { index ->
                    val value = data[index]
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .aspectRatio(1f) // giữ box vuông đều
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = value.second,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = value.first,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                    }
                }
            }
        }

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        (activity as? ComponentActivity)?.let { nonNullActivity ->
                            val result = nonNullActivity.setActionCenterTheme(services)
                            withContext(Dispatchers.Main) {
                                isApplying = true
                                Toast.makeText(context, "Wallpaper applied!", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            if (result) isApplying = false
                        }
                    } catch (e: Exception) {
                        Log.d("Exception", "Exception: $e")
                        withContext(Dispatchers.Main) {
                            isApplying = false
                            Toast.makeText(context, "Failed to apply wallpaper", Toast.LENGTH_SHORT)
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
                Text(
                    text = "Apply",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
        }
    }
}