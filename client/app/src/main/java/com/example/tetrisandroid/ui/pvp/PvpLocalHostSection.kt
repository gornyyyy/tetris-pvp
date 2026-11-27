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
import com.example.tetrisandroid.pvp.LocalRoomDiscovery
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
    startLevel: Int,
    enableGarbage: Boolean,
    roomDiscovery: LocalRoomDiscovery,
    onStartGameWithTransport: (transport: PvpTransport, roomId: String?, playerName: String, startLevel: Int, enableGarbage: Boolean) -> Unit
) {
    var roomCreated by remember { mutableStateOf(false) }
    var transport by remember { mutableStateOf<LocalSocketTransport?>(null) }
    val myNetworks = remember { getLocalNetworkInfo() }
    var enteredGame by remember { mutableStateOf(false) }

    Text(
        "Создайте комнату. Клиенты увидят её автоматически в списке ниже.",
        color = mutedColor, textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(16.dp))

    Button(
        onClick = {
            if (nameValid && !roomCreated) {
                val t = LocalSocketTransport(
                    isHost = true,
                    tcpPort = 8988,
                    startLevel = startLevel,
                    playerName = name.trim(),
                    enableGarbage = enableGarbage
                )
                t.connect()
                transport = t
                roomCreated = true

                val bestIp = myNetworks.firstOrNull()?.ipAddress ?: "127.0.0.1"
                roomDiscovery.startHosting("${name.trim()}'s Room", bestIp, 8988)
            }
        },
        enabled = nameValid && !roomCreated,
        colors = ButtonDefaults.buttonColors(
            containerColor = btnColor, contentColor = txtColor,
            disabledContainerColor = btnColor.copy(alpha = 0.3f),
            disabledContentColor = mutedColor.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.9f).height(56.dp)
    ) {
        if (roomCreated) Text("Комната создана", style = MaterialTheme.typography.titleMedium)
        else Text("Создать комнату", style = MaterialTheme.typography.titleMedium)
    }

    Spacer(Modifier.height(12.dp))

    Button(
        onClick = {
            enteredGame = true
            transport?.let { t ->
                onStartGameWithTransport(t, null, name.trim(), startLevel, enableGarbage)
            }
        },
        enabled = roomCreated && transport != null,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.9f).height(64.dp)
    ) {
        Text("Войти в комнату и ждать", style = MaterialTheme.typography.titleMedium)
    }

    Spacer(Modifier.height(24.dp))
    
    if (myNetworks.isNotEmpty()) {
        Text("Ваши IP-адреса (для ручной настройки):", color = mutedColor, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        myNetworks.forEach { net ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(text = net.ipAddress, color = txtColor, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text(text = net.displayName, color = mutedColor, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Порт: 8988", color = mutedColor, style = MaterialTheme.typography.bodySmall)
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!enteredGame) {
                transport?.disconnect()
                roomDiscovery.stop()
            }
        }
    }
}