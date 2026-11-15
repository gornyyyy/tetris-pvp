package com.example.tetrisandroid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.R
import com.example.tetrisandroid.data.ThemeType
import kotlin.math.abs

@Composable
fun TetrisBoardCanvas(
    grid: Array<IntArray>,
    variantGrid: Array<IntArray>,
    activeBlocks: List<Pair<Int, Int>>,
    activeColorArgb: Int,
    activeVariantIndex: Int,
    highlightColumn: Boolean,
    isLightTheme: Boolean,
    themeType: ThemeType,
    level: Int,
    clearingLines: List<Int> = emptyList(),
    clearProgress: Float = 0f,
    modifier: Modifier = Modifier
) {
    val isClassicOrModern = themeType == ThemeType.CLASSIC || themeType == ThemeType.MODERN
    val background = if (isClassicOrModern) Color(0xFF1A1A1A) else if (isLightTheme) Color(0xFFF3F6FB) else Color(0xFF0D1B2A)
    val gridLineColor = if (isLightTheme) Color(0xFFE0E6EF) else Color(0xFF152238)
    val activeColor = if (isLightTheme && !isClassicOrModern) Color.Black else Color(activeColorArgb)
    val lockedColor = if (isLightTheme) Color(0xFF8A8A8A) else Color(0xFF808080)
    val wallColor = if (isLightTheme) Color(0xFFB0B8C4) else Color(0xFF1A2A44)

    val ghostAlpha = 0.3f

    val wallClassicBitmap = if (themeType == ThemeType.CLASSIC) {
        ImageBitmap.imageResource(R.drawable.block_wall_classic)
    } else null

    val wallModernBitmap = if (themeType == ThemeType.MODERN) {
        ImageBitmap.imageResource(R.drawable.block_wall_modern)
    } else null

    // Classic: картинки по уровню
    val classicBitmaps = if (themeType == ThemeType.CLASSIC) {
        val paletteLevel = ((level - 1) % 15) + 1
        listOf(
            ImageBitmap.imageResource(when(paletteLevel) {
                1 -> R.drawable.block_classic_lvl1_1
                2 -> R.drawable.block_classic_lvl2_1
                3 -> R.drawable.block_classic_lvl3_1
                4 -> R.drawable.block_classic_lvl4_1
                5 -> R.drawable.block_classic_lvl5_1
                6 -> R.drawable.block_classic_lvl6_1
                7 -> R.drawable.block_classic_lvl7_1
                8 -> R.drawable.block_classic_lvl8_1
                9 -> R.drawable.block_classic_lvl9_1
                10 -> R.drawable.block_classic_lvl10_1
                11 -> R.drawable.block_classic_lvl11_1
                12 -> R.drawable.block_classic_lvl12_1
                13 -> R.drawable.block_classic_lvl13_1
                14 -> R.drawable.block_classic_lvl14_1
                else -> R.drawable.block_classic_lvl15_1
            }),
            ImageBitmap.imageResource(when(paletteLevel) {
                1 -> R.drawable.block_classic_lvl1_2
                2 -> R.drawable.block_classic_lvl2_2
                3 -> R.drawable.block_classic_lvl3_2
                4 -> R.drawable.block_classic_lvl4_2
                5 -> R.drawable.block_classic_lvl5_2
                6 -> R.drawable.block_classic_lvl6_2
                7 -> R.drawable.block_classic_lvl7_2
                8 -> R.drawable.block_classic_lvl8_2
                9 -> R.drawable.block_classic_lvl9_2
                10 -> R.drawable.block_classic_lvl10_2
                11 -> R.drawable.block_classic_lvl11_2
                12 -> R.drawable.block_classic_lvl12_2
                13 -> R.drawable.block_classic_lvl13_2
                14 -> R.drawable.block_classic_lvl14_2
                else -> R.drawable.block_classic_lvl15_2
            }),
            ImageBitmap.imageResource(when(paletteLevel) {
                1 -> R.drawable.block_classic_lvl1_3
                2 -> R.drawable.block_classic_lvl2_3
                3 -> R.drawable.block_classic_lvl3_3
                4 -> R.drawable.block_classic_lvl4_3
                5 -> R.drawable.block_classic_lvl5_3
                6 -> R.drawable.block_classic_lvl6_3
                7 -> R.drawable.block_classic_lvl7_3
                8 -> R.drawable.block_classic_lvl8_3
                9 -> R.drawable.block_classic_lvl9_3
                10 -> R.drawable.block_classic_lvl10_3
                11 -> R.drawable.block_classic_lvl11_3
                12 -> R.drawable.block_classic_lvl12_3
                13 -> R.drawable.block_classic_lvl13_3
                14 -> R.drawable.block_classic_lvl14_3
                else -> R.drawable.block_classic_lvl15_3
            })
        )
    } else null

    // Modern: картинки по цвету
    val modernBitmaps = if (themeType == ThemeType.MODERN) {
        mapOf(
            0xFF00FFFF.toInt() to ImageBitmap.imageResource(R.drawable.block_i_modern_00bfbf),
            0xFFFFFF00.toInt() to ImageBitmap.imageResource(R.drawable.block_o_modern_bfbf00),
            0xFF800080.toInt() to ImageBitmap.imageResource(R.drawable.block_t_modern_800080),
            0xFF00FF00.toInt() to ImageBitmap.imageResource(R.drawable.block_s_modern_00bf00),
            0xFFFF0000.toInt() to ImageBitmap.imageResource(R.drawable.block_z_modern_bf0000),
            0xFF0000FF.toInt() to ImageBitmap.imageResource(R.drawable.block_j_modern_0000bf),
            0xFFFFA500.toInt() to ImageBitmap.imageResource(R.drawable.block_l_modern_ffa500)
        )
    } else null

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

                if (!isClassicOrModern) {
                    drawRect(gridLineColor, topLeft, Size(cellSize - 1f, cellSize - 1f))
                }

                val isActive = activeBlocks.any { it.first == c && it.second == r }
                val cellValue = if (isActive) 1 else grid[r][c]

                when (cellValue) {
                    1 -> {
                        // Активная фигура
                        if (themeType == ThemeType.CLASSIC && classicBitmaps != null) {
                            drawImage(
                                image = classicBitmaps[activeVariantIndex.coerceIn(0, 2)],
                                dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                                dstSize = IntSize(cellSize.toInt(), cellSize.toInt())
                            )
                        } else if (themeType == ThemeType.MODERN && modernBitmaps != null) {
                            val bmp = modernBitmaps[activeColorArgb] ?: modernBitmaps[0xFF00FFFF.toInt()]!!
                            drawImage(
                                image = bmp,
                                dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                                dstSize = IntSize(cellSize.toInt(), cellSize.toInt())
                            )
                        } else {
                            drawRect(activeColor, topLeft, Size(cellSize - 2f, cellSize - 2f))
                        }
                    }
                    2 -> {
                        // Упавшая фигура
                        val variant = variantGrid[r][c]
                        if (themeType == ThemeType.CLASSIC && classicBitmaps != null) {
                            drawImage(
                                image = classicBitmaps[variant.coerceIn(0, 2)],
                                dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                                dstSize = IntSize(cellSize.toInt(), cellSize.toInt())
                            )
                        } else if (themeType == ThemeType.MODERN && modernBitmaps != null) {
                            val bmp = modernBitmaps[variant] ?: modernBitmaps[0xFF00FFFF.toInt()]!!
                            drawImage(
                                image = bmp,
                                dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                                dstSize = IntSize(cellSize.toInt(), cellSize.toInt())
                            )
                        } else {
                            drawRect(lockedColor, topLeft, Size(cellSize - 2f, cellSize - 2f))
                        }
                    }
                    3 -> {
                        if (themeType == ThemeType.CLASSIC && wallClassicBitmap != null) {
                            drawImage(
                                image = wallClassicBitmap,
                                dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                                dstSize = IntSize(cellSize.toInt(), cellSize.toInt())
                            )
                        } else if (themeType == ThemeType.MODERN && wallModernBitmap != null) {
                            drawImage(
                                image = wallModernBitmap,
                                dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                                dstSize = IntSize(cellSize.toInt(), cellSize.toInt())
                            )
                        } else {
                            drawRect(wallColor, topLeft, Size(cellSize - 2f, cellSize - 2f))
                        }
                    }
                    else -> Unit
                }
            }
        }

        if (highlightColumn && ghostBlocks.isNotEmpty() && clearingLines.isEmpty()) {
            for ((gx, gy) in ghostBlocks) {
                if (gy < 4) continue
                val topLeft = Offset(offsetX + gx * cellSize, offsetY + (gy - 4) * cellSize)

                // Рисуем призрак с учётом скина
                if (themeType == ThemeType.CLASSIC && classicBitmaps != null) {
                    drawImage(
                        image = classicBitmaps[activeVariantIndex.coerceIn(0, 2)],
                        dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                        dstSize = IntSize(cellSize.toInt(), cellSize.toInt()),
                        alpha = ghostAlpha
                    )
                } else if (themeType == ThemeType.MODERN && modernBitmaps != null) {
                    val bmp = modernBitmaps[activeColorArgb] ?: modernBitmaps[0xFF00FFFF.toInt()]!!
                    drawImage(
                        image = bmp,
                        dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                        dstSize = IntSize(cellSize.toInt(), cellSize.toInt()),
                        alpha = ghostAlpha
                    )
                } else {
                    drawRect(
                        color = activeColor.copy(alpha = ghostAlpha),
                        topLeft = topLeft,
                        size = Size(cellSize - 2f, cellSize - 2f)
                    )
                }
            }
        }
    }
}