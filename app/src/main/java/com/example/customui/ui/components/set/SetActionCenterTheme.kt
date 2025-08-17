package com.example.customui.ui.components.set

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import com.example.customui.services.assistantmenu.AssistantMenuService

@RequiresPermission(Manifest.permission.SET_WALLPAPER)
suspend fun setActionCenterTheme(context: Context): Boolean {

    if (Settings.canDrawOverlays(context)) {
        val intent = Intent(context, AssistantMenuService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        return true
    } else {
        // Hướng dẫn người dùng cấp quyền.
        // Có thể mở một Activity để yêu cầu quyền.
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Cần thiết nếu gọi từ non-Activity context
        context.startActivity(intent)
        // Hoặc hiển thị một dialog thông báo
        return false
    }
}
