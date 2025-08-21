package com.example.customui.utils

import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

fun LinearGradientBrush(colors: List<Color>): Brush {
    val result = Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset.Infinite
    )
    return result
}

@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
    colors: List<Color>,
    fontSize: TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    fontFamily: FontFamily = FontFamily.SansSerif,
    style: TextStyle = TextStyle.Default
) {
    val gradientBrush = LinearGradientBrush(colors)

    Text(
        text = text,
        modifier = modifier,
        style = style.merge(
            TextStyle(
                brush = gradientBrush,
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontFamily = fontFamily
            )
        )
    )
}
