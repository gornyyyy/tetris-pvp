package com.example.tetrisandroid.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun TetrisBoardCanvas(
    grid: Array<IntArray>,
    activeBlocks: List<Pair<Int, Int>>,
    activeColorArgb: Int,
    highlightColumn: Boolean,
    isLightTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val gridLineColor = if (isLightTheme) Color(0xFFE0E6EF) else Color(0xFF152238)
    val activeColor = if (isLightTheme) Color.Black else Color(activeColorArgb)
    val lockedColor = if (isLightTheme) Color(0xFF8A8A8A) else Color(0xFF808080)
    val wallColor = if (isLightTheme) Color(0xFFB0B8C4) else Color(0xFF1A2A44)
    val edgeLineColor = if (isLightTheme) Color(0xFFB8B8B8) else Color(0xFFA0A0A0)

    Canvas(modifier = modifier.fillMaxSize()) {
        val cellWidth = size.width / 10f
        val visibleRows = 20
        val cellHeight = size.height / visibleRows
        val cellSize = minOf(cellWidth, cellHeight)
        val offsetX = (size.width - cellSize * 10) / 2f
        val offsetY = (size.height - cellSize * visibleRows) / 2f

        // 1) Сетка и блоки
        for (r in 4 until 24) {
            for (c in 0 until 10) {
                val isActive = activeBlocks.any { it.first == c && it.second == r }
                val cellValue = if (isActive) 1 else grid[r][c]

                val topLeft = Offset(
                    x = offsetX + c * cellSize,
                    y = offsetY + (r - 4) * cellSize
                )

                drawRect(
                    color = gridLineColor,
                    topLeft = topLeft,
                    size = Size(cellSize - 1f, cellSize - 1f)
                )

                when (cellValue) {
                    1 -> drawRect(activeColor, topLeft, Size(cellSize - 2f, cellSize - 2f))
                    2 -> drawRect(lockedColor, topLeft, Size(cellSize - 2f, cellSize - 2f))
                    3 -> drawRect(wallColor, topLeft, Size(cellSize - 2f, cellSize - 2f))
                    else -> Unit
                }
            }
        }

        // 2) Линии подсветки - после сетки и блоков
        if (highlightColumn && activeBlocks.isNotEmpty()) {
            val minCol = activeBlocks.minOf { it.first }
            val maxCol = activeBlocks.maxOf { it.first }

            val top = offsetY
            val bottom = offsetY + (visibleRows - 1) * cellSize
            val height = bottom - top

            if (height > 0) {
                val leftX = offsetX + minCol * cellSize
                val rightX = offsetX + (maxCol + 1) * cellSize
                val strokeWidth = 2.dp.toPx()

                drawLine(
                    color = edgeLineColor,
                    start = Offset(leftX, top),
                    end = Offset(leftX, top + height),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = edgeLineColor,
                    start = Offset(rightX, top),
                    end = Offset(rightX, top + height),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}