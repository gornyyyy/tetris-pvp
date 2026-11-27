package com.example.tetrisandroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.tetrisandroid.data.GameSettings
import com.example.tetrisandroid.audio.GameAudioManager
import com.example.tetrisandroid.pvp.LocalRoomDiscovery
import com.example.tetrisandroid.pvp.PvpTransport
import com.example.tetrisandroid.ui.theme.*
import com.example.tetrisandroid.ui.pvp.*

enum class LocalPvpMode { ONLINE, LOCAL_HOST, LOCAL_CLIENT }

@Composable
fun PvpRoomScreen(
    gameSettings: GameSettings,
    isLightTheme: Boolean,
    audioManager: GameAudioManager,
    onStartGameWithTransport: (transport: PvpTransport, roomId: String?, playerName: String, startLevel: Int, enableGarbage: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val bg = if (isLightTheme) LightBackground else DarkBackground
    val btn = if (isLightTheme) LightButton else DarkButton
    val txt = if (isLightTheme) LightText else DarkText
    val muted = if (isLightTheme) LightMuted else DarkMuted

    var name by remember { mutableStateOf(gameSettings.username) }
    var localMode by remember { mutableStateOf(LocalPvpMode.ONLINE) }
    val nameValid = name.trim().length in 2..16

    var startLevel by remember { mutableStateOf(1) }
    var enableGarbage by remember { mutableStateOf(true) }

    val roomDiscovery = remember { LocalRoomDiscovery() }

    LaunchedEffect(localMode) {
        if (localMode == LocalPvpMode.LOCAL_CLIENT) {
            roomDiscovery.startListening()
        } else {
            roomDiscovery.stop()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        audioManager.stopDialogSound()
        audioManager.stopMusic()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        audioManager.playRandomMenuMusic()
    }

    DisposableEffect(Unit) {
        onDispose {
            roomDiscovery.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = txt
                )
            }
        }

        Text("PvP Режим", color = txt, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it.take(16)
                gameSettings.username = name
            },
            label = { Text("Ваш ник") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "2–16 символов",
            color = if (nameValid) muted else Color(0xFFE53935),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            colors = CardDefaults.cardColors(containerColor = muted.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Настройки матча", color = txt, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Стартовый уровень:", fontSize = 12.sp, color = txt)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (startLevel > 1) startLevel-- }) {
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(2.dp)
                                    .background(txt, RoundedCornerShape(1.dp))
                            )
                        }

                        Text(
                            text = "$startLevel",
                            color = txt,
                            fontSize = 14.sp,
                            modifier = Modifier.width(24.dp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(onClick = { if (startLevel < 15) startLevel++ }) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height(2.dp)
                                        .background(txt, RoundedCornerShape(1.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(12.dp)
                                        .background(txt, RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }

                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = muted.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Мусорные линии", color = txt, style = MaterialTheme.typography.bodyMedium)
                        Text(if (enableGarbage) "Включены" else "Выключены", color = muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = enableGarbage,
                        onCheckedChange = { enableGarbage = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(muted.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(LocalPvpMode.ONLINE, LocalPvpMode.LOCAL_HOST, LocalPvpMode.LOCAL_CLIENT).forEach { mode ->
                val isSelected = localMode == mode
                TextButton(
                    onClick = { localMode = mode },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (mode) {
                            LocalPvpMode.ONLINE -> "Онлайн"
                            LocalPvpMode.LOCAL_HOST -> "Хост"
                            LocalPvpMode.LOCAL_CLIENT -> "Клиент"
                        },
                        color = if (isSelected) txt else muted,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        when (localMode) {
            LocalPvpMode.ONLINE -> PvpOnlineSection(
                isLightTheme = isLightTheme,
                btnColor = btn,
                txtColor = txt,
                mutedColor = muted,
                name = name,
                nameValid = nameValid,
                startLevel = startLevel,
                enableGarbage = enableGarbage,
                onStartGameWithTransport = onStartGameWithTransport
            )

            LocalPvpMode.LOCAL_HOST -> PvpLocalHostSection(
                btnColor = btn,
                txtColor = txt,
                mutedColor = muted,
                name = name,
                nameValid = nameValid,
                startLevel = startLevel,
                enableGarbage = enableGarbage,
                roomDiscovery = roomDiscovery,
                onStartGameWithTransport = onStartGameWithTransport
            )

            LocalPvpMode.LOCAL_CLIENT -> PvpLocalClientSection(
                btnColor = btn,
                txtColor = txt,
                mutedColor = muted,
                name = name,
                nameValid = nameValid,
                roomDiscovery = roomDiscovery,
                onStartGameWithTransport = onStartGameWithTransport
            )
        }

        Spacer(Modifier.height(22.dp))
        TextButton(onClick = onBack) { Text("Назад в меню", color = muted) }
        Spacer(Modifier.height(24.dp))
    }
}