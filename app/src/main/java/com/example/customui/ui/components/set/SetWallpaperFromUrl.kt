package com.example.customui.ui.components.set

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.RequiresPermission
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

@RequiresPermission(Manifest.permission.SET_WALLPAPER)
suspend fun setWallpaperFromUrl(context: Context, imageUrl: String): Boolean {
    return try {
        // Tải ảnh bằng Coil
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false) // cần Bitmap
            .build()

        val result = (loader.execute(request) as SuccessResult).drawable
        val bitmap = (result as Drawable).toBitmap()

        // Đặt làm hình nền
        val wallpaperManager = WallpaperManager.getInstance(context)
        wallpaperManager.setBitmap(bitmap)

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
