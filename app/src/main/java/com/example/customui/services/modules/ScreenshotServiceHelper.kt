package com.example.customui.services.modules

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

class ScreenshotPermissionHelper(
    private val activity: ComponentActivity
) {
    private val projectionManager: MediaProjectionManager =
        activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    private var continuation: (Boolean) -> Unit = {}

    private val projectionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                try {
                    // Start foreground service FIRST with correct type
                    val intent = Intent(activity, ScreenshotService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        activity.startForegroundService(intent)
                    } else {
                        activity.startService(intent)
                    }

                    // Small delay to ensure service is started
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            val projection = projectionManager.getMediaProjection(result.resultCode, result.data!!)
                            ScreenshotService.setProjection(projection)
                            continuation(true)
                            Toast.makeText(activity, "MediaProjection granted!", Toast.LENGTH_SHORT).show()
                        } catch (e: SecurityException) {
                            Log.e("ScreenshotHelper", "Failed to create MediaProjection: ${e.message}")
                            continuation(false)
                            Toast.makeText(activity, "Failed to setup screen capture", Toast.LENGTH_SHORT).show()
                        }
                    }, 100) // 100ms delay

                } catch (e: Exception) {
                    Log.e("ScreenshotHelper", "Error starting service: ${e.message}")
                    continuation(false)
                    Toast.makeText(activity, "Failed to start screenshot service", Toast.LENGTH_SHORT).show()
                }
            } else {
                continuation(false)
                Toast.makeText(activity, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }

    fun requestPermission(onResult: (Boolean) -> Unit) {
        continuation = onResult
        val intent = projectionManager.createScreenCaptureIntent()
        projectionLauncher.launch(intent)
    }
}