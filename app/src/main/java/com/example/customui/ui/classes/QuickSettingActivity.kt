package com.example.customui.ui.classes

import android.content.Intent
import android.os.Bundle
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.customui.services.assistantmenu.AssistantMenuService

class QuickSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kiểm tra và yêu cầu quyền SYSTEM_ALERT_WINDOW nếu cần
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            // Bạn cần một ActivityResultLauncher để xử lý kết quả
            // startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE) // Cách cũ
            // Nên dùng ActivityResultLauncher
            getOverlayPermission.launch(intent)
        } else {
            startAssistantMenuServiceAndExpand()
        }
        // finish() // Có thể đóng Activity này ngay sau khi khởi động Service
    }

    private val getOverlayPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(this)) {
                startAssistantMenuServiceAndExpand()
            } else {
                Toast.makeText(
                    this,
                    "Quyền hiển thị trên ứng dụng khác là cần thiết.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

    private fun startAssistantMenuServiceAndExpand() {
        val serviceIntent = Intent(this, AssistantMenuService::class.java)
        // Bạn có thể thêm một extra vào Intent để báo Service mở rộng menu
        serviceIntent.putExtra("ACTION_EXPAND", true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        finish() // Đóng Activity sau khi yêu cầu Service
    }

}