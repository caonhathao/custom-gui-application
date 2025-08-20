package com.example.customui.services.assistantmenu


import com.example.customui.ui.components.modules.assistant_menu.modules._interfaceHelper

class AssistantMenuServiceHelper(private val features: List<_interfaceHelper>) {
    fun toggleFeature(name: String, enable: Boolean) {
        features.find { it.name == name }?.toggle(enable)
    }

    fun listAvailableFeatures(): List<String> {
        return features.map { it.name }
    }

    fun findModuleByName(name: String): _interfaceHelper? {
        return features.find { it.name == name }
    }
}
