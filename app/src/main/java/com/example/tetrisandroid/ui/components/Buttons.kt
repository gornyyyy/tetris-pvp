package com.example.tetrisandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StartButton(buttonColor: Color, textColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(64.dp)
    ) {
        Text("Начать игру", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun SettingsButton(buttonColor: Color, textColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(52.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun ActionButton(
    iconRes: Int,
    enabled: Boolean = true,
    rotationDeg: Float = 0f,
    buttonColor: Color,
    buttonPressed: Color,
    textColor: Color,
    onPress: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var pressJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    val bg = when {
        !enabled -> buttonColor.copy(alpha = 0.4f)
        isPressed -> buttonPressed
        else -> buttonColor
    }
    val iconColor = if (enabled) textColor else textColor.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .width(84.dp)
            .height(72.dp)
            .background(bg, RoundedCornerShape(16.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        isPressed = true
                        onPress()
                        pressJob = scope.launch {
                            delay(200)
                            while (isPressed) {
                                onPress()
                                delay(50)
                            }
                        }
                        waitForUpOrCancellation()
                        isPressed = false
                        pressJob?.cancel()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp).rotate(rotationDeg)
        )
    }
}

@Composable
fun SimpleButton(
    iconRes: Int,
    enabled: Boolean = true,
    rotationDeg: Float = 0f,
    buttonColor: Color,
    buttonPressed: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onPress: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val bg = when {
        !enabled -> buttonColor.copy(alpha = 0.4f)
        isPressed -> buttonPressed
        else -> buttonColor
    }
    val iconColor = if (enabled) textColor else textColor.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .width(84.dp)
            .height(72.dp)
            .background(bg, RoundedCornerShape(16.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        isPressed = true
                        onPress()
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp).rotate(rotationDeg)
        )
    }
}