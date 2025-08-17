package com.example.customui.ui.components.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WrapContentGrid(
    columns: Int,
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    padding: Dp = 0.dp, // ✅ thêm padding cho toàn bộ grid
    content: @Composable () -> Unit
) {
    Layout(content = content) { measurables, constraints ->
        val itemConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        // Đo tất cả item
        val placeables = measurables.map { it.measure(itemConstraints) }

        val itemWidth = placeables.maxOfOrNull { it.width } ?: 0
        val itemHeight = placeables.maxOfOrNull { it.height } ?: 0

        val spacingX = horizontalSpacing.roundToPx()
        val spacingY = verticalSpacing.roundToPx()
        val paddingPx = padding.roundToPx()

        val rowCount = (placeables.size + columns - 1) / columns
        val gridWidth = (itemWidth * columns) + spacingX * (columns - 1) + paddingPx * 2
        val gridHeight = (itemHeight * rowCount) + spacingY * (rowCount - 1) + paddingPx * 2

        layout(gridWidth, gridHeight) {
            placeables.forEachIndexed { index, placeable ->
                val col = index % columns
                val row = index / columns

                val x = paddingPx + col * (itemWidth + spacingX) // ✅ cộng padding
                val y = paddingPx + row * (itemHeight + spacingY) // ✅ cộng padding

                placeable.placeRelative(x, y)
            }
        }
    }
}

