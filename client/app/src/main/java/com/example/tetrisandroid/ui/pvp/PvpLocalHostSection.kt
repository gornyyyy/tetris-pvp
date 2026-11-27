package com.example.tetrisandroid.ui.pvp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.pvp.LocalPvpManager
import com.example.tetrisandroid.pvp.LocalSocketTransport
import com.example.tetrisandroid.pvp.PvpTransport

@Composable
fun PvpLocalHostSection(
    isLightTheme: Boolean,
    btnColor: Color,
    txtColor: Color,
    mutedColor: Color,
    name: String,
    nameValid: Boolean,
    localPvpManager: LocalPvpManager,
    connectionError: String?,
    onStartGameWithTransport: (PvpTransport, String?, String) -> Unit
) {
    var startLevel by remember { mutableStateOf(1) }
    var isConnectingLocal by remember { mutableStateOf(false) }
    var currentTransport by remember { mutableStateOf<PvpTransport?>(null) }

    val context = LocalContext.current
    val withPermissions = rememberWifiDirectPermissions(context, onGranted = {})

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
        "Создайте группу Wi-Fi Direct. Второй игрок найдёт вас в списке устройств.",
        color = mutedColor, textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(24.dp))

    if (currentTransport == null) {
        Button(
            onClick = {
                if (nameValid) {
                    withPermissions {
                        isConnectingLocal = true
                        localPvpManager.createGroup {
                            val transport = LocalSocketTransport(
                                isHost = true,
                                tcpPort = 8988,
                                startLevel = startLevel,
                                playerName = name.trim()
                            )
                            currentTransport = transport
                            isConnectingLocal = false
                            onStartGameWithTransport(transport, null, name.trim())
                        }
                    }
                }
            },
            enabled = nameValid && !isConnectingLocal,
            colors = ButtonDefaults.buttonColors(containerColor = btnColor, contentColor = txtColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.9f).height(56.dp)
        ) {
            if (isConnectingLocal) CircularProgressIndicator(color = txtColor, modifier = Modifier.size(24.dp))
            else Text("Создать комнату", style = MaterialTheme.typography.titleMedium)
        }
    } else {
        Text("Группа создана. Ожидание клиента...", color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodyMedium)
    }

    connectionError?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, color = Color(0xFFE53935), style = MaterialTheme.typography.bodySmall)
    }
}