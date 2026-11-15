package com.example.tetrisandroid.ui.dialogs

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
import androidx.compose.ui.window.Dialog
import com.example.tetrisandroid.data.AudioSettings

@Composable
fun AudioSettingsDialog(
    isLightTheme: Boolean,
    audioSettings: AudioSettings,
    onClose: () -> Unit,
    onVolumeChanged: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White

    var soundEnabled by remember { mutableStateOf(audioSettings.isSoundEnabled) }
    var musicEnabled by remember { mutableStateOf(audioSettings.isMusicEnabled) }
    var masterVol by remember { mutableStateOf(audioSettings.masterVolume) }
    var sfxVol by remember { mutableStateOf(audioSettings.sfxVolume) }
    var musicVol by remember { mutableStateOf(audioSettings.musicVolume) }

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("ЗВУКИ", color = txt, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Все звуки", color = txt)
                    Switch(checked = soundEnabled, onCheckedChange = {
                        soundEnabled = it; audioSettings.isSoundEnabled = it; onVolumeChanged()
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Музыка", color = txt)
                    Switch(checked = musicEnabled, enabled = soundEnabled, onCheckedChange = {
                        musicEnabled = it; audioSettings.isMusicEnabled = it; onVolumeChanged()
                    })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Общая громкость: ${(masterVol * 100).toInt()}%", color = txt)
                Slider(value = masterVol, onValueChange = {
                    masterVol = it; audioSettings.masterVolume = it; onVolumeChanged()
                }, enabled = soundEnabled)

                Text("Громкость звуков (SFX): ${(sfxVol * 100).toInt()}%", color = txt)
                Slider(value = sfxVol, onValueChange = {
                    sfxVol = it; audioSettings.sfxVolume = it; onVolumeChanged()
                }, enabled = soundEnabled)

                Text("Громкость музыки: ${(musicVol * 100).toInt()}%", color = txt)
                Slider(value = musicVol, onValueChange = {
                    musicVol = it; audioSettings.musicVolume = it; onVolumeChanged()
                }, enabled = soundEnabled && musicEnabled)

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                    Text("Закрыть")
                }
            }
        }
    }
}