package com.example.customui.ui.components.set

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import com.example.customui.ui.classes.Accessibility
import android.view.accessibility.AccessibilityManager
import com.example.customui.MainActivity
import com.example.customui.services.assistantmenu.AssistantMenuService
import kotlinx.coroutines.CompletableDeferred

fun isAccessibilityServiceEnabled(
    context: Context, service: Class<out AccessibilityService>
): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(
                ComponentName(context, service).flattenToString(), ignoreCase = true
            )
        ) {
            return true
        }
    }
    return false
}

@RequiresPermission(Manifest.permission.SET_WALLPAPER)
suspend fun ComponentActivity.setActionCenterTheme(services: List<String>): Boolean {
    val context = this

    // ---- B1: Check Overlay ----
    if (!Settings.canDrawOverlays(context)) {
        val granted = requestOverlayPermissionSuspend()
        if (!granted) {
            Toast.makeText(
                context, "Overlay permission is required.", Toast.LENGTH_LONG
            ).show()
            return false
        }
    }
    Log.d("Main", "Overlay permission OK")

    // ---- B2: Check Accessibility ----
    if (!isAccessibilityServiceEnabled(context, Accessibility::class.java)) {
        val granted = requestAccessibilityPermissionSuspend()
        if (!granted) {
            Toast.makeText(
                context, "Accessibility Service permission is required.", Toast.LENGTH_LONG
            ).show()
            return false
        }
    }
    Log.d("Main", "Accessibility permission OK")

// ---- B3: Check service's permission ----
    val activity = this as? MainActivity
    if (activity != null) {
        val granted = CompletableDeferred<Boolean>()

        activity.requestServicePermissions(services) { ok ->
            granted.complete(ok)
        }

        if (!granted.await()) {
            Log.e("Main", "Some permissions denied")
            return false
        }
    }

    // ---- B4: Action ----
    val service = Accessibility.instance
    return if (service != null) {
        val serviceIntent = Intent(this, AssistantMenuService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Log.d("MainActivity", "Attempting to start AssistantMenuService.")
        true
    } else {
        Log.e("MainActivity", "Accessibility service not available")
        Toast.makeText(context, "Accessibility service not connected", Toast.LENGTH_SHORT).show()
        false
    }
}

