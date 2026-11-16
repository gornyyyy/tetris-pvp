package com.example.tetrisandroid.ui.components

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

    BoxWithConstraints(modifier = modifier) {
        val menuWidth = maxWidth * 0.8f

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(menuWidth)
        ) {
            Button(
                onClick = { if (enabled) expanded = true },
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = textColor
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Режим: ${selectedMode.displayName} ▼", style = MaterialTheme.typography.titleMedium)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(12.dp),
                containerColor = buttonColor,
                modifier = Modifier.width(menuWidth)
            ) {
                GameMode.values().forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = mode.displayName,
                                color = textColor,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onClick = {
                            onModeSelected(mode)
                            expanded = false
                        },
                        enabled = true
                    )
                }
            }
        }
    }
}