package com.example.tetrisandroid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NextShapeCanvas(nextBlocks: List<Pair<Int, Int>>, colorArgb: Int, isLightTheme: Boolean) {
    val cellSizeDp = 28.dp
    val fixedSize = cellSizeDp * 4 + 24.dp
    val bg = if (isLightTheme) Color(0xFFF9FBFF) else Color(0xFF0D1B2A)
    val borderColor = if (isLightTheme) Color(0xFFD0D8E4) else Color(0xFF2A2A2A)

    Canvas(
        modifier = Modifier
            .size(fixedSize)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(bg, RoundedCornerShape(8.dp))
    ) {
        if (nextBlocks.isEmpty()) return@Canvas

        val minX = nextBlocks.minOf { it.first }
        val maxX = nextBlocks.maxOf { it.first }
        val minY = nextBlocks.minOf { it.second }
        val maxY = nextBlocks.maxOf { it.second }
        val cols = maxX - minX + 1
        val rows = maxY - minY + 1

        val cellSize = minOf(size.width, size.height) / 4f * 0.8f
        val offsetX = (size.width - cellSize * cols) / 2f
        val offsetY = (size.height - cellSize * rows) / 2f

        for ((bx, by) in nextBlocks) {
            drawRect(
                color = Color(colorArgb),
                topLeft = Offset(
                    x = offsetX + (bx - minX) * cellSize,
                    y = offsetY + (by - minY) * cellSize
                ),
                size = Size(cellSize - 3f, cellSize - 3f)
            )
        }
    }
}