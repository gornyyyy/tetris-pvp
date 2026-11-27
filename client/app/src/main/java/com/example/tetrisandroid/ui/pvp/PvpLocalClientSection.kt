package com.example.tetrisandroid.ui.pvp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.pvp.LocalRoomDiscovery
import com.example.tetrisandroid.pvp.LocalSocketTransport
import com.example.tetrisandroid.pvp.PvpTransport
import kotlinx.coroutines.launch

@Composable
fun PvpLocalClientSection(
    btnColor: Color,
    txtColor: Color,
    mutedColor: Color,
    name: String,
    nameValid: Boolean,
    roomDiscovery: LocalRoomDiscovery,
    startLevel: Int = 1,
    enableGarbage: Boolean = true,
    onStartGameWithTransport: (transport: PvpTransport, roomId: String?, playerName: String, startLevel: Int, enableGarbage: Boolean) -> Unit
) {
    val discoveredRooms by roomDiscovery.discoveredRooms.collectAsState()
    var isConnecting by remember { mutableStateOf(false) }

    var ipInput by remember { mutableStateOf("") }
    var isPinging by remember { mutableStateOf(false) }
    var showUnavailable by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Text(
        "Подключитесь по IP:",
        color = txtColor,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth(0.9f)
    )
    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = ipInput,
        onValueChange = { ipInput = it.take(15) },
        label = { Text("IP хоста (например, 192.168.1.5)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth(0.9f)
    )
    Spacer(Modifier.height(4.dp))
    Text("Порт: 8988", color = mutedColor, style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(16.dp))

    Button(
        onClick = {
            focusManager.clearFocus()
            if (nameValid && ipInput.isNotBlank()) {
                isPinging = true
                showUnavailable = false
                scope.launch {
                    val ip = ipInput.trim()
                    val transport = LocalSocketTransport(
                        isHost = false,
                        hostIp = ip,
                        tcpPort = 8988,
                        playerName = name.trim()
                    )
                    val isUp = transport.ping(ip, 3000)
                    if (!isUp) {
                        isPinging = false
                        showUnavailable = true
                    } else {
                        transport.connect()
                        isPinging = false
                        onStartGameWithTransport(transport, null, name.trim(), startLevel, enableGarbage)
                    }
                }
            }
        },
        enabled = nameValid && ipInput.isNotBlank() && !isPinging && !isConnecting,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.9f).height(56.dp)
    ) {
        if (isPinging) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text("Войти в игру", style = MaterialTheme.typography.titleMedium)
        }
    }

    if (showUnavailable) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Сервер не отвечает. Проверьте IP и что хост создал комнату.",
            color = Color(0xFFE53935),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { showUnavailable = false }) {
            Text("Повторить", color = mutedColor)
        }
    }

    Spacer(Modifier.height(16.dp))
    Text(
        "Убедитесь, что вы подключены к той же Wi-Fi сети или VPN (ZeroTier/Tailscale), что и хост.",
        color = mutedColor,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(0.9f)
    )

    Spacer(Modifier.height(24.dp))

    Text(
        "Доступные комнаты в вашей сети:",
        color = txtColor,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth(0.9f)
    )
    Spacer(Modifier.height(12.dp))

    if (discoveredRooms.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth(0.9f).height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = mutedColor, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
                Text("Поиск комнат...", color = mutedColor, style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(discoveredRooms) { room ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = btnColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(room.name, color = txtColor, style = MaterialTheme.typography.titleMedium)
                            Text("${room.ip}:${room.port}", color = mutedColor, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = {
                                if (nameValid) {
                                    isConnecting = true
                                    val transport = LocalSocketTransport(
                                        isHost = false,
                                        hostIp = room.ip,
                                        tcpPort = room.port,
                                        playerName = name.trim()
                                    )
                                    transport.connect()
                                    onStartGameWithTransport(transport, null, name.trim(), startLevel, enableGarbage)
                                }
                            },
                            enabled = nameValid && !isConnecting && !isPinging,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                        ) {
                            if (isConnecting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            else Text("Подключиться")
                        }
                    }
                }
            }
        }
    }
}