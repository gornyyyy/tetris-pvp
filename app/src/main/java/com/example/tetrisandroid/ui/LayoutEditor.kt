package com.example.tetrisandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

    val bg = if (isLightTheme) LightBackground else DarkBackground
    val txt = if (isLightTheme) LightText else DarkText
    val muted = if (isLightTheme) LightMuted else DarkMuted
    val btnBg = if (isLightTheme) LightButton else DarkButton
    val fieldBg = if (isLightTheme) Color(0xFFF9FBFF) else Color(0xFF14202E)
    val panelBg = if (isLightTheme) Color(0xFFE8EEF5) else Color(0xFF22324A)
    val borderColor = if (isLightTheme) Color(0xFF9AA8B8) else Color(0xFF3A4F6B)

    fun applyAndSave() {
        layoutSettings.topWeight = topWeight
        layoutSettings.bottomWeight = bottomWeight
        layoutSettings.fieldWeight = fieldWeight
        layoutSettings.panelWeight = panelWeight
        layoutSettings.actionButtonWidth = btnWidth
        layoutSettings.actionButtonHeight = btnHeight
        layoutSettings.horizontalPadding = hPadding
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

        Row(modifier = Modifier.fillMaxWidth()) {
            // === Левая колонка: ползунки ===
            Column(modifier = Modifier.weight(0.6f)) {

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
                Spacer(modifier = Modifier.height(12.dp))

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
                Spacer(modifier = Modifier.height(12.dp))

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
                Spacer(modifier = Modifier.height(12.dp))

                Text("Горизонтальный отступ: ${hPadding.toInt()} dp", color = txt)
                Slider(
                    value = hPadding,
                    onValueChange = { hPadding = it; applyAndSave() },
                    valueRange = 0f..32f
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // === Правая колонка: превью ===
            Column(modifier = Modifier.weight(0.4f)) {
                Text("Превью", color = muted, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.5f)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .background(bg, RoundedCornerShape(8.dp))
                        .padding((hPadding / 2f).dp)
                ) {
                    val previewWidth = maxWidth
                    // Масштаб: сколько dp превью на 1 dp реального экрана.
                    // 360dp — усреднённая ширина телефона.
                    val scaleFactor = previewWidth.value / 360f

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Верхняя часть: поле + панель
                        Row(
                            modifier = Modifier
                                .weight(topWeight)
                                .fillMaxWidth()
                        ) {
                            // Поле
                            Box(
                                modifier = Modifier
                                    .weight(fieldWeight)
                                    .fillMaxHeight()
                                    .background(fieldBg, RoundedCornerShape(4.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                            ) {
                                PreviewFieldGrid()
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Панель
                            Box(
                                modifier = Modifier
                                    .weight(panelWeight)
                                    .fillMaxHeight()
                                    .background(panelBg, RoundedCornerShape(4.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                            ) {
                                // Мини-версия правой панели: SCORE, BEST, LEVEL, NEXT, II
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    // SCORE
                                    PreviewLine(widthFactor = 0.6f, color = muted.copy(alpha = 0.5f))
                                    PreviewLine(widthFactor = 0.9f, color = muted.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // BEST
                                    PreviewLine(widthFactor = 0.5f, color = muted.copy(alpha = 0.5f))
                                    PreviewLine(widthFactor = 0.8f, color = muted.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // LEVEL
                                    PreviewLine(widthFactor = 0.5f, color = muted.copy(alpha = 0.5f))
                                    PreviewLine(widthFactor = 0.4f, color = muted.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // NEXT
                                    PreviewLine(widthFactor = 0.4f, color = muted.copy(alpha = 0.5f))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(fieldBg, RoundedCornerShape(2.dp))
                                            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // Кнопка паузы "II"
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .background(btnBg, RoundedCornerShape(2.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("II", color = txt, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Нижняя часть: кнопки
                        Box(
                            modifier = Modifier
                                .weight(bottomWeight)
                                .fillMaxWidth()
                                .background(btnBg.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        PreviewButton(btnWidth, btnHeight, scaleFactor)
                                        PreviewButton(btnWidth, btnHeight, scaleFactor)
                                    }
                                    PreviewButton(btnWidth, btnHeight, scaleFactor)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    PreviewButton(btnWidth, btnHeight, scaleFactor)
                                    PreviewButton(btnWidth, btnHeight, scaleFactor)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Схематично: поле/панель и кнопки",
                    color = muted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

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

/** Мини-линия для имитации текста в превью. */
@Composable
private fun PreviewLine(widthFactor: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFactor)
            .height(3.dp)
            .background(color, RoundedCornerShape(1.dp))
    )
}

/** Мини-сетка поля (10×20 линий). */
@Composable
private fun PreviewFieldGrid() {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            repeat(9) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(0.3.dp, Color(0xFFB0B8C4).copy(alpha = 0.4f))
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight())
        }
    }
}

/**
 * Мини-кнопка в превью.
 * Размер масштабируется пропорционально размеру превью.
 *
 * @param btnWidth реальная ширина кнопки (dp)
 * @param btnHeight реальная высота кнопки (dp)
 * @param scaleFactor коэффициент масштаба (previewWidth / realScreenWidth)
 */
@Composable
private fun PreviewButton(
    btnWidth: Float,
    btnHeight: Float,
    scaleFactor: Float
) {
    val w = (btnWidth * scaleFactor).dp
    val h = (btnHeight * scaleFactor).dp
    Box(
        modifier = Modifier
            .size(w, h)
            .background(Color(0xFF9AA8B8).copy(alpha = 0.6f), RoundedCornerShape(2.dp))
    )
}