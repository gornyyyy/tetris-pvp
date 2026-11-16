package com.example.tetrisandroid.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.tetrisandroid.ui.theme.*

@Composable
fun PvpRoomScreen(
    isLightTheme: Boolean,
    onCreateRoom: (roomId: String, name: String) -> Unit,
    onJoinRoom: (roomId: String, name: String) -> Unit,
    onBack: () -> Unit
) {
    val bg = if (isLightTheme) LightBackground else DarkBackground
    val btn = if (isLightTheme) LightButton else DarkButton
    val txt = if (isLightTheme) LightText else DarkText
    val muted = if (isLightTheme) LightMuted else DarkMuted

    var name by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

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
        Spacer(modifier = Modifier.height(24.dp))
        Text("PvP", color = txt, style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(24.dp))

        // --- Ник ---
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(16) },
            label = { Text("Ваш ник") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "2–16 символов",
            color = if (nameValid) muted else Color(0xFFE53935),
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Создать комнату ---
        Button(
            onClick = {
                focusManager.clearFocus()
                val code = generateRoomCode()
                generatedCode = code
                if (nameValid) onCreateRoom(code, name.trim())
            },
            enabled = nameValid,
            colors = ButtonDefaults.buttonColors(containerColor = btn, contentColor = txt),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
        ) {
            Text("Создать комнату", style = MaterialTheme.typography.titleMedium)
        }

        generatedCode?.let { code ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ваш код:", color = muted, style = MaterialTheme.typography.labelMedium)
            Text(
                text = code,
                color = txt,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Передайте его другу",
                color = muted,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (nameValid) onJoinRoom(code, name.trim())
                },
                enabled = nameValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
            ) {
                Text("Войти в эту комнату", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Matchmaking ---
        Button(
            onClick = { /* TODO */ },
            enabled = false,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = btn.copy(alpha = 0.3f),
                disabledContentColor = muted.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
        ) {
            Text("Matchmaking (скоро)", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("— или —", color = muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // --- Войти по коду ---
        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(8) },
            label = { Text("Код комнаты") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                if (nameValid && codeInput.length in 4..8) onJoinRoom(codeInput, name.trim())
            },
            enabled = nameValid && codeInput.length in 4..8,
            colors = ButtonDefaults.buttonColors(containerColor = btn, contentColor = txt),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
        ) {
            Text("Войти по коду", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onBack) { Text("Назад", color = muted) }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun generateRoomCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars.random() }.joinToString("")
}