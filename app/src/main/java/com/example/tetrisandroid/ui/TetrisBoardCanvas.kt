package com.example.tetrisandroid.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun TetrisBoardCanvas(
    grid: Array<IntArray>,
    activeBlocks: List<Pair<Int, Int>>,
    activeColorArgb: Int,
    modifier: Modifier = Modifier
) {
    val emptyColor = Color(0xFF0D1B2A)
    val gridLineColor = Color(0xFF152238)
    val activeColor = Color(activeColorArgb)
    val lockedColor = Color(0xFF808080)
    val wallColor = Color(0xFF1A2A44)

    Canvas(modifier = modifier.fillMaxSize()) {
        val cellWidth = size.width / 10f
        val visibleRows = 20
        val cellHeight = size.height / visibleRows
        val cellSize = minOf(cellWidth, cellHeight)
        val offsetX = (size.width - cellSize * 10) / 2f
        val offsetY = (size.height - cellSize * visibleRows) / 2f

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
    }
}