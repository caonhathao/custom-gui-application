package com.example.customui.ui.components.modules.assistant_menu.modules

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.customui.ui.classes.Accessibility
import compose.icons.FeatherIcons
import compose.icons.feathericons.Square

class DirectRecentsFeature : _interfaceHelper {
    override val name = "Recents"

    private val iconDefault = FeatherIcons.Square
    private val iconChanged = FeatherIcons.Square
    private val handler = Handler(Looper.getMainLooper())

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performRecentsGesture(): Boolean {
        val service = Accessibility.instance ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return try {
                val displayMetrics = service.resources.displayMetrics
                val width = displayMetrics.widthPixels.toFloat()
                val height = displayMetrics.heightPixels.toFloat()

                // Gesture recents - vuốt từ dưới lên và dừng ở giữa (không vuốt hết)
                val startX = width / 3f
                val startY = height - 1f
                val midX = width / 3f
                val midY = height * 0.9f // Dừng ở giữa màn hình

                val path = Path().apply {
                    moveTo(startX, startY)
                    lineTo(midX, midY)
                }

                // Stroke 1: Vuốt đến điểm giữ
                // Tham số cuối cùng (willContinue) là true để báo rằng cử chỉ sẽ tiếp tục
                val stroke1 = GestureDescription.StrokeDescription(
                    path,
                    0L, // Bắt đầu ngay
                    300,
                    true // Sẽ có stroke tiếp theo tiếp nối cử chỉ này
                )

                // Path cho hành động giữ (thực chất là một điểm, không di chuyển)
                // Stroke tiếp theo sẽ bắt đầu từ điểm cuối của stroke1
                val holdPath = Path().apply {
                    moveTo(midX, midY) // Bắt đầu tại điểm giữ
                    lineTo(midX, midY) // Kết thúc cũng tại điểm giữ (không di chuyển)
                }

                // Stroke 2: Giữ tại điểm đó
                // startTime cho stroke này là thời gian kết thúc của stroke trước đó
                val stroke2 = GestureDescription.StrokeDescription(
                    holdPath,
                    300, // Bắt đầu SAU KHI stroke1 hoàn thành
                    300,
                    false // Đây là stroke cuối cùng của cử chỉ này
                )

                val gestureBuilder = GestureDescription.Builder()
                gestureBuilder.addStroke(stroke1)
                gestureBuilder.addStroke(stroke2) // Thêm stroke thứ hai

                val gesture = gestureBuilder.build()

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

    @RequiresApi(Build.VERSION_CODES.O)
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

    override fun isEnabled(): Boolean = false
    override fun getDefaultIcon(): ImageVector = iconDefault
    override fun getChangedIcon(): ImageVector = iconChanged
}