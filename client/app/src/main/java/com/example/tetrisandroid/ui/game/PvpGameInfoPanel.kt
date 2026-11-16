package com.example.tetrisandroid.ui.game

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.data.ThemeType
import com.example.tetrisandroid.ui.game.UiState
import com.example.tetrisandroid.ui.game.formatScore
import com.example.tetrisandroid.ui.components.NextShapeCanvas
import com.example.tetrisandroid.ui.theme.DarkAccent
import com.example.tetrisandroid.ui.theme.LightAccent

@Composable
fun PvpGameInfoPanel(
    state: UiState,
    opponentNickname: String,
    opponentScore: Int,
    opponentConnected: Boolean,
    pvpStatus: String,
    pvpMatchStatus: PvpMatchStatus,
    themeType: ThemeType,
    nextVariantIndex: Int,
    isLightTheme: Boolean,
    isPaused: Boolean,
    buttonColor: Color,
    textColor: Color,
    mutedColor: Color,
    nextSize: Dp,
    onPause: () -> Unit
) {
    val scoreDiff = state.score - opponentScore
    val diffColor = when {
        !opponentConnected -> mutedColor
        scoreDiff > 0 -> Color(0xFF4CAF50)
        scoreDiff < 0 -> Color(0xFFE53935)
        else -> mutedColor
    }
    val diffText = when {
        scoreDiff > 0 -> "+$scoreDiff"
        scoreDiff < 0 -> "-${-scoreDiff}"
        else -> "0"
    }

    val accent = if (isLightTheme) LightAccent else DarkAccent

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 15.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // 1) Режим
        Text(
            text = "COMPETITIVE",
            color = mutedColor.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 2) Ник оппонента
        Text("Соперник", color = mutedColor, style = MaterialTheme.typography.labelMedium)
        Text(
            text = if (opponentConnected) opponentNickname else pvpStatus,
            color = if (opponentConnected) textColor else mutedColor,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3) Обычный SCORE
        Text("SCORE", color = mutedColor, style = MaterialTheme.typography.labelMedium)
        Text(formatScore(state.score), color = textColor, style = MaterialTheme.typography.titleLarge)

        // 4) Разница очков
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = diffText,
            color = diffColor,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = MaterialTheme.typography.titleMedium.fontSize * 0.85f
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5) Текущий уровень
        Text("LEVEL", color = mutedColor, style = MaterialTheme.typography.labelMedium)
        Text("${state.level}", color = textColor, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(20.dp))

        // 6) NEXT
        Text("NEXT", color = mutedColor, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(6.dp))
        NextShapeCanvas(
            nextBlocks = state.nextBlocks,
            colorArgb = state.nextColorArgb,
            themeType = themeType,
            level = state.level,
            variantIndex = nextVariantIndex,
            isLightTheme = isLightTheme,
            canvasSize = nextSize
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 7) Пауза
        if (state.isRunning && !isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(buttonColor, RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onPause() })
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("| |", color = textColor, style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (pvpMatchStatus) {
            PvpMatchStatus.WINNER -> {
                val infiniteTransition = rememberInfiniteTransition(label = "win")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "win_alpha"
                )
                Text(
                    text = "ПОБЕДА!",
                    color = Color(0xFF4CAF50).copy(alpha = alpha),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            PvpMatchStatus.WAITING_OPPONENT -> {
                Text(
                    text = "Ожидаем соперника…",
                    color = mutedColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            PvpMatchStatus.LOSER -> {
                Text(
                    text = "Поражение",
                    color = Color(0xFFE53935),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            PvpMatchStatus.PLAYING -> Unit
        }
    }
}