package com.example.tetrisandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.data.LayoutSettings
import com.example.tetrisandroid.ui.theme.*

@Composable
fun LayoutEditorScreen(
    layoutSettings: LayoutSettings,
    isLightTheme: Boolean,
    onBack: () -> Unit
) {
    var topWeight by remember { mutableStateOf(layoutSettings.topWeight) }
    var bottomWeight by remember { mutableStateOf(layoutSettings.bottomWeight) }
    var fieldWeight by remember { mutableStateOf(layoutSettings.fieldWeight) }
    var panelWeight by remember { mutableStateOf(layoutSettings.panelWeight) }
    var btnWidth by remember { mutableStateOf(layoutSettings.actionButtonWidth) }
    var btnHeight by remember { mutableStateOf(layoutSettings.actionButtonHeight) }
    var hPadding by remember { mutableStateOf(layoutSettings.horizontalPadding) }
    var vPadding by remember { mutableStateOf(layoutSettings.verticalPadding) }

    val bg = if (isLightTheme) LightBackground else DarkBackground
    val txt = if (isLightTheme) LightText else DarkText
    val muted = if (isLightTheme) LightMuted else DarkMuted
    val btnBg = if (isLightTheme) LightButton else DarkButton

    fun applyAndSave() {
        layoutSettings.topWeight = topWeight
        layoutSettings.bottomWeight = bottomWeight
        layoutSettings.fieldWeight = fieldWeight
        layoutSettings.panelWeight = panelWeight
        layoutSettings.actionButtonWidth = btnWidth
        layoutSettings.actionButtonHeight = btnHeight
        layoutSettings.horizontalPadding = hPadding
        layoutSettings.verticalPadding = vPadding
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Раскладка", color = txt, style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onBack) { Text("Назад", color = txt) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Верхняя / нижняя часть ---
        Text("Верхняя часть: ${"%.2f".format(topWeight)}", color = txt)
        Slider(
            value = topWeight,
            onValueChange = {
                topWeight = it
                bottomWeight = 1f - it
                applyAndSave()
            },
            valueRange = 0.5f..0.9f
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Поле / панель ---
        Text("Поле: ${"%.2f".format(fieldWeight)}", color = txt)
        Slider(
            value = fieldWeight,
            onValueChange = {
                fieldWeight = it
                panelWeight = 1f - it
                applyAndSave()
            },
            valueRange = 0.5f..0.85f
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Размер кнопок ---
        Text("Ширина кнопки: ${btnWidth.toInt()} dp", color = txt)
        Slider(
            value = btnWidth,
            onValueChange = { btnWidth = it; applyAndSave() },
            valueRange = 60f..120f
        )

        Text("Высота кнопки: ${btnHeight.toInt()} dp", color = txt)
        Slider(
            value = btnHeight,
            onValueChange = { btnHeight = it; applyAndSave() },
            valueRange = 50f..100f
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Отступы ---
        Text("Горизонтальный отступ: ${hPadding.toInt()} dp", color = txt)
        Slider(
            value = hPadding,
            onValueChange = { hPadding = it; applyAndSave() },
            valueRange = 0f..32f
        )

        Text("Вертикальный отступ: ${vPadding.toInt()} dp", color = txt)
        Slider(
            value = vPadding,
            onValueChange = { vPadding = it; applyAndSave() },
            valueRange = 0f..32f
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                layoutSettings.resetToDefaults()
                topWeight = layoutSettings.topWeight
                bottomWeight = layoutSettings.bottomWeight
                fieldWeight = layoutSettings.fieldWeight
                panelWeight = layoutSettings.panelWeight
                btnWidth = layoutSettings.actionButtonWidth
                btnHeight = layoutSettings.actionButtonHeight
                hPadding = layoutSettings.horizontalPadding
                vPadding = layoutSettings.verticalPadding
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B3A3A),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Вернуть по умолчанию", style = MaterialTheme.typography.titleMedium)
        }
    }
}