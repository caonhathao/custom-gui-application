package com.example.customui.ui.components.modules.assistant_menu.assistant_menu_service_helper

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
import compose.icons.feathericons.Square

class DirectRecentsFeature : _interfaceHelper {
    override val name = "Recents"

    private val iconDefault = FeatherIcons.Square
    private val iconChanged = FeatherIcons.Square
    private val handler = Handler(Looper.getMainLooper())

    private fun performRecentsGesture(): Boolean {
        val service = Accessibility.instance ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return try {
                val displayMetrics = service.resources.displayMetrics
                val width = displayMetrics.widthPixels.toFloat()
                val height = displayMetrics.heightPixels.toFloat()

                // Gesture recents - vuốt từ dưới lên và dừng ở giữa (không vuốt hết)
                val startX = width / 2f
                val startY = height - 10f
                val midX = width / 2f
                val midY = height * 0.5f // Dừng ở giữa màn hình

                val path = Path().apply {
                    moveTo(startX, startY)
                    lineTo(midX, midY)
                }

                // Recents gesture cần thời gian dài hơn và có thể cần "hold"
                val strokeDescription = GestureDescription.StrokeDescription(path, 0, 600)
                val gestureBuilder = GestureDescription.Builder()
                val gesture = gestureBuilder.addStroke(strokeDescription).build()

                val callback = object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        Log.d("RecentsFeature", "✅ Recents gesture completed successfully")
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        Log.w("RecentsFeature", "❌ Recents gesture cancelled")
                    }
                }

                val dispatched = service.dispatchGesture(gesture, callback, null)
                Log.d("RecentsFeature", "Recents gesture dispatched: $dispatched")
                dispatched

            } catch (e: Exception) {
                Log.e("RecentsFeature", "Recents gesture error: ${e.message}", e)
                false
            }
        } else {
            return false
        }
    }

    override fun toggle(enable: Boolean) {
        Log.d("DirectRecentsFeature", "📱 Performing Recents action")

        val service = Accessibility.instance
        if (service == null) {
            Log.e("DirectRecentsFeature", "❌ Accessibility service is null")
            return
        }

        // Thử gesture trước
        val gestureSuccess = performRecentsGesture()

        // Nếu gesture không thành công, thử global action
        if (!gestureSuccess) {
            Log.d("DirectRecentsFeature", "🔄 Gesture failed, trying global action")
            val globalSuccess = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            Log.d("DirectRecentsFeature", "Global action result: $globalSuccess")
        }
    }

    override fun isEnabled(): Boolean = true
    override fun getDefaultIcon(): ImageVector = iconDefault
    override fun getChangedIcon(): ImageVector = iconChanged
}