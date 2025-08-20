package com.example.customui.services.assistantmenu

import android.net.Uri
import android.provider.Settings
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.IntOffset
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.customui.R
import com.example.customui.ui.components.modules.assistant_menu.AssistantMenuModule
import com.example.customui.ui.components.modules.assistant_menu.modules.DirectBackFeature
import com.example.customui.ui.components.modules.assistant_menu.modules.DirectHomeFeature
import com.example.customui.ui.components.modules.assistant_menu.modules.DirectRecentsFeature
import com.example.customui.ui.components.modules.assistant_menu.modules.FlashlightFeature
import com.example.customui.ui.components.modules.assistant_menu.modules.ScreenshotFeature
import com.example.customui.ui.components.modules.assistant_menu.modules.WifiFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.math.pow
import kotlin.math.sqrt

class AssistantMenuService : Service(), LifecycleOwner, ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null

    private var menuOffsetState = mutableStateOf(IntOffset(100, 100))

    private lateinit var composeView: ComposeView
    private lateinit var params: WindowManager.LayoutParams

    private var isMenuExpanded by mutableStateOf(false)

    // --- LifecycleOwner implementation ---
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    // --- ViewModelStoreOwner implementation ---
    private val _viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = _viewModelStore

    // --- SavedStateRegistryOwner implementation ---
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        // Kiểm tra quyền SYSTEM_ALERT_WINDOW
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            // Dừng service nếu không có quyền, hoặc hiển thị thông báo cho người dùng
            stopSelf()
            return
        }

        // Kiểm tra quyền SYSTEM_ALERT_WINDOW
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            // Dừng service nếu không có quyền, hoặc hiển thị thông báo cho người dùng
            stopSelf()
            return
        }

        super.onCreate()
        savedStateRegistryController.performRestore(null) // Restore state
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Khởi tạo ComposeView
        composeView = ComposeView(this).apply {
            //Crucial: Set the ViewTree owners before setContent**
            setViewTreeLifecycleOwner(this@AssistantMenuService)
            setViewTreeViewModelStoreOwner(this@AssistantMenuService)
            setViewTreeSavedStateRegistryOwner(this@AssistantMenuService)
            // Quan trọng: Đảm bảo ComposeView có thể render khi không attach vào Activity
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                // Gọi Composable của bạn ở đây, ví dụ: AssistantMenuModule
                // Bạn sẽ cần truyền trạng thái isMenuExpanded và các callback để xử lý hành động
                var menuManager = AssistantMenuServiceHelper(
                    listOf(
                        WifiFeature(this@AssistantMenuService),
                        FlashlightFeature(this@AssistantMenuService),
                        ScreenshotFeature(this@AssistantMenuService),
                        DirectBackFeature(onCloseMenu = { toggleMenuCloseState() }),
                        DirectHomeFeature(),
                        DirectRecentsFeature()
                    )
                )

                AssistantMenuModule(
                    isExpanded = isMenuExpanded,
                    menuOffset = menuOffsetState.value,
                    onToggleExpand = { toggleMenuOpenState() },
                    onToggleClose = { toggleMenuCloseState() },
                    onCloseMenu = { stopSelf() }, // Ví dụ: đóng service
                    menuManager = menuManager
                )
            }
        }

        floatingView = composeView // Gán composeView cho floatingView

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START

        params.x = menuOffsetState.value.x
        params.y = menuOffsetState.value.y

        windowManager.addView(floatingView, params)
        makeForeground() // Chạy service ở foreground

        setupTouchListener() // Để xử lý kéo thả
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    companion object {
        var instance: AssistantMenuService? = null
        var isMenuExpanded: Boolean = false
    }

    fun closeMenuFromAccessibility() {
        if (isMenuExpanded) {
            toggleMenuCloseState()
        }
    }

    private fun makeForeground() {
        val channelId = "assistant_menu_service"
        val channelName = "Assistant Menu Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_NONE
            )
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Assistant Menu")
            .setContentText("Running...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_NONE
            )
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
        }
        return channelId
    }

    private fun setupTouchListener() {
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()
            private var isDragging = false
            private val CLICK_DRAG_TOLERANCE = 10f // Ngưỡng để phân biệt click và drag

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                // Log.d("Event", isMenuExpanded.toString())
                // Only handle dragging if the menu is collapsed
                if (isMenuExpanded) {
                    return false // Let Compose handle touch events when expanded
                }

                // Xử lý chạm bên ngoài khi menu thu gọn
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    // Người dùng đã chạm bên ngoài view
                    // Bạn có thể thực hiện hành động ở đây, ví dụ: đóng menu nếu nó đang mở
                    // hoặc không làm gì cả nếu bạn chỉ muốn theo dõi
                    Log.d("AssistantMenuService", "Touch outside menu detected")
                    // Quan trọng: Nếu bạn muốn menu đóng lại khi chạm ra ngoài
                    // và menu đang không mở rộng (ví dụ, một icon nhỏ),
                    // bạn có thể không cần làm gì ở đây, vì FLAG_WATCH_OUTSIDE_TOUCH
                    // chủ yếu dùng khi view lớn hơn và bạn muốn biết khi nào chạm ra ngoài nó.
                    // Nếu bạn muốn đóng menu khi nó mở rộng và chạm ra ngoài,
                    // bạn sẽ cần một logic khác vì khi mở rộng, FLAG_WATCH_OUTSIDE_TOUCH bị xóa.
                    return false // Trả về false để các view khác có thể xử lý sự kiện này
                }

                // Check if touch is within the button bounds (48dp circle)
                val density = this@AssistantMenuService.resources.displayMetrics.density
                val buttonRadius = 24 * density // 24dp radius for 48dp button
                val viewCenterX = v.width / 2f
                val viewCenterY = v.height / 2f
                val touchX = event.x
                val touchY = event.y
                val distance = sqrt((touchX - viewCenterX).pow(2) + (touchY - viewCenterY).pow(2))

//                Log.d("Testing", "touchX:$touchX touchY:$touchY")
//                Log.d("Testing", "viewCenterX:$viewCenterX viewCenterY:$viewCenterY")
//                Log.d("Testing", "button radius:$buttonRadius distance:$distance")

                // If touch is outside button area, don't handle it
                if (distance > buttonRadius && event.action == MotionEvent.ACTION_DOWN) {
                    return false // Allow touch to pass through
                }

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false

                        Log.d("Event", "$initialTouchX $initialTouchY")
                        return true // Quan trọng để nhận các event tiếp theo
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY

                        if (Math.abs(deltaX) > CLICK_DRAG_TOLERANCE || Math.abs(deltaY) > CLICK_DRAG_TOLERANCE) {
                            isDragging = true
                        }

                        if (isDragging) {
                            val newX = initialX + deltaX.toInt()
                            val newY = initialY + deltaY.toInt()

                            // KEY FIX: Get screen bounds and constrain properly
                            val metrics = Resources.getSystem().displayMetrics
                            val density = this@AssistantMenuService.resources.displayMetrics.density
                            val iconSize = 48 * density // Approximate icon button size

                            params.x = newX.coerceIn(0, (metrics.widthPixels - iconSize).toInt())
                            params.y = newY.coerceIn(0, (metrics.heightPixels - iconSize).toInt())

                            menuOffsetState.value = (
                                    IntOffset(params.x, params.y)
                                    )

                            windowManager.updateViewLayout(floatingView, params)
                            // Update Compose state
                            //menuOffsetState.value = IntOffset(0, 0) // Reset offset
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        menuOffsetState.value = (
                                IntOffset(params.x, params.y)
                                )
                        if (!isDragging) {
                            toggleMenuOpenState()
                        }
                        isDragging = false
                        return true
                    }

                    MotionEvent.ACTION_OUTSIDE -> {
                        menuOffsetState.value = (
                                IntOffset(params.x, params.y)
                                )
                        toggleMenuCloseState()
                        isDragging = false
                        Log.d("Testing", "Clicked outside")
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun toggleMenuOpenState() {
        isMenuExpanded = true
        Companion.isMenuExpanded = true

        params.width =
            WindowManager.LayoutParams.MATCH_PARENT // Khi mở rộng, chiếm toàn bộ chiều rộng
        params.height =
            WindowManager.LayoutParams.MATCH_PARENT // Khi mở rộng, chiếm toàn bộ chiều cao

        // Remove flags that prevent interaction
        params.flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN


        // QUAN TRỌNG: Đảm bảo params.x và params.y là vị trí hiện tại của icon
        // trước khi cập nhật layout với WRAP_CONTENT.
        val currentOffset = menuOffsetState.value
        params.x = currentOffset.x
        params.y = currentOffset.y

        windowManager.updateViewLayout(floatingView, params)
    }

    private fun toggleMenuCloseState() {
        //Log.d("AssistantMenu", "📍 Closing menu")
        isMenuExpanded = false
        Companion.isMenuExpanded = false

        params.width = WindowManager.LayoutParams.WRAP_CONTENT // Kích thước của icon thu nhỏ
        params.height = WindowManager.LayoutParams.WRAP_CONTENT

        // Add back flags for collapsed state - allows touch-through except on the button
        params.flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

        // QUAN TRỌNG: Đảm bảo params.x và params.y là vị trí hiện tại của icon
        // trước khi cập nhật layout với WRAP_CONTENT.
        val currentOffset = menuOffsetState.value
        params.x = currentOffset.x
        params.y = currentOffset.y

        windowManager.updateViewLayout(floatingView, params)
    }

    // Helper extension for dp to px conversion
    private fun dpToPx(dp: Int): Float {
        return dp * Resources.getSystem().displayMetrics.density
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Trong AssistantMenuService
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START) // Đảm bảo lifecycle được cập nhật
        if (intent?.getBooleanExtra("ACTION_EXPAND", false) == true) {
            if (!isMenuExpanded) {
                toggleMenuOpenState() // Mở rộng menu nếu đang thu nhỏ
            }
        }
        return START_STICKY // Hoặc START_NOT_STICKY tùy theo yêu cầu
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        if (floatingView != null) windowManager.removeView(floatingView)
        serviceJob.cancel()
    }
}
