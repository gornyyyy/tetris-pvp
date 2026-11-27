package com.example.tetrisandroid.ui.pvp

import androidx.compose.foundation.layout.*
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
    onStartGameWithTransport: (PvpTransport, String?, String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var ipInput by remember { mutableStateOf("") }
    var isPinging by remember { mutableStateOf(false) }
    var showUnavailable by remember { mutableStateOf(false) }

    Text(
        "Введите IP-адрес хоста (его покажет первый игрок).",
        color = mutedColor, textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = ipInput,
        onValueChange = { ipInput = it.take(15) },
        label = { Text("IP хоста (например, 192.168.1.5)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth(0.9f)
    )
    Spacer(Modifier.height(4.dp))
    Text("Порт: 8988", color = mutedColor, style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(24.dp))

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
                        onStartGameWithTransport(transport, null, name.trim())
                    }
                }
            }
        },
        enabled = nameValid && ipInput.isNotBlank() && !isPinging,
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
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { showUnavailable = false }) {
            Text("Повторить", color = mutedColor)
        }
    }
}