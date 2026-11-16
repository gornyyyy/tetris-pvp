package com.example.tetrisandroid.ui.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LayoutSliders(
    topWeight: Float,
    bottomWeight: Float,
    fieldWeight: Float,
    panelWeight: Float,
    btnWidth: Float,
    btnHeight: Float,
    hPadding: Float,
    txt: Color,
    onTopWeightChange: (Float) -> Unit,
    onFieldWeightChange: (Float) -> Unit,
    onBtnWidthChange: (Float) -> Unit,
    onBtnHeightChange: (Float) -> Unit,
    onHPaddingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Верхняя часть: ${"%.2f".format(topWeight)}", color = txt)
        Slider(
            value = topWeight,
            onValueChange = onTopWeightChange,
            valueRange = 0.5f..0.9f
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Поле: ${"%.2f".format(fieldWeight)}", color = txt)
        Slider(
            value = fieldWeight,
            onValueChange = onFieldWeightChange,
            valueRange = 0.5f..0.85f
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Ширина кнопки: ${btnWidth.toInt()} dp", color = txt)
        Slider(
            value = btnWidth,
            onValueChange = onBtnWidthChange,
            valueRange = 60f..120f
        )
        Text("Высота кнопки: ${btnHeight.toInt()} dp", color = txt)
        Slider(
            value = btnHeight,
            onValueChange = onBtnHeightChange,
            valueRange = 50f..100f
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Горизонтальный отступ: ${hPadding.toInt()} dp", color = txt)
        Slider(
            value = hPadding,
            onValueChange = onHPaddingChange,
            valueRange = 0f..32f
        )
    }
}