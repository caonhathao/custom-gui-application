package com.example.customui.ui.components.modules.assistant_menu.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Home
import compose.icons.feathericons.Image
import compose.icons.feathericons.Square
import compose.icons.feathericons.Wifi
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Lightbulb

class ListFeatures() {
    private val features = mutableListOf<Pair<String, ImageVector>>(
        "Wifi" to FeatherIcons.Wifi,
        "Flash" to FontAwesomeIcons.Solid.Lightbulb,
        "Screenshot" to FeatherIcons.Image,
        "Settings" to Icons.Default.Settings,
        "Home" to FeatherIcons.Home,
        "Back" to FeatherIcons.ArrowLeft,
        "Recents" to FeatherIcons.Square
    )

    fun getFeatures(): List<Pair<String, ImageVector>> {
        return features
    }
}