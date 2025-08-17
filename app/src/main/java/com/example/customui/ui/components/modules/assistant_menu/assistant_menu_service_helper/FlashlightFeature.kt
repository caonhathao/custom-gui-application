package com.example.customui.ui.components.modules.assistant_menu.assistant_menu_service_helper

import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Lightbulb

class FlashlightFeature(private val context: Context) : _interfaceHelper {
    private val flashlightManager by lazy {
        context.applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private var cameraId: String? = null
    private var isFlashlightOn = false

    init {
        try {
            cameraId = flashlightManager.cameraIdList.firstOrNull() // Thường là camera sau
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override val name = "Flashlight"
    private var iconDefault = FontAwesomeIcons.Solid.Lightbulb
    private var iconChanged = FontAwesomeIcons.Solid.Lightbulb

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun toggle(enable: Boolean) {
        cameraId?.let {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flashlightManager.setTorchMode(it, enable)
                    isFlashlightOn = enable
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    override fun isEnabled(): Boolean {
        return isFlashlightOn
    }

    override fun getDefaultIcon(): ImageVector {
        return iconDefault
    }

    override fun getChangedIcon(): ImageVector {
        return iconChanged
    }
}