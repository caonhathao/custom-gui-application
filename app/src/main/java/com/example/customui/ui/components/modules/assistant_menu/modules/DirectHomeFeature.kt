package com.example.customui.ui.components.modules.assistant_menu.modules

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.customui.ui.classes.Accessibility
import compose.icons.FeatherIcons
import compose.icons.feathericons.Home

class DirectHomeFeature : _interfaceHelper {
    override val name = "Home"

    private val iconDefault = FeatherIcons.Home
    private val iconChanged = FeatherIcons.Home
    private val handler = Handler(Looper.getMainLooper())

    private fun performHomeGesture(): Boolean {
        val service = Accessibility.instance ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return try {
                val displayMetrics = service.resources.displayMetrics
                val width = displayMetrics.widthPixels.toFloat()
                val height = displayMetrics.heightPixels.toFloat()

                // Gesture home - vuốt từ dưới lên giữa màn hình
                val startX = width / 2f // Giữa màn hình
                val startY = height - 10f // Rất gần đáy màn hình
                val endX = width / 2f // Giữ nguyên X
                val endY = height * 0.4f // Vuốt lên 60% chiều cao

                val path = Path().apply {
                    moveTo(startX, startY)
                    lineTo(endX, endY)
                }

                val strokeDescription = GestureDescription.StrokeDescription(path, 0, 500)
                val gestureBuilder = GestureDescription.Builder()
                val gesture = gestureBuilder.addStroke(strokeDescription).build()

                val callback = object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        Log.d("HomeFeature", "✅ Home gesture completed successfully")
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        Log.w("HomeFeature", "❌ Home gesture cancelled")
                    }
                }

                val dispatched = service.dispatchGesture(gesture, callback, null)
                Log.d("HomeFeature", "Home gesture dispatched: $dispatched")
                dispatched

            } catch (e: Exception) {
                Log.e("HomeFeature", "Home gesture error: ${e.message}", e)
                false
            }
        } else {
            return false
        }
    }

    private fun performHomeIntent(): Boolean {
        return try {
            val service = Accessibility.instance ?: return false
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            service.startActivity(homeIntent)
            Log.d("HomeFeature", "✅ Home intent started successfully")
            true
        } catch (e: Exception) {
            Log.e("HomeFeature", "Home intent error: ${e.message}", e)
            false
        }
    }

    override fun toggle(enable: Boolean) {
        Log.d("DirectHomeFeature", "🏠 Performing Home action")

        val service = Accessibility.instance
        if (service == null) {
            Log.e("DirectHomeFeature", "❌ Accessibility service is null")
            return
        }

        // Thử gesture trước
        var success = performHomeGesture()

        // Nếu gesture không thành công, thử Intent
        if (!success) {
            Log.d("DirectHomeFeature", "🔄 Gesture failed, trying Intent")
            success = performHomeIntent()
        }

        // Cuối cùng thử global action
        if (!success) {
            Log.d("DirectHomeFeature", "🔄 Intent failed, trying global action")
            val globalSuccess = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            Log.d("DirectHomeFeature", "Global action result: $globalSuccess")
        }
    }

    override fun isEnabled(): Boolean = false
    override fun getDefaultIcon(): ImageVector = iconDefault
    override fun getChangedIcon(): ImageVector = iconChanged
}