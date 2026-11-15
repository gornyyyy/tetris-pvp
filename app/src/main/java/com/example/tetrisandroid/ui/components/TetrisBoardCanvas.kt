package com.example.tetrisandroid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.data.ThemeType
import kotlin.math.abs

@Composable
fun TetrisBoardCanvas(
    grid: Array<IntArray>,
    activeBlocks: List<Pair<Int, Int>>,
    activeColorArgb: Int,
    highlightColumn: Boolean,
    isLightTheme: Boolean,
    themeType: ThemeType,
    clearingLines: List<Int> = emptyList(),
    clearProgress: Float = 0f,
    modifier: Modifier = Modifier
) {
    val isClassicOrModern = themeType == ThemeType.CLASSIC || themeType == ThemeType.MODERN
    val background = if (isClassicOrModern) Color.Black else if (isLightTheme) Color(0xFFF3F6FB) else Color(0xFF0D1B2A)
    val gridLineColor = if (isLightTheme) Color(0xFFE0E6EF) else Color(0xFF152238)
    val activeColor = if (isLightTheme && !isClassicOrModern) Color.Black else Color(activeColorArgb)
    val lockedColor = if (isLightTheme) Color(0xFF8A8A8A) else Color(0xFF808080)
    val wallColor = if (isLightTheme) Color(0xFFB0B8C4) else Color(0xFF1A2A44)
    val ghostColor = if (isLightTheme) Color(0xFF000000).copy(alpha = 0.2f) else Color(activeColorArgb).copy(alpha = 0.3f)

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

        // 1) Фон поля
        drawRect(color = background, topLeft = Offset(offsetX, offsetY), size = Size(cellSize * 10, cellSize * 20))

        // 2) Рамка для classic/modern
        if (isClassicOrModern) {
            drawRect(
                color = Color.White,
                topLeft = Offset(offsetX - 2.dp.toPx(), offsetY - 2.dp.toPx()),
                size = Size(cellSize * 10 + 4.dp.toPx(), cellSize * 20 + 4.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 3) Сетка и блоки
        for (r in 4 until 24) {
            val isClearingRow = r in clearingLines

            for (c in 0 until 10) {
                val topLeft = Offset(offsetX + c * cellSize, offsetY + (r - 4) * cellSize)

                if (isClearingRow) {
                    val centerCol = 4.5f
                    val distanceFromCenter = abs(c - centerCol) / 5f
                    val localProgress = (clearProgress * 1.5f - distanceFromCenter).coerceIn(0f, 1f)
                    val alpha = 1f - localProgress
                    if (alpha > 0.01f) {
                        drawRect(lockedColor.copy(alpha = alpha), topLeft, Size(cellSize - 2f, cellSize - 2f))
                    }
                    continue
                }

                // Рисуем сетку только для BASIC (не для classic/modern)
                if (!isClassicOrModern) {
                    drawRect(gridLineColor, topLeft, Size(cellSize - 1f, cellSize - 1f))
                }

                val isActive = activeBlocks.any { it.first == c && it.second == r }
                val cellValue = if (isActive) 1 else grid[r][c]

                when (cellValue) {
                    1 -> drawRect(activeColor, topLeft, Size(cellSize - 2f, cellSize - 2f))
                    2 -> drawRect(lockedColor, topLeft, Size(cellSize - 2f, cellSize - 2f))
                    3 -> drawRect(wallColor, topLeft, Size(cellSize - 2f, cellSize - 2f))
                    else -> Unit
                }
            }
        }

        // 4) Призрак
        if (highlightColumn && ghostBlocks.isNotEmpty() && clearingLines.isEmpty()) {
            for ((gx, gy) in ghostBlocks) {
                if (gy < 4) continue
                val topLeft = Offset(offsetX + gx * cellSize, offsetY + (gy - 4) * cellSize)
                drawRect(ghostColor, topLeft, Size(cellSize - 2f, cellSize - 2f))
            }
        }
    }
}