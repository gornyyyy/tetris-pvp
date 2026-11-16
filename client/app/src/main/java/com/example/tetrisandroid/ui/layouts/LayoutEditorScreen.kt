package com.example.tetrisandroid.ui.layout

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

    val bg = if (isLightTheme) LightBackground else DarkBackground
    val txt = if (isLightTheme) LightText else DarkText
    val muted = if (isLightTheme) LightMuted else DarkMuted

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
            LayoutSliders(
                topWeight = topWeight,
                bottomWeight = bottomWeight,
                fieldWeight = fieldWeight,
                panelWeight = panelWeight,
                btnWidth = btnWidth,
                btnHeight = btnHeight,
                hPadding = hPadding,
                txt = txt,
                onTopWeightChange = {
                    topWeight = it; bottomWeight = 1f - it; applyAndSave()
                },
                onFieldWeightChange = {
                    fieldWeight = it; panelWeight = 1f - it; applyAndSave()
                },
                onBtnWidthChange = { btnWidth = it; applyAndSave() },
                onBtnHeightChange = { btnHeight = it; applyAndSave() },
                onHPaddingChange = { hPadding = it; applyAndSave() },
                modifier = Modifier.weight(0.6f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            LayoutPreview(
                topWeight = topWeight,
                bottomWeight = bottomWeight,
                fieldWeight = fieldWeight,
                panelWeight = panelWeight,
                btnWidth = btnWidth,
                btnHeight = btnHeight,
                hPadding = hPadding,
                isLightTheme = isLightTheme,
                txt = txt,
                muted = muted,
                modifier = Modifier.weight(0.4f)
            )
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