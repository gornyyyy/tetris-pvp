package com.example.tetrisandroid.ui.pvp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.pvp.LocalSocketTransport
import com.example.tetrisandroid.pvp.PvpTransport
import com.example.tetrisandroid.pvp.getLocalNetworkInfo

@Composable
fun PvpLocalHostSection(
    btnColor: Color,
    txtColor: Color,
    mutedColor: Color,
    name: String,
    nameValid: Boolean,
    onStartGameWithTransport: (PvpTransport, String?, String) -> Unit
) {
    var startLevel by remember { mutableStateOf(1) }
    var roomCreated by remember { mutableStateOf(false) }
    var transport by remember { mutableStateOf<LocalSocketTransport?>(null) }

    val myNetworks = remember { getLocalNetworkInfo() }

    var enteredGame by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(0.9f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Стартовый уровень:", color = txtColor, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (startLevel > 1) startLevel-- }) {
                Text("−", color = txtColor, style = MaterialTheme.typography.titleLarge)
            }
            Text(
                "$startLevel", color = txtColor,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(24.dp), textAlign = TextAlign.Center
            )
            IconButton(onClick = { if (startLevel < 15) startLevel++ }) {
                Text("+", color = txtColor, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
    Spacer(Modifier.height(16.dp))

    Text(
        "Создайте комнату и сообщите свой IP-адрес второму игроку.\n" +
                "Это может быть IP локальной Wi-Fi сети или VPN (ZeroTier, WireGuard и т.д.).",
        color = mutedColor, textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(16.dp))

    if (myNetworks.isNotEmpty()) {
        Text("Ваши доступные IP-Addresses:", color = mutedColor, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))

        myNetworks.forEach { net ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = net.ipAddress,
                    color = txtColor,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = net.displayName,
                    color = mutedColor,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Порт: 8988", color = mutedColor, style = MaterialTheme.typography.bodySmall)
    } else {
        Text(
            "Не удалось определить IP. Проверьте подключение к Wi-Fi или VPN.",
            color = Color(0xFFE53935),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = {
            if (nameValid && !roomCreated) {
                val t = LocalSocketTransport(
                    isHost = true,
                    tcpPort = 8988,
                    startLevel = startLevel,
                    playerName = name.trim()
                )
                t.connect()
                transport = t
                roomCreated = true
            }
        },
        enabled = nameValid && !roomCreated,
        colors = ButtonDefaults.buttonColors(
            containerColor = btnColor,
            contentColor = txtColor,
            disabledContainerColor = btnColor.copy(alpha = 0.3f),
            disabledContentColor = mutedColor.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.9f).height(56.dp)
    ) {
        Text(
            if (roomCreated) "Комната создана" else "Создать комнату",
            style = MaterialTheme.typography.titleMedium
        )
    }

    Spacer(Modifier.height(12.dp))

    Button(
        onClick = {
            enteredGame = true
            transport?.let { t ->
                onStartGameWithTransport(t, null, name.trim())
            }
        },
        enabled = roomCreated && transport != null,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.3f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.9f).height(64.dp)
    ) {
        Text("Войти в комнату", style = MaterialTheme.typography.titleMedium)
    }

    if (roomCreated) {
        Spacer(Modifier.height(12.dp))
        Text(
            "Комната создана. Сообщите IP второму игроку и нажмите «Войти в комнату».",
            color = mutedColor, textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
    } else {
        Spacer(Modifier.height(12.dp))
        Text(
            "Сначала создайте комнату — после этого появится кнопка входа.",
            color = mutedColor, textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!enteredGame) transport?.disconnect()
        }
    }
}