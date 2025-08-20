package com.example.customui.ui.components.modules.assistant_menu.modules

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.customui.services.modules.ScreenshotService
import compose.icons.FeatherIcons
import compose.icons.feathericons.Image
import java.io.File
import java.io.FileOutputStream

class ScreenshotFeature(
    private val context: Context
) : _interfaceHelper {

    override val name = "Screenshot"
    private val iconDefault = FeatherIcons.Image
    private val iconChanged = FeatherIcons.Image

    // Khai báo windowManager từ context
    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    override fun toggle(enable: Boolean) {
        var mediaProjection = ScreenshotService.mediaProjection

        if (mediaProjection == null) {
            // Chưa có quyền MediaProjection -> cần xin quyền từ Activity
            Log.e("ScreenshotFeature", "MediaProjection is null. Please request permission first.")
            return
        }

        val metrics = DisplayMetrics()
        val display: Display = windowManager.defaultDisplay
        display.getRealMetrics(metrics)
        val density = metrics.densityDpi
        val size = Point()
        display.getRealSize(size)

        val imageReader = ImageReader.newInstance(size.x, size.y, PixelFormat.RGBA_8888, 2)
        val surface: Surface = imageReader.surface

        val virtualDisplay = mediaProjection?.createVirtualDisplay(
            "screenshot",
            size.x, size.y, density,
            0,
            surface,
            null, null
        )

        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener

            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * size.x

            val bitmap = Bitmap.createBitmap(
                size.x + rowPadding / pixelStride,
                size.y,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            image.close()
            reader.close()
            virtualDisplay?.release()
            mediaProjection?.stop()
            mediaProjection = null

            saveBitmapToFile(bitmap, "screenshot_full.png")
        }, Handler(Looper.getMainLooper()))
    }

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            fileName
        )
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getDefaultIcon(): ImageVector {
        return iconDefault
    }

    override fun getChangedIcon(): ImageVector {
        return iconChanged
    }
}
