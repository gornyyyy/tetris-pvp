package com.example.tetrisandroid.ui.screens

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
import com.example.tetrisandroid.pvp.PvpTransport
import com.example.tetrisandroid.ui.theme.*
import com.example.tetrisandroid.ui.pvp.*

enum class LocalPvpMode { ONLINE, LOCAL_HOST, LOCAL_CLIENT }

@Composable
fun PvpRoomScreen(
    isLightTheme: Boolean,
    onStartGameWithTransport: (transport: PvpTransport, roomId: String?, playerName: String) -> Unit,
    onBack: () -> Unit
) {
    val bg = if (isLightTheme) LightBackground else DarkBackground
    val btn = if (isLightTheme) LightButton else DarkButton
    val txt = if (isLightTheme) LightText else DarkText
    val muted = if (isLightTheme) LightMuted else DarkMuted

    var name by remember { mutableStateOf("") }
    var localMode by remember { mutableStateOf(LocalPvpMode.ONLINE) }
    val nameValid = name.trim().length in 2..16

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text("PvP Режим", color = txt, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(16) },
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

        Row(
            modifier = Modifier.fillMaxWidth(0.9f).background(muted.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
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
                onStartGameWithTransport = onStartGameWithTransport
            )

            LocalPvpMode.LOCAL_HOST -> PvpLocalHostSection(
                btnColor = btn,
                txtColor = txt,
                mutedColor = muted,
                name = name,
                nameValid = nameValid,
                onStartGameWithTransport = onStartGameWithTransport
            )

            LocalPvpMode.LOCAL_CLIENT -> PvpLocalClientSection(
                btnColor = btn,
                txtColor = txt,
                mutedColor = muted,
                name = name,
                nameValid = nameValid,
                onStartGameWithTransport = onStartGameWithTransport
            )
        }

        Spacer(Modifier.weight(1f))
        TextButton(onClick = onBack) { Text("Назад в меню", color = muted) }
        Spacer(Modifier.height(24.dp))
    }
}