package com.example.customui.ui.components.modules.assistant_menu_service_helper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.customui.ui.classes.Accessibility
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft

class DirectBackFeature : _interfaceHelper {
    override val name = "Back"

    private val iconDefault = FeatherIcons.ArrowLeft
    private val iconChanged = FeatherIcons.ArrowLeft

    fun performBack() {
        val service = Accessibility.instance ?: return

        // Ưu tiên dùng lệnh hệ thống trước
        val ok = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        if (ok) {
            Log.d("BackFeature", "GLOBAL_ACTION_BACK thành công")
            return
        }

        // Nếu không hiệu lực (gestural navigation hoặc ROM chặn) → giả lập vuốt back
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val displayMetrics = service.resources.displayMetrics
                val width = displayMetrics.widthPixels
                val height = displayMetrics.heightPixels

                // Vuốt từ mép trái màn hình vào (giả lập gesture back)
                val startX = 0f
                val startY = height / 2f
                val endX = width * 0.1f
                val endY = height / 2f

                val path = Path().apply {
                    moveTo(startX, startY)
                    lineTo(endX, endY)
                }

                val stroke = GestureDescription.StrokeDescription(path, 0, 200)
                val gesture = GestureDescription.Builder().addStroke(stroke).build()

                val dispatched = service.dispatchGesture(gesture, null, null)
                Log.d("BackFeature", "Fallback dispatchGesture = $dispatched")
            } catch (e: Exception) {
                Log.e("BackFeature", "dispatchGesture error: ${e.message}", e)
            }
        } else {
            Log.w("BackFeature", "dispatchGesture yêu cầu Android 7.0+")
        }
    }


    override fun toggle(enable: Boolean) {
        Log.d("DirectBackFeature", "Click Back")
        val success = Accessibility.instance?.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_BACK
        ) ?: false
        Log.d("DirectBackFeature", "performGlobalAction result = $success")
    }

    override fun isEnabled(): Boolean = true

    override fun getDefaultIcon(): ImageVector = iconDefault
    override fun getChangedIcon(): ImageVector = iconChanged
}

