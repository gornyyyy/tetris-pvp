package com.example.tetrisandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.example.tetrisandroid.R
import com.example.tetrisandroid.data.GameSettings
import com.example.tetrisandroid.data.GameStatsStorage
import com.example.tetrisandroid.game.*
import com.example.tetrisandroid.model.PlayerAction
import com.example.tetrisandroid.ui.components.*
import com.example.tetrisandroid.ui.dialogs.*
import com.example.tetrisandroid.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

data class UiState(
    val grid: Array<IntArray>,
    val activeBlocks: List<Pair<Int, Int>>,
    val activeColorArgb: Int,
    val nextBlocks: List<Pair<Int, Int>>,
    val nextColorArgb: Int,
    val score: Int,
    val level: Int,
    val isRunning: Boolean,
    val clearingLines: List<Int> = emptyList(),
    val clearProgress: Float = 0f
)

fun formatScore(score: Int): String {
    val s = score.toLong()
    return when {
        s >= 1_000_000_000L -> "${"%.1f".format(Locale.US, s / 1_000_000_000.0)}B"
        s >= 1_000_000L -> "${"%.1f".format(Locale.US, s / 1_000_000.0)}M"
        s >= 1_000L -> "${"%.1f".format(Locale.US, s / 1_000.0)}K"
        else -> s.toString()
    }
}

@Composable
fun TetrisScreen(
    mode: GameMode,
    statsStorage: GameStatsStorage,
    gameSettings: GameSettings,
    onExitToMenu: () -> Unit
) {
    var gameId by remember { mutableStateOf(0) }
    val game = remember(gameId) {
        when (mode) {
            GameMode.CLASSIC -> GameClassic({}, {})
            GameMode.FAST -> GameFast({}, {})
            GameMode.FINISH -> GameWithFinish({}, {})
            GameMode.COMPETITIVE -> GameCompetitive({}, {})
        }
    }

    var isPaused by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }

    var isLightTheme by remember { mutableStateOf(gameSettings.isLightTheme) }
    var highlightColumn by remember { mutableStateOf(gameSettings.highlightColumn) }

    var bestScore by remember { mutableStateOf(statsStorage.getBestScore(mode.id)) }
    var bestLevel by remember { mutableStateOf(statsStorage.getBestLevel(mode.id)) }

    var statsSaved by remember(gameId) { mutableStateOf(false) }

    val background = if (isLightTheme) LightBackground else DarkBackground
    val buttonColor = if (isLightTheme) LightButton else DarkButton
    val buttonPressed = if (isLightTheme) LightButtonPressed else DarkButtonPressed
    val textColor = if (isLightTheme) LightText else DarkText
    val mutedColor = if (isLightTheme) LightMuted else DarkMuted

    var uiState by remember {
        mutableStateOf(
            UiState(
                grid = Array(24) { IntArray(10) },
                activeBlocks = emptyList(),
                activeColorArgb = 0xFF00FFFF.toInt(),
                nextBlocks = emptyList(),
                nextColorArgb = 0xFF00FFFF.toInt(),
                score = 0, level = 1, isRunning = true
            )
        )
    }

    fun refreshUi() {
        uiState = UiState(
            grid = Array(game.grid.size) { r -> game.grid[r].clone() },
            activeBlocks = game.currentShape.getBlocks().map { it.x to it.y },
            activeColorArgb = game.currentShape.colorArgb,
            nextBlocks = game.nextShape.getBlocks().map { it.x to it.y },
            nextColorArgb = game.nextShape.colorArgb,
            score = game.score,
            level = game.level,
            isRunning = game.isRunning,
            clearingLines = game.clearingLines,
            clearProgress = game.clearProgress
        )
        if (!game.isRunning && !statsSaved) {
            statsSaved = true
            val won = (game as? GameWithFinish)?.isWon == true
            statsStorage.saveResult(
                modeId = mode.id,
                score = game.score,
                level = game.level,
                lines = game.linesClearedTotal,
                won = won
            )
            bestScore = statsStorage.getBestScore(mode.id)
            bestLevel = statsStorage.getBestLevel(mode.id)
            if (won) showWinDialog = true
        }
    }

    LaunchedEffect(gameId, isPaused) {
        if (isPaused) return@LaunchedEffect
        refreshUi()
        while (game.isRunning) {
            delay(16)
            game.tick()
            refreshUi()
        }
        refreshUi()
    }

    val state = uiState

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        if (!isPaused && state.isRunning) isPaused = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.weight(0.75f).fillMaxWidth()) {
            Box(
                modifier = Modifier.weight(0.7f).fillMaxHeight()
            ) {
                TetrisBoardCanvas(
                    grid = state.grid,
                    activeBlocks = if (state.clearingLines.isEmpty()) state.activeBlocks else emptyList(),
                    activeColorArgb = state.activeColorArgb,
                    highlightColumn = highlightColumn,
                    isLightTheme = isLightTheme,
                    clearingLines = state.clearingLines,
                    clearProgress = state.clearProgress
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(0.3f).fillMaxHeight(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatScore(bestScore), color = if (isLightTheme) LightAccent else DarkAccent, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("lvl $bestLevel", color = mutedColor, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("LEVEL", color = mutedColor, style = MaterialTheme.typography.labelMedium)
                Text("${state.level}", color = textColor, style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(20.dp))
                Text("NEXT", color = mutedColor, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                NextShapeCanvas(state.nextBlocks, state.nextColorArgb, isLightTheme)

                if (state.isRunning && !isPaused) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { isPaused = true },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = textColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("II", style = MaterialTheme.typography.titleMedium) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(0.25f).fillMaxWidth()) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    ActionButton(R.drawable.ic_right_arrow, !isPaused, 180f, buttonColor, buttonPressed, textColor) {
                        game.handleInput(PlayerAction.MOVE_LEFT); refreshUi()
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ActionButton(R.drawable.ic_right_arrow, !isPaused, 0f, buttonColor, buttonPressed, textColor) {
                        game.handleInput(PlayerAction.MOVE_RIGHT); refreshUi()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(R.drawable.ic_right_arrow, !isPaused, 90f, buttonColor, buttonPressed, textColor) {
                    game.handleInput(PlayerAction.MOVE_DOWN); refreshUi()
                }
            }
            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SimpleButton(R.drawable.rotate_arrow, !isPaused, 0f, buttonColor, buttonPressed, textColor) {
                    game.handleInput(PlayerAction.ROTATE); refreshUi()
                }
                Spacer(modifier = Modifier.height(8.dp))
                SimpleButton(R.drawable.hd_arrow, !isPaused, 90f, buttonColor, buttonPressed, textColor) {
                    game.handleInput(PlayerAction.HARD_DROP); refreshUi()
                }
            }
        }
    }

    if (isPaused) {
        PauseDialog(
            bestScore = bestScore,
            bestLevel = bestLevel,
            currentScore = state.score,
            currentLevel = state.level,
            isLightTheme = isLightTheme,
            onResume = { isPaused = false },
            onRestart = {
                isPaused = false
                gameId++
            },
            onSettings = { showSettings = true },
            onExitToMenu = onExitToMenu
        )
    }

    if (!state.isRunning) {
        if (showWinDialog && game is GameWithFinish) {
            WinDialog(
                score = state.score,
                level = state.level,
                isLightTheme = isLightTheme,
                onRestart = { showWinDialog = false; gameId++ },
                onExitToMenu = onExitToMenu
            )
        } else if (!showWinDialog) {
            GameOverDialog(
                score = state.score,
                bestScore = bestScore,
                level = state.level,
                bestLevel = bestLevel,
                isLightTheme = isLightTheme,
                onRestart = { gameId++; showWinDialog = false },
                onExitToMenu = onExitToMenu,
                onSettings = { showSettings = true }
            )
        }
    }

    if (showSettings) {
        SettingsDialog(
            isLightTheme = isLightTheme,
            highlightColumn = highlightColumn,
            onLightThemeChange = { isLightTheme = it; gameSettings.isLightTheme = it },
            onHighlightChange = { highlightColumn = it; gameSettings.highlightColumn = it },
            onClose = { showSettings = false }
        )
    }
}