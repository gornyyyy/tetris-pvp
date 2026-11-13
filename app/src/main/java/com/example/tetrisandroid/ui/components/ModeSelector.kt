package com.example.tetrisandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.game.GameMode

@Composable
fun ModeSelector(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit,
    enabled: Boolean = true,
    buttonColor: Color,
    textColor: Color,
    mutedColor: Color,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Кнопка — та же ширина, что у StartButton/SettingsButton
        Button(
            onClick = { if (enabled) expanded = true },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = textColor
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(52.dp)
                .align(Alignment.Center)
        ) {
            Text("Режим: ${selectedMode.displayName} ▼", style = MaterialTheme.typography.titleMedium)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(buttonColor, RoundedCornerShape(12.dp))
                .width(240.dp)   // фиксированная ширина для читаемости
        ) {
            GameMode.values().forEach { mode ->
                val isCompetitive = mode == GameMode.COMPETITIVE
                DropdownMenuItem(
                    text = {
                        Text(
                            text = mode.displayName + if (isCompetitive) " (скоро)" else "",
                            color = if (isCompetitive) mutedColor.copy(alpha = 0.5f) else textColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    onClick = {
                        if (!isCompetitive) {
                            onModeSelected(mode)
                            expanded = false
                        }
                    },
                    enabled = !isCompetitive
                )
            }
        }
    }
}