package com.example.customui

import androidx.activity.ComponentActivity
import android.os.Bundle
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

        setContent {
            CustomuiTheme {
                AppNavigation()
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
