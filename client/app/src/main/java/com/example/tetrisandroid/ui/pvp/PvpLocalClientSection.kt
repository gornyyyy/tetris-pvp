package com.example.tetrisandroid.ui.pvp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.pvp.LocalPvpManager

@Composable
fun PvpLocalClientSection(
    btnColor: Color,
    txtColor: Color,
    mutedColor: Color,
    nameValid: Boolean,
    localPvpManager: LocalPvpManager,
    peers: List<android.net.wifi.p2p.WifiP2pDevice>,
    connectionError: String?,
    groupOwnerIp: String?,
    isConnectingLocal: Boolean,
    onConnectingStateChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val withPermissions = rememberWifiDirectPermissions(context, onGranted = {})

    Text(
        "Нажмите на устройство хоста в списке ниже, чтобы подключиться.",
        color = mutedColor, textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(16.dp))

    Button(
        onClick = {
            withPermissions { localPvpManager.discoverPeers() }
        },
        enabled = nameValid && !isConnectingLocal,
        colors = ButtonDefaults.buttonColors(containerColor = btnColor, contentColor = txtColor),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.9f).height(56.dp)
    ) { Text("Найти устройства", style = MaterialTheme.typography.titleMedium) }

    Spacer(Modifier.height(16.dp))

    if (peers.isEmpty()) {
        Text(
            "Устройства не найдены. Убедитесь, что хост создал комнату.",
            color = mutedColor, style = MaterialTheme.typography.bodySmall
        )
    } else {
        peers.forEach { device ->
            Card(
                modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = btnColor)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(device.deviceName ?: "Устройство", color = txtColor, style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = {
                            if (nameValid) {
                                withPermissions {
                                    onConnectingStateChange(true)
                                    localPvpManager.connectToDevice(device) {
                                        // LaunchedEffect в PvpRoomScreen обработает groupOwnerIp
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                    ) { Text("Подключиться") }
                }
            }
        }
    }

    if (isConnectingLocal && groupOwnerIp == null) {
        Spacer(Modifier.height(16.dp))
        CircularProgressIndicator(color = txtColor)
        Spacer(Modifier.height(8.dp))
        Text("Подключение и получение IP...", color = mutedColor)
    }

    connectionError?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, color = Color(0xFFE53935), style = MaterialTheme.typography.bodySmall)
    }
}