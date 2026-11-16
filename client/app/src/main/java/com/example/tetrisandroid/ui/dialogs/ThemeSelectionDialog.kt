package com.example.tetrisandroid.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tetrisandroid.R
import com.example.tetrisandroid.data.ThemeType
import com.example.tetrisandroid.ui.theme.DarkAccent
import com.example.tetrisandroid.ui.theme.LightAccent
import kotlin.random.Random

@Composable
fun ThemeSelectionDialog(
    isLightTheme: Boolean,
    currentTheme: ThemeType,
    keepFallenColor: Boolean,
    onThemeChange: (ThemeType) -> Unit,
    onLightThemeChange: (Boolean) -> Unit,
    onKeepFallenColorChange: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White
    val accent = if (isLightTheme) LightAccent else DarkAccent

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ВЫБОР ТЕМЫ", color = txt, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Светлая тема", color = txt, style = MaterialTheme.typography.titleMedium)
                    Switch(checked = isLightTheme, onCheckedChange = onLightThemeChange)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeType.values().forEach { theme ->
                        ThemeCard(
                            theme = theme,
                            isSelected = theme == currentTheme,
                            onClick = { onThemeChange(theme) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Оставлять цвет\n после падения",
                        color = txt,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = keepFallenColor,
                        onCheckedChange = onKeepFallenColorChange
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF415A77),
                        contentColor = txt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Закрыть", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: ThemeType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boxColor = if (isSelected) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Transparent

    // Реальные цвета для Basic
    val basicColors = remember {
        listOf(
            Color(0xFF00FFFF), // Cyan
            Color(0xFFFFFF00), // Yellow
            Color(0xFF800080), // Purple
            Color(0xFF00FF00), // Green
            Color(0xFFFF0000), // Red
            Color(0xFF0000FF), // Blue
            Color(0xFFFFA500)  // Orange
        )
    }

    val classicBitmaps = if (theme == ThemeType.CLASSIC) {
        listOf(
            ImageBitmap.imageResource(R.drawable.block_classic_lvl1_1),
            ImageBitmap.imageResource(R.drawable.block_classic_lvl2_2),
            ImageBitmap.imageResource(R.drawable.block_classic_lvl3_3),
            ImageBitmap.imageResource(R.drawable.block_classic_lvl4_1),
            ImageBitmap.imageResource(R.drawable.block_classic_lvl5_2),
            ImageBitmap.imageResource(R.drawable.block_classic_lvl6_3),
            ImageBitmap.imageResource(R.drawable.block_classic_lvl7_1)
        ).shuffled()
    } else emptyList()

    val modernBitmaps = if (theme == ThemeType.MODERN) {
        listOf(
            ImageBitmap.imageResource(R.drawable.block_i_modern_00bfbf),
            ImageBitmap.imageResource(R.drawable.block_o_modern_bfbf00),
            ImageBitmap.imageResource(R.drawable.block_t_modern_800080),
            ImageBitmap.imageResource(R.drawable.block_s_modern_00bf00),
            ImageBitmap.imageResource(R.drawable.block_z_modern_bf0000),
            ImageBitmap.imageResource(R.drawable.block_j_modern_0000bf),
            ImageBitmap.imageResource(R.drawable.block_l_modern_ffa500)
        ).shuffled()
    } else emptyList()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(boxColor, RoundedCornerShape(12.dp))
            .padding(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 0..3) {
                when (theme) {
                    ThemeType.BASIC -> Box(modifier = Modifier.size(14.dp).background(basicColors[i]))
                    ThemeType.CLASSIC -> Image(bitmap = classicBitmaps[i], contentDescription = null, modifier = Modifier.size(14.dp))
                    ThemeType.MODERN -> Image(bitmap = modernBitmaps[i], contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 4..6) {
                when (theme) {
                    ThemeType.BASIC -> Box(modifier = Modifier.size(14.dp).background(basicColors[i]))
                    ThemeType.CLASSIC -> Image(bitmap = classicBitmaps[i], contentDescription = null, modifier = Modifier.size(14.dp))
                    ThemeType.MODERN -> Image(bitmap = modernBitmaps[i], contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(theme.displayName, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) Color(0xFF4CAF50) else Color(0xFF415A77),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(36.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(if (isSelected) "Выбрано" else "Выбрать", style = MaterialTheme.typography.labelSmall)
        }
    }
}