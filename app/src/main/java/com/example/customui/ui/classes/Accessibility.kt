package com.example.customui.ui.classes

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.customui.services.assistantmenu.AssistantMenuService
import com.example.customui.utils.NavigationUtils


class Accessibility : AccessibilityService() {

    private val TAG = "MyAccessibilityService"

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return

            Log.d("MyAccessibilityService", "Window changed: $pkg")

            // Nếu menu đang mở và user chuyển sang app khác (không phải overlay app)
            if (AssistantMenuService.isMenuExpanded && pkg != "com.example.customui") {
                AssistantMenuService.instance?.closeMenuFromAccessibility()
                Log.d("MyAccessibilityService", "📌 Auto closed menu when switching to $pkg")
            }
        }

        // Chỉ log các event quan trọng để tránh spam log
        when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d(TAG, "Window changed: ${event.packageName}")
            }
            // Bỏ comment dòng dưới nếu muốn log tất cả events
            // else -> Log.d(TAG, "Event: ${event?.toString()}")
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt")
    }

    companion object {
        var instance: Accessibility? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "✅ Accessibility Service Connected Successfully")

        // Log thông tin navigation
        val navInfo = NavigationUtils.getNavigationInfo(this)
        Log.d(TAG, "📱 Navigation Info:\n$navInfo")

        // Test global actions (tùy chọn - có thể comment lại)
        // NavigationUtils.testGlobalActions(this)

        // Cấu hình service info nếu cần
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }

        // Uncomment nếu muốn override XML config
        // this.serviceInfo = info
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "🔌 Accessibility Service Disconnected")
        instance = null
        return super.onUnbind(intent)
    }

    // Method để test trực tiếp từ code khác
    fun testNavigationActions() {
        Log.d(TAG, "🧪 Testing all navigation actions...")

        val actions = listOf(
            "BACK" to GLOBAL_ACTION_BACK,
            "HOME" to GLOBAL_ACTION_HOME,
            "RECENTS" to GLOBAL_ACTION_RECENTS
        )

        actions.forEach { (name, action) ->
            val result = performGlobalAction(action)
            Log.d(TAG, "🎯 $name action result: $result")
        }
    }
}