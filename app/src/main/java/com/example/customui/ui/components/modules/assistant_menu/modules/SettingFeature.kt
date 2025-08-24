package com.example.customui.ui.components.modules.assistant_menu.modules

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

//Open setting
class SettingFeature(private val context: Context) : _interfaceHelper {
    private val defaultIcon = Icons.Default.Settings
    private val changedIcon = Icons.Default.Settings

    override val name = "Settings"

    override fun toggle(enable: Boolean) {
        val intent = Intent(Settings.ACTION_SETTINGS)
        context.startActivity(intent)
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getDefaultIcon(): ImageVector {
        return defaultIcon
    }

    override fun getChangedIcon(): ImageVector {
        return changedIcon
    }
}