package com.example.customui.ui.classes

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class Accessibility : AccessibilityService() {

    private val TAG = "MyAccessibilityService"

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Xử lý các sự kiện accessibility ở đây
        // Ví dụ: event?.eventType, event?.source
        Log.d(TAG, "onAccessibilityEvent: " + event?.toString())
    }

    override fun onInterrupt() {
        // Được gọi khi hệ thống muốn ngắt phản hồi của service
        Log.d(TAG, "onInterrupt")
    }

    companion object {
        var instance: Accessibility? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Được gọi khi hệ thống kết nối thành công với service
        // Đây là nơi tốt để thực hiện các cài đặt ban đầu
        instance = this
        Log.d(TAG, "onServiceConnected")

        val info = AccessibilityServiceInfo()
        // Thiết lập các thuộc tính cho service của bạn
        // Ví dụ:
        // info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED
        // info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
        // info.notificationTimeout = 100
        // info.flags = AccessibilityServiceInfo.DEFAULT or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS

        // Hoặc bạn có thể thiết lập các thuộc tính này trong file XML như đã đề cập ở trên.
        // Nếu bạn đã định nghĩa trong XML, dòng này có thể không cần thiết hoặc dùng để ghi đè.
        // this.serviceInfo = info
    }

    // (Tùy chọn) Bạn có thể thêm các phương thức để hiển thị menu nổi của mình ở đây
    // Ví dụ: sử dụng WindowManager để thêm một view lên trên các ứng dụng khác.

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        // Dọn dẹp tài nguyên nếu cần
        instance = null
        return super.onUnbind(intent)
    }
}
