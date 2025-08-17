package com.example.customui

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri // Required for ACTION_MANAGE_OVERLAY_PERMISSION Uri
import android.os.Build // Required for checking SDK version
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.customui.ui.theme.CustomuiTheme
import com.example.customui.navigation.AppNavigation
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Toast // For showing messages to the user
import com.example.customui.services.assistantmenu.AssistantMenuService // **Import your Service**
import com.example.customui.ui.classes.Accessibility

fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<out AccessibilityService>): Boolean {
    val am = context.getSystemService(AccessibilityManager::class.java) ?: return false
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices ?: "")
    val expectedComponentName = ComponentName(context, serviceClass).flattenToString()
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedComponentName, ignoreCase = true)) {
            return true
        }
    }
    return false
}


class MainActivity : ComponentActivity() {

    private val requestOverlayPermissionLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(this)) {
                checkAndRequestAccessibilityPermission()
            } else {
                Toast.makeText(
                    this,
                    "Overlay permission is required to use the Assistant Menu.",
                    Toast.LENGTH_LONG
                ).show()
                Log.w("MainActivity", "Overlay permission not granted after request.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomuiTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        checkAndRequestOverlayAndAccessibility()
                    }) {
                        Text(text = "Bật menu trợ năng")
                    }

                    Button(onClick = {
                        setContent {
                            CustomuiTheme {
                                AppNavigation()
                            }
                        }
                    }) {
                        Text(text = "Vào ứng dụng")
                    }
                }
            }
        }
    }

    private fun checkAndRequestOverlayAndAccessibility() {
        // B1: check overlay trước
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            requestOverlayPermissionLauncher.launch(intent)
            return
        }

        // B2: check accessibility
        checkAndRequestAccessibilityPermission()
    }

    private fun checkAndRequestAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled(this, Accessibility::class.java)) {
            Toast.makeText(
                this,
                "Vui lòng bật Accessibility Service cho ứng dụng.",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            return
        }

        // Nếu đủ quyền thì start service
        startAssistantMenuService()
    }

    private fun startAssistantMenuService() {
        val serviceIntent = Intent(this, AssistantMenuService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Log.d("MainActivity", "Attempting to start AssistantMenuService.")
    }
}
