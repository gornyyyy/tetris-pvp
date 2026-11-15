package com.example.tetrisandroid.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tetrisandroid.data.AudioSettings

@Composable
fun SettingsDialog(
    isLightTheme: Boolean,
    highlightColumn: Boolean,
    audioSettings: AudioSettings,
    onLightThemeChange: (Boolean) -> Unit,
    onHighlightChange: (Boolean) -> Unit,
    onAudioSettingsChanged: () -> Unit,
    onOpenLayoutEditor: () -> Unit,
    onClose: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White

    var showThemeSelection by remember { mutableStateOf(false) }
    var showAudioSettings by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("НАСТРОЙКИ", color = txt, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Подсветка колонки\n(ах ты казуальщик))", color = txt, style = MaterialTheme.typography.titleMedium)
                    Switch(checked = highlightColumn, onCheckedChange = onHighlightChange)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showThemeSelection = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF2E4A66),
                        contentColor = txt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Выбор темы", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showAudioSettings = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF2E4A66),
                        contentColor = txt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Звуки", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onOpenLayoutEditor,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF2E4A66),
                        contentColor = txt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Редактор раскладки", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

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

    if (showThemeSelection) {
        ThemeSelectionDialog(
            isLightTheme = isLightTheme,
            onLightThemeChange = onLightThemeChange,
            onClose = { showThemeSelection = false }
        )
    }

    if (showAudioSettings) {
        AudioSettingsDialog(
            isLightTheme = isLightTheme,
            audioSettings = audioSettings,
            onVolumeChanged = onAudioSettingsChanged,
            onClose = { showAudioSettings = false }
        )
    }
}