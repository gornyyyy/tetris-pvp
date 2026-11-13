package com.example.tetrisandroid.ui.components

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
    highlightColumn: Boolean,
    isLightTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val gridLineColor = if (isLightTheme) Color(0xFFE0E6EF) else Color(0xFF152238)
    val activeColor = if (isLightTheme) Color.Black else Color(activeColorArgb)

    // Упавшие блоки
    val lockedColor = if (isLightTheme) Color(0xFF808080) else Color(0xFF808080)

    val wallColor = if (isLightTheme) Color(0xFFB0B8C4) else Color(0xFF1A2A44)

    // Призрак
    val ghostColor = if (isLightTheme)
        Color(0xFF000000).copy(alpha = 0.2f)
    else
        Color(activeColorArgb).copy(alpha = 0.3f)

    // Вычисляем, куда упадёт фигура
    val ghostBlocks: List<Pair<Int, Int>> = if (highlightColumn && activeBlocks.isNotEmpty()) {
        var shift = 0
        while (true) {
            val testBlocks = activeBlocks.map { (x, y) -> x to (y + shift + 1) }
            val canFall = testBlocks.all { (x, y) ->
                y < grid.size && x in 0 until grid[0].size && grid[y][x] == 0
            }
            if (!canFall) break
            shift++
        }
        activeBlocks.map { (x, y) -> x to (y + shift) }
    } else {
        emptyList()
    }

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

        // 2) Призрак фигуры
        if (highlightColumn && ghostBlocks.isNotEmpty()) {
            for ((gx, gy) in ghostBlocks) {
                if (gy < 4) continue
                val topLeft = Offset(
                    x = offsetX + gx * cellSize,
                    y = offsetY + (gy - 4) * cellSize
                )
                drawRect(
                    color = ghostColor,
                    topLeft = topLeft,
                    size = Size(cellSize - 2f, cellSize - 2f)
                )
            }
        }
    }
}