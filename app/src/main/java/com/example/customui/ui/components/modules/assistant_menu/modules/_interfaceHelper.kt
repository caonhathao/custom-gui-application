package com.example.customui.ui.components.modules.assistant_menu.modules

import androidx.compose.ui.graphics.vector.ImageVector

interface _interfaceHelper {
    val name: String
    fun toggle(enable: Boolean)
    fun isEnabled(): Boolean

    fun getDefaultIcon(): ImageVector

    fun getChangedIcon(): ImageVector
}