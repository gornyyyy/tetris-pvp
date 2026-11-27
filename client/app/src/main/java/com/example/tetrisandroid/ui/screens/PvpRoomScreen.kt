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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.pvp.LocalPvpManager
import com.example.tetrisandroid.pvp.LocalSocketTransport
import com.example.tetrisandroid.pvp.PvpTransport
import com.example.tetrisandroid.ui.pvp.*
import com.example.tetrisandroid.ui.theme.*
import kotlinx.coroutines.delay

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
    var isConnectingLocal by remember { mutableStateOf(false) }
    val nameValid = name.trim().length in 2..16

    val context = LocalContext.current
    val localPvpManager = remember { LocalPvpManager(context) }
    val peers by localPvpManager.peers.collectAsState()
    val isGroupOwner by localPvpManager.isGroupOwner.collectAsState()
    val groupOwnerIp by localPvpManager.groupOwnerIp.collectAsState()
    val connectionError by localPvpManager.connectionError.collectAsState()

    LaunchedEffect(Unit) { localPvpManager.init() }
    DisposableEffect(Unit) { onDispose { localPvpManager.release() } }

    LaunchedEffect(localMode) {
        isConnectingLocal = false
    }

    LaunchedEffect(groupOwnerIp, isGroupOwner, localMode) {
        if (localMode == LocalPvpMode.LOCAL_CLIENT && groupOwnerIp != null && !isGroupOwner) {
            delay(500)
            val transport = LocalSocketTransport(
                isHost = false,
                hostIp = groupOwnerIp,
                tcpPort = 8988,
                playerName = name.trim()
            )
            isConnectingLocal = false
            transport.connect()
            onStartGameWithTransport(transport, null, name.trim())
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

        // Переключатель режимов
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
                isLightTheme = isLightTheme,
                btnColor = btn,
                txtColor = txt,
                mutedColor = muted,
                name = name,
                nameValid = nameValid,
                localPvpManager = localPvpManager,
                connectionError = connectionError,
                onStartGameWithTransport = onStartGameWithTransport
            )

            LocalPvpMode.LOCAL_CLIENT -> PvpLocalClientSection(
                btnColor = btn,
                txtColor = txt,
                mutedColor = muted,
                nameValid = nameValid,
                localPvpManager = localPvpManager,
                peers = peers,
                connectionError = connectionError,
                groupOwnerIp = groupOwnerIp,
                isConnectingLocal = isConnectingLocal,
                onConnectingStateChange = { isConnectingLocal = it }
            )
        }

        Spacer(Modifier.weight(1f))
        TextButton(onClick = onBack) { Text("Назад в меню", color = muted) }
        Spacer(Modifier.height(24.dp))
    }
}