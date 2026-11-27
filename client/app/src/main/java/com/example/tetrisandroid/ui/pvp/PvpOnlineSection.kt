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
import com.example.tetrisandroid.BuildConfig
import com.example.tetrisandroid.pvp.PvpTransport
import com.example.tetrisandroid.pvp.WebSocketTransport
import com.example.tetrisandroid.ui.dialogs.ServerUnavailableDialog
import kotlinx.coroutines.launch

@Composable
fun PvpOnlineSection(
    isLightTheme: Boolean,
    btnColor: Color,
    txtColor: Color,
    mutedColor: Color,
    name: String,
    nameValid: Boolean,
    startLevel: Int,
    enableGarbage: Boolean,
    onStartGameWithTransport: (transport: PvpTransport, roomId: String?, playerName: String, startLevel: Int, enableGarbage: Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var codeInput by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var currentTransport by remember { mutableStateOf<PvpTransport?>(null) }
    var isCheckingServer by remember { mutableStateOf(false) }
    var showServerUnavailable by remember { mutableStateOf(false) }

    if (generatedCode == null) {
        Button(
            onClick = {
                focusManager.clearFocus()
                if (nameValid) {
                    isCheckingServer = true
                    scope.launch {
                        val transport = WebSocketTransport(BuildConfig.PVP_SERVER_URL)
                        val isServerUp = transport.ping(3000)
                        if (!isServerUp) {
                            isCheckingServer = false
                            showServerUnavailable = true
                        } else {
                            val code = (1..6).map { "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".random() }.joinToString("")
                            generatedCode = code
                            currentTransport = transport
                            transport.connect()
                            transport.createRoom(code, name.trim(), startLevel, enableGarbage)
                            isCheckingServer = false
                        }
                    }
                }
            },
            enabled = nameValid && !isCheckingServer,
            colors = ButtonDefaults.buttonColors(containerColor = btnColor, contentColor = txtColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.9f).height(56.dp)
        ) {
            if (isCheckingServer) CircularProgressIndicator(color = txtColor, modifier = Modifier.size(24.dp))
            else Text("Создать комнату", style = MaterialTheme.typography.titleMedium)
        }
    } else {
        Spacer(Modifier.height(16.dp))
        Text("Ваш код комнаты:", color = mutedColor, style = MaterialTheme.typography.labelMedium)
        Text(generatedCode!!, color = txtColor, style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Передайте его другу", color = mutedColor, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                currentTransport?.let { transport ->
                    onStartGameWithTransport(transport, generatedCode, name.trim(), startLevel, enableGarbage)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.9f).height(64.dp)
        ) { Text("Войти в эту комнату", style = MaterialTheme.typography.titleMedium) }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = {
            generatedCode = null
            currentTransport?.disconnect()
            currentTransport = null
        }) { Text("Отменить и создать другую", color = mutedColor) }

        Spacer(Modifier.height(24.dp))
        Text("— или —", color = mutedColor, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
    }

    OutlinedTextField(
        value = codeInput,
        onValueChange = { codeInput = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(8) },
        label = { Text("Код комнаты друга") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth(0.9f)
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = {
            focusManager.clearFocus()
            if (nameValid && codeInput.length in 4..8) {
                isCheckingServer = true
                scope.launch {
                    val transport = WebSocketTransport(BuildConfig.PVP_SERVER_URL)
                    val isServerUp = transport.ping(3000)
                    if (!isServerUp) {
                        isCheckingServer = false
                        showServerUnavailable = true
                    } else {
                        transport.connect()
                        currentTransport = transport
                        onStartGameWithTransport(transport, codeInput, name.trim(), startLevel, enableGarbage)
                    }
                }
            }
        },
        enabled = nameValid && codeInput.length in 4..8 && !isCheckingServer,
        colors = ButtonDefaults.buttonColors(containerColor = btnColor, contentColor = txtColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.9f).height(56.dp)
    ) {
        if (isCheckingServer) CircularProgressIndicator(color = txtColor, modifier = Modifier.size(24.dp))
        else Text("Войти по коду", style = MaterialTheme.typography.titleMedium)
    }

    if (showServerUnavailable) {
        ServerUnavailableDialog(
            isLightTheme = isLightTheme,
            serverUrl = BuildConfig.PVP_SERVER_URL,
            onRetry = { showServerUnavailable = false },
            onDismiss = { showServerUnavailable = false }
        )
    }
}