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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.R
import com.example.tetrisandroid.data.ThemeType

@Composable
fun NextShapeCanvas(
    nextBlocks: List<Pair<Int, Int>>,
    colorArgb: Int,
    isLightTheme: Boolean,
    themeType: ThemeType,
    level: Int,
    variantIndex: Int,
    canvasSize: Dp = 136.dp
) {
    val bg = if (isLightTheme) Color(0xFFF9FBFF) else Color(0xFF0D1B2A)
    val borderColor = if (isLightTheme) Color(0xFFD0D8E4) else Color(0xFF2A2A2A)

    val canvasHeight = canvasSize * 0.55f


    val classicBitmaps = if (themeType == ThemeType.CLASSIC) {
        val paletteLevel = ((level - 1) % 15) + 1

        val suffix = when (variantIndex) {
            0 -> 1
            1 -> 2
            else -> 3
        }

        val resId = when (paletteLevel) {
            1 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl1_1
                2 -> R.drawable.block_classic_lvl1_2
                else -> R.drawable.block_classic_lvl1_3
            }
            2 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl2_1
                2 -> R.drawable.block_classic_lvl2_2
                else -> R.drawable.block_classic_lvl2_3
            }
            3 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl3_1
                2 -> R.drawable.block_classic_lvl3_2
                else -> R.drawable.block_classic_lvl3_3
            }
            4 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl4_1
                2 -> R.drawable.block_classic_lvl4_2
                else -> R.drawable.block_classic_lvl4_3
            }
            5 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl5_1
                2 -> R.drawable.block_classic_lvl5_2
                else -> R.drawable.block_classic_lvl5_3
            }
            6 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl6_1
                2 -> R.drawable.block_classic_lvl6_2
                else -> R.drawable.block_classic_lvl6_3
            }
            7 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl7_1
                2 -> R.drawable.block_classic_lvl7_2
                else -> R.drawable.block_classic_lvl7_3
            }
            8 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl8_1
                2 -> R.drawable.block_classic_lvl8_2
                else -> R.drawable.block_classic_lvl8_3
            }
            9 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl9_1
                2 -> R.drawable.block_classic_lvl9_2
                else -> R.drawable.block_classic_lvl9_3
            }
            10 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl10_1
                2 -> R.drawable.block_classic_lvl10_2
                else -> R.drawable.block_classic_lvl10_3
            }
            11 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl11_1
                2 -> R.drawable.block_classic_lvl11_2
                else -> R.drawable.block_classic_lvl11_3
            }
            12 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl12_1
                2 -> R.drawable.block_classic_lvl12_2
                else -> R.drawable.block_classic_lvl12_3
            }
            13 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl13_1
                2 -> R.drawable.block_classic_lvl13_2
                else -> R.drawable.block_classic_lvl13_3
            }
            14 -> when (suffix) {
                1 -> R.drawable.block_classic_lvl14_1
                2 -> R.drawable.block_classic_lvl14_2
                else -> R.drawable.block_classic_lvl14_3
            }
            else -> when (suffix) {
                1 -> R.drawable.block_classic_lvl15_1
                2 -> R.drawable.block_classic_lvl15_2
                else -> R.drawable.block_classic_lvl15_3
            }
        }

        ImageBitmap.imageResource(resId)
    } else null

    // --- Картинки для Modern ---
    val modernBitmap = if (themeType == ThemeType.MODERN) {
        val resId = when (colorArgb) {
            0xFF00FFFF.toInt() -> R.drawable.block_i_modern_00bfbf
            0xFFFFFF00.toInt() -> R.drawable.block_o_modern_bfbf00
            0xFF800080.toInt() -> R.drawable.block_t_modern_800080
            0xFF00FF00.toInt() -> R.drawable.block_s_modern_00bf00
            0xFFFF0000.toInt() -> R.drawable.block_z_modern_bf0000
            0xFF0000FF.toInt() -> R.drawable.block_j_modern_0000bf
            0xFFFFA500.toInt() -> R.drawable.block_l_modern_ffa500
            else -> R.drawable.block_i_modern_00bfbf
        }
        ImageBitmap.imageResource(resId)
    } else null

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

        val cell = if (rows <= 2) {
            minOf(size.width / 4f, size.height / 2f) * 0.8f
        } else {
            minOf(size.width / 4f, size.height / 4f) * 0.8f
        }

        val offsetX = (size.width - cell * cols) / 2f
        val offsetY = (size.height - cell * rows) / 2f

        for ((bx, by) in nextBlocks) {
            val topLeft = Offset(
                x = offsetX + (bx - minX) * cell,
                y = offsetY + (by - minY) * cell
            )
            if (themeType == ThemeType.CLASSIC && classicBitmaps != null) {
                drawImage(
                    image = classicBitmaps,
                    dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                    dstSize = IntSize(cell.toInt(), cell.toInt())
                )
            } else if (themeType == ThemeType.MODERN && modernBitmap != null) {
                drawImage(
                    image = modernBitmap,
                    dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                    dstSize = IntSize(cell.toInt(), cell.toInt())
                )
            } else {
                drawRect(
                    color = Color(colorArgb),
                    topLeft = topLeft,
                    size = Size(cell - 3f, cell - 3f)
                )
            }
        }
    }
}