package com.example.customui.services.assistantmenu

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.IntOffset

class AssistantMenuPosition(offset: IntOffset) {
    private var menuOffsetState = mutableStateOf(IntOffset(0, 0))

    fun getMenuOffset(): IntOffset {
        return menuOffsetState.value
    }

    fun setMenuOffset(offset: IntOffset) {
        menuOffsetState.value = offset

    }
}