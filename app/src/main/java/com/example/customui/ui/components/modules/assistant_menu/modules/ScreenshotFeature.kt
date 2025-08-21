package com.example.customui.ui.components.modules.assistant_menu.modules

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.customui.MainActivity
import com.example.customui.services.modules.ScreenshotService
import compose.icons.FeatherIcons
import compose.icons.feathericons.Image
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.createBitmap

class ScreenshotFeature(
    private val context: Context
) : _interfaceHelper {

    override val name = "Screenshot"
    private val iconDefault = FeatherIcons.Image
    private val iconChanged = FeatherIcons.Image

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val backgroundThread = HandlerThread("ScreenshotBackground").apply { start() }
    private val backgroundHandler = Handler(backgroundThread.looper)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var lastToastTime = 0L
    private val TOAST_THROTTLE_MS = 2000L

    override fun toggle(enable: Boolean) {
        val mediaProjection = ScreenshotService.mediaProjection
        if (mediaProjection == null) {
            Log.e("ScreenshotFeature", "MediaProjection is null. Requesting permission...")
            requestScreenshotPermission()
            return
        }
        takeScreenshot(mediaProjection)
    }

    private fun requestScreenshotPermission() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("REQUEST_SCREENSHOT_PERMISSION", true)
        }
        context.startActivity(intent)
        showToast("Opening permission request...")
    }

    private fun takeScreenshot(mediaProjection: MediaProjection) {
        try {
            val callback = object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.d("ScreenshotFeature", "MediaProjection stopped by system")
                }
            }
            mediaProjection.registerCallback(callback, backgroundHandler)

            val metrics = DisplayMetrics()
            val display: Display = windowManager.defaultDisplay
            display.getRealMetrics(metrics)
            val density = metrics.densityDpi
            val size = Point()
            display.getRealSize(size)

            val imageReader = ImageReader.newInstance(size.x, size.y, PixelFormat.RGBA_8888, 1)
            val surface: Surface = imageReader.surface

            val virtualDisplay = mediaProjection.createVirtualDisplay(
                "screenshot_${System.currentTimeMillis()}",
                size.x, size.y, density,
                0,
                surface,
                null, backgroundHandler
            )

            imageReader.setOnImageAvailableListener({ reader ->
                processImageAsync(reader, virtualDisplay, mediaProjection, callback)
            }, backgroundHandler)

        } catch (e: SecurityException) {
            Log.e("ScreenshotFeature", "SecurityException: ${e.message}")
            showToast("Permission expired, please grant again")
            ScreenshotService.setProjection(null)
            requestScreenshotPermission()
        } catch (e: Exception) {
            Log.e("ScreenshotFeature", "Unexpected error: ${e.message}")
            showToast("Screenshot failed: ${e.message}")
        }
    }

    private fun processImageAsync(
        reader: ImageReader,
        virtualDisplay: android.hardware.display.VirtualDisplay?,
        mediaProjection: MediaProjection,
        callback: MediaProjection.Callback
    ) {
        scope.launch {
            var image: android.media.Image? = null
            try {
                image = reader.acquireLatestImage()
                if (image != null) {
                    val bitmap = convertImageToBitmap(image)

                    // 👉 đóng image ngay sau khi convert
                    image.close()
                    image = null

                    val fileName = generateFileName()
                    val success = saveBitmapToFile(bitmap, fileName)

                    withContext(Dispatchers.Main) {
                        if (success) {
                            showToast("📸 Screenshot saved")
                            Log.d("ScreenshotFeature", "✅ Screenshot saved: $fileName")
                        } else {
                            showToast("❌ Failed to save screenshot")
                            Log.e("ScreenshotFeature", "❌ Failed to save: $fileName")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ScreenshotFeature", "Error processing image: ${e.message}")
                withContext(Dispatchers.Main) {
                    showToast("Screenshot processing failed")
                }
            } finally {
                cleanupResources(reader, virtualDisplay, mediaProjection, callback)
            }
        }
    }

    private suspend fun convertImageToBitmap(image: android.media.Image): Bitmap =
        withContext(Dispatchers.IO) {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val width = image.width
            val height = image.height
            val rowPadding = rowStride - pixelStride * width

            val bitmap = createBitmap(width + rowPadding / pixelStride, height)
            bitmap.copyPixelsFromBuffer(buffer)
            bitmap
        }

    private suspend fun saveBitmapToFile(bitmap: Bitmap, fileName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/Screenshots"
                        )
                        put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                    }
                    val uri =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    uri?.let { resolver.openOutputStream(it) }?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                } else {
                    val screenshotsDir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "Screenshots"
                    )
                    if (!screenshotsDir.exists()) {
                        screenshotsDir.mkdirs()
                    }
                    val file = File(screenshotsDir, fileName)
                    FileOutputStream(file).use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }
                true
            } catch (e: Exception) {
                Log.e("ScreenshotFeature", "Error saving screenshot: ${e.message}")
                false
            }
        }

    private fun generateFileName(): String {
        val timestamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "Screenshot_$timestamp.png"
    }

    private fun cleanupResources(
        reader: ImageReader,
        virtualDisplay: android.hardware.display.VirtualDisplay?,
        mediaProjection: MediaProjection,
        callback: MediaProjection.Callback
    ) {
        try {
            reader.close()
            virtualDisplay?.release()
            mediaProjection.unregisterCallback(callback)
            // 👉 nếu bạn muốn dừng hẳn
            // mediaProjection.stop()
            Log.d("ScreenshotFeature", "🧹 Resources cleaned up")
        } catch (e: Exception) {
            Log.w("ScreenshotFeature", "Cleanup warning: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > TOAST_THROTTLE_MS) {
            lastToastTime = currentTime
            mainHandler.post {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun cleanup() {
        scope.cancel()
        backgroundThread.quitSafely()
    }

    override fun isEnabled(): Boolean {
        return ScreenshotService.mediaProjection != null
    }

    override fun getDefaultIcon(): ImageVector = iconDefault
    override fun getChangedIcon(): ImageVector = iconChanged
}
