package com.example.customui.ui.components.modules.assistant_menu.modules

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.customui.ui.classes.Accessibility
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft

class DirectBackFeature(private val onCloseMenu: () -> Unit) : _interfaceHelper {
    override val name = "Back"

    private val iconDefault = FeatherIcons.ArrowLeft
    private val iconChanged = FeatherIcons.ArrowLeft
    private val handler = Handler(Looper.getMainLooper())

    private fun performBackGesture(): Boolean {
        val service = Accessibility.instance ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return try {
                val displayMetrics = service.resources.displayMetrics
                val width = displayMetrics.widthPixels.toFloat()
                val height = displayMetrics.heightPixels.toFloat()

                // Tạo gesture back - vuốt từ mép trái vào trong
                val startX = 1f // Rất gần mép trái
                val startY = height / 2f // Giữa màn hình
                val endX = width * 0.5f // Vuốt vào 50% màn hình
                val endY = height / 2f // Giữ nguyên Y

                val path = Path().apply {
                    moveTo(startX, startY)
                    lineTo(endX, endY)
                }

                // Tạo gesture với thời gian phù hợp
                val strokeDescription = GestureDescription.StrokeDescription(path, 0, 350)
                val gestureBuilder = GestureDescription.Builder()
                val gesture = gestureBuilder.addStroke(strokeDescription).build()

                // Callback để theo dõi kết quả
                val callback = object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        Log.d("BackFeature", "✅ Back gesture completed successfully")
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        Log.w("BackFeature", "❌ Back gesture cancelled")
                    }
                }

                val dispatched = service.dispatchGesture(gesture, callback, null)
                Log.d("BackFeature", "Back gesture dispatched: $dispatched")
                dispatched

            } catch (e: Exception) {
                Log.e("BackFeature", "Back gesture error: ${e.message}", e)
                false
            }
        } else {
            Log.w("BackFeature", "Gesture API requires Android N+")
            return false
        }
    }

    override fun toggle(enable: Boolean) {
        onCloseMenu()
        Log.d("DirectBackFeature", "🔄 Performing Back action")

        val service = Accessibility.instance
        if (service == null) {
            Log.e("DirectBackFeature", "❌ Accessibility service is null")
            return
        }

        // Ưu tiên sử dụng gesture thay vì global action
        val gestureSuccess = performBackGesture()

        // Nếu gesture không thành công, thử global action
        if (!gestureSuccess) {
            Log.d("DirectBackFeature", "🔄 Gesture failed, trying global action")
            val globalSuccess = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            Log.d("DirectBackFeature", "Global action result: $globalSuccess")
        }
    }

    override fun isEnabled(): Boolean = false
    override fun getDefaultIcon(): ImageVector = iconDefault
    override fun getChangedIcon(): ImageVector = iconChanged
}

