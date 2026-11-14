package com.example.tetrisandroid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NextShapeCanvas(
    nextBlocks: List<Pair<Int, Int>>,
    colorArgb: Int,
    isLightTheme: Boolean,
    canvasSize: Dp = 136.dp   // ширина (высота = 0.55 * ширина)
) {
    val bg = if (isLightTheme) Color(0xFFF9FBFF) else Color(0xFF0D1B2A)
    val borderColor = if (isLightTheme) Color(0xFFD0D8E4) else Color(0xFF2A2A2A)

    // Высота = 55% от ширины (76 dp при ширине 136 dp)
    val canvasHeight = canvasSize * 0.55f

    Canvas(
        modifier = Modifier
            .width(canvasSize)
            .height(canvasHeight)
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

        // Ячейка подстраивается под обе оси (чтобы фигура влезла)
        val cellSize = minOf(
            size.width / 4f,      // 4 клетки по горизонтали максимум
            size.height / 2f      // 2 клетки по вертикали максимум (для I-фигуры повёрнутой — 4, но обычно 2)
        ) * 0.8f

        // Но у I-фигуры бывает 4 клетки по вертикали (вертикальная).
        // Ограничим по 4 клеткам:
        val cellSizeStrict = minOf(size.width / 4f, size.height / 4f) * 0.8f
        // Используем "мягкий" вариант — по 2 клеткам, если фигура не высокая.
        // Для универсальности — min из обоих:
        val cell = if (rows <= 2) {
            minOf(size.width / 4f, size.height / 2f) * 0.8f
        } else {
            minOf(size.width / 4f, size.height / 4f) * 0.8f
        }

        val offsetX = (size.width - cell * cols) / 2f
        val offsetY = (size.height - cell * rows) / 2f

        for ((bx, by) in nextBlocks) {
            drawRect(
                color = Color(colorArgb),
                topLeft = Offset(
                    x = offsetX + (bx - minX) * cell,
                    y = offsetY + (by - minY) * cell
                ),
                size = Size(cell - 3f, cell - 3f)
            )
        }
    }
}