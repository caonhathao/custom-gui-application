package com.example.customui

import androidx.activity.ComponentActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import com.example.customui.navigation.AppNavigation
import com.example.customui.services.modules.ScreenshotPermissionHelper
import com.example.customui.ui.theme.CustomuiTheme

class MainActivity : ComponentActivity() {
    private lateinit var screenshotHelper: ScreenshotPermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Khởi tạo helper sớm
        screenshotHelper = ScreenshotPermissionHelper(this)

        // Check if we need to request screenshot permission
        if (intent.getBooleanExtra("REQUEST_SCREENSHOT_PERMISSION", false)) {
            requestScreenshotPermission()
        }

        setContent {
            CustomuiTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Check if we need to request screenshot permission
        if (intent.getBooleanExtra("REQUEST_SCREENSHOT_PERMISSION", false)) {
            requestScreenshotPermission()
        }
    }

    private fun requestScreenshotPermission() {
        screenshotHelper.requestPermission { granted ->
            if (granted) {
                Toast.makeText(this, "Screenshot permission granted!", Toast.LENGTH_SHORT).show()
                // Close MainActivity and return to floating menu
                finish()
            } else {
                Toast.makeText(this, "Screenshot permission denied!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun requestServicePermissions(
        services: List<String>,
        onDone: (Boolean) -> Unit
    ) {
        var allGranted = true
        var remaining = services.size

        services.forEach { service ->
            when (service) {
                "Screenshot" -> {
                    screenshotHelper.requestPermission { granted ->
                        if (!granted) allGranted = false
                        if (--remaining == 0) onDone(allGranted)
                    }
                }
                // sau này thêm service khác thì thêm case ở đây
                else -> {
                    if (--remaining == 0) onDone(allGranted)
                }
            }
        }

        // nếu danh sách rỗng thì gọi luôn callback
        if (services.isEmpty()) {
            onDone(true)
        }
    }
}