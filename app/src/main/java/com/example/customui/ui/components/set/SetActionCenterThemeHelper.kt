package com.example.customui.ui.components.set

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.customui.ui.classes.Accessibility
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun ComponentActivity.requestOverlayPermissionSuspend(): Boolean =
    suspendCoroutine { cont ->
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            cont.resume(Settings.canDrawOverlays(this))
        }
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        launcher.launch(intent)
    }

suspend fun ComponentActivity.requestAccessibilityPermissionSuspend(): Boolean =
    suspendCoroutine { cont ->
        // Vì Android không có API callback trực tiếp khi user bật accessibility
        // nên cách thường dùng là mở settings rồi polling lại sau một khoảng delay.
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        // Đợi user thao tác rồi kiểm tra lại
        lifecycleScope.launch {
            repeat(10) { // check 10 lần mỗi 1 giây
                delay(1000)
                if (isAccessibilityServiceEnabled(this@requestAccessibilityPermissionSuspend, Accessibility::class.java)) {
                    cont.resume(true)
                    return@launch
                }
            }
            cont.resume(false)
        }
    }
