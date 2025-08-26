package com.example.customui.ui.components.modules.assistant_menu

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.provider.Settings
import android.util.Log

object NavigationUtils {

    /**
     * Kiểm tra thiết bị có đang sử dụng gesture navigation không
     */
    fun isGestureNavigationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resourceId = Resources.getSystem().getIdentifier(
                    "config_navBarInteractionMode", "integer", "android"
                )
                if (resourceId > 0) {
                    val mode = Resources.getSystem().getInteger(resourceId)
                    Log.d("NavigationUtils", "Navigation mode: $mode (2=gesture, 0=3-button)")
                    mode == 2 // 2 means gesture navigation
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("NavigationUtils", "Error checking navigation mode", e)
                false
            }
        } else {
            false
        }
    }

    /**
     * Lấy thông tin chi tiết về navigation bar
     */
    fun getNavigationInfo(context: Context): String {
        val info = StringBuilder()

        try {
            val isGesture = isGestureNavigationEnabled(context)
            info.append("Gesture Navigation: $isGesture\n")

            // Kiểm tra navigation bar height
            val resourceId = Resources.getSystem().getIdentifier(
                "navigation_bar_height", "dimen", "android"
            )
            if (resourceId > 0) {
                val height = Resources.getSystem().getDimensionPixelSize(resourceId)
                info.append("Navigation Bar Height: ${height}px\n")
            }

            // Kiểm tra screen size
            val displayMetrics = context.resources.displayMetrics
            info.append("Screen: ${displayMetrics.widthPixels} x ${displayMetrics.heightPixels}\n")
            info.append("Density: ${displayMetrics.density}\n")

        } catch (e: Exception) {
            Log.e("NavigationUtils", "Error getting navigation info", e)
            info.append("Error getting info: ${e.message}\n")
        }

        return info.toString()
    }

    /**
     * Test các global actions
     */
    fun testGlobalActions(service: android.accessibilityservice.AccessibilityService) {
        Log.d("NavigationUtils", "=== Testing Global Actions ===")

        val actions = mapOf(
            "BACK" to android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK,
            "HOME" to android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME,
            "RECENTS" to android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS,
            "NOTIFICATIONS" to android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS,
            "QUICK_SETTINGS" to android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
        )

        actions.forEach { (name, action) ->
            try {
                val result = service.performGlobalAction(action)
                Log.d("NavigationUtils", "$name: $result")
            } catch (e: Exception) {
                Log.e("NavigationUtils", "$name failed: ${e.message}")
            }
        }
    }
}

