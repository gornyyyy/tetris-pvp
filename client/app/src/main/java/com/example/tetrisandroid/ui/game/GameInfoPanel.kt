package com.example.tetrisandroid.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.data.ThemeType
import com.example.tetrisandroid.game.GameMode
import com.example.tetrisandroid.ui.theme.DarkAccent
import com.example.tetrisandroid.ui.theme.LightAccent

@Composable
fun GameInfoPanel(
    mode: GameMode,
    state: UiState,
    bestScore: Int,
    bestLevel: Int,
    isLightTheme: Boolean,
    themeType: ThemeType,
    nextVariantIndex: Int,
    isPaused: Boolean,
    buttonColor: Color,
    textColor: Color,
    mutedColor: Color,
    nextSize: androidx.compose.ui.unit.Dp,
    isPvp: Boolean = false,
    opponentScore: Int = 0,
    opponentLevel: Int = 1,
    opponentConnected: Boolean = false,
    pvpStatus: String = "",
    onPause: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 15.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = mode.displayName.uppercase(),
            color = mutedColor.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("SCORE", color = mutedColor, style = MaterialTheme.typography.labelMedium)
        Text(formatScore(state.score), color = textColor, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))
        Text("BEST", color = mutedColor, style = MaterialTheme.typography.labelMedium)
        Text(
            formatScore(bestScore),
            color = if (isLightTheme) LightAccent else DarkAccent,
            style = MaterialTheme.typography.titleLarge
        )
        Text("lvl $bestLevel", color = mutedColor, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(12.dp))
        Text("LEVEL", color = mutedColor, style = MaterialTheme.typography.labelMedium)
        Text("${state.level}", color = textColor, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(20.dp))
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
    }
}