package com.example.tetrisandroid.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.ui.theme.*

@Composable
fun LayoutPreview(
    topWeight: Float,
    bottomWeight: Float,
    fieldWeight: Float,
    panelWeight: Float,
    btnWidth: Float,
    btnHeight: Float,
    hPadding: Float,
    isLightTheme: Boolean,
    txt: Color,
    muted: Color,
    modifier: Modifier = Modifier
) {
    val bg = if (isLightTheme) LightBackground else DarkBackground
    val btnBg = if (isLightTheme) LightButton else DarkButton
    val fieldBg = if (isLightTheme) Color(0xFFF9FBFF) else Color(0xFF14202E)
    val panelBg = if (isLightTheme) Color(0xFFE8EEF5) else Color(0xFF22324A)
    val borderColor = if (isLightTheme) Color(0xFF9AA8B8) else Color(0xFF3A4F6B)

    Column(modifier = modifier) {
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
            val scaleFactor = previewWidth.value / 360f

            Column(modifier = Modifier.fillMaxSize()) {
                // Верхняя часть
                Row(modifier = Modifier.weight(topWeight).fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(fieldWeight)
                            .fillMaxHeight()
                            .background(fieldBg, RoundedCornerShape(4.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                    ) { PreviewFieldGrid() }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .weight(panelWeight)
                            .fillMaxHeight()
                            .background(panelBg, RoundedCornerShape(4.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    ) { PreviewInfoPanel(txt = txt, muted = muted, fieldBg = fieldBg, borderColor = borderColor, btnBg = btnBg) }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Нижняя часть
                Box(
                    modifier = Modifier
                        .weight(bottomWeight)
                        .fillMaxWidth()
                        .background(btnBg.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                ) {
                    PreviewControls(
                        btnWidth = btnWidth,
                        btnHeight = btnHeight,
                        scaleFactor = scaleFactor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
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