package com.example.customui.ui.components.modules.assistant_menu.assistant_menu_service_helper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.feathericons.Wifi
import compose.icons.feathericons.WifiOff

class WifiFeature(private val context: Context) : _interfaceHelper {

    private val wifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override val name = "Wi-Fi"

    private var iconDefault = FeatherIcons.WifiOff
    private var iconChanged = FeatherIcons.Wifi

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE])
    override fun toggle(enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(panelIntent)
        } else {
            wifiManager.isWifiEnabled = enable
        }
    }

    override fun isEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }

   override fun getDefaultIcon(): ImageVector {
        return iconDefault
    }

    override fun getChangedIcon(): ImageVector {
        return iconChanged
    }
}
