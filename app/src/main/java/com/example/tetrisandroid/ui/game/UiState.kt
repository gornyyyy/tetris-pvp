package com.example.tetrisandroid.ui.game

import androidx.compose.ui.graphics.Color
import java.util.Locale

data class UiState(
    val grid: Array<IntArray>,
    val activeBlocks: List<Pair<Int, Int>>,
    val activeColorArgb: Int,
    val nextBlocks: List<Pair<Int, Int>>,
    val nextColorArgb: Int,
    val score: Int,
    val level: Int,
    val isRunning: Boolean,
    val clearingLines: List<Int> = emptyList(),
    val clearProgress: Float = 0f,
    val linesClearedTotal: Int = 0
)

fun formatScore(score: Int): String {
    val s = score.toLong()
    return when {
        s >= 1_000_000_000L -> "${"%.1f".format(Locale.US, s / 1_000_000_000.0)}B"
        s >= 1_000_000L -> "${"%.1f".format(Locale.US, s / 1_000_000.0)}M"
        s >= 1_000L -> "${"%.1f".format(Locale.US, s / 1_000.0)}K"
        else -> s.toString()
    }
}

fun Color.darken(factor: Float): Color {
    return Color(
        red = red * factor,
        green = green * factor,
        blue = blue * factor,
        alpha = alpha
    )
}