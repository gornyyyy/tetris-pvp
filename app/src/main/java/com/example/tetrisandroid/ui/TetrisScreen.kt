package com.example.tetrisandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.example.tetrisandroid.R
import com.example.tetrisandroid.data.BestScoreStorage
import com.example.tetrisandroid.data.GameSettings
import com.example.tetrisandroid.game.Game
import com.example.tetrisandroid.model.PlayerAction
import com.example.tetrisandroid.ui.components.ActionButton
import com.example.tetrisandroid.ui.components.NextShapeCanvas
import com.example.tetrisandroid.ui.components.SettingsButton
import com.example.tetrisandroid.ui.components.SimpleButton
import com.example.tetrisandroid.ui.components.StartButton
import com.example.tetrisandroid.ui.components.TetrisBoardCanvas
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
    val isRunning: Boolean
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
    bestScoreStorage: BestScoreStorage,
    gameSettings: GameSettings,
    oldRecordsOnUpdate: BestScoreStorage.OldRecords? = null,
    onUpdateDialogClose: () -> Unit = {}
) {
    val game = remember { Game(onUpdate = {}, onGameOver = {}) }

    var bestScore by remember { mutableStateOf(bestScoreStorage.getBestScore()) }
    var bestLevel by remember { mutableStateOf(bestScoreStorage.getBestLevel()) }
    var isStarted by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var gameId by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    var isLightTheme by remember { mutableStateOf(gameSettings.isLightTheme) }
    var highlightColumn by remember { mutableStateOf(gameSettings.highlightColumn) }
    var showUpdateDialog by remember { mutableStateOf(oldRecordsOnUpdate != null) }

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
            isRunning = game.isRunning
        )
        if (!game.isRunning) {
            bestScoreStorage.saveResults(game.score, game.level)
            bestScore = bestScoreStorage.getBestScore()
            bestLevel = bestScoreStorage.getBestLevel()
        }
    }

    LaunchedEffect(gameId, isPaused) {
        if (!isStarted || isPaused) return@LaunchedEffect
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
        if (isStarted && !isPaused && state.isRunning) isPaused = true
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
                modifier = Modifier.weight(0.7f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                if (!isStarted) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StartButton(buttonColor, textColor) {
                            game.reset()
                            isStarted = true
                            isPaused = false
                            gameId++
                            refreshUi()
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsButton(buttonColor, mutedColor) { showSettings = true }
                    }
                } else {
                    TetrisBoardCanvas(
                        grid = state.grid,
                        activeBlocks = state.activeBlocks,
                        activeColorArgb = state.activeColorArgb,
                        highlightColumn = highlightColumn,
                        isLightTheme = isLightTheme
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(0.3f).fillMaxHeight(),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("SCORE", color = mutedColor, style = MaterialTheme.typography.labelMedium)
                Text(formatScore(state.score), color = textColor, style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(8.dp))
                Text("BEST", color = mutedColor, style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatScore(bestScore), color = Color(0xFFFFD700), style = MaterialTheme.typography.titleLarge)
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

                if (isStarted && state.isRunning && !isPaused) {
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
                    ActionButton(R.drawable.ic_right_arrow, isStarted && !isPaused, 180f, buttonColor, buttonPressed, textColor) {
                        game.handleInput(PlayerAction.MOVE_LEFT); refreshUi()
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ActionButton(R.drawable.ic_right_arrow, isStarted && !isPaused, 0f, buttonColor, buttonPressed, textColor) {
                        game.handleInput(PlayerAction.MOVE_RIGHT); refreshUi()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(R.drawable.ic_right_arrow, isStarted && !isPaused, 90f, buttonColor, buttonPressed, textColor) {
                    game.handleInput(PlayerAction.MOVE_DOWN); refreshUi()
                }
            }

            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SimpleButton(R.drawable.rotate_arrow, isStarted && !isPaused, 0f, buttonColor, buttonPressed, textColor) {
                    game.handleInput(PlayerAction.ROTATE); refreshUi()
                }
                Spacer(modifier = Modifier.height(8.dp))
                SimpleButton(R.drawable.hd_arrow, isStarted && !isPaused, 90f, buttonColor, buttonPressed, textColor) {
                    game.handleInput(PlayerAction.HARD_DROP); refreshUi()
                }
            }
        }
    }

    if (isStarted && !state.isRunning) {
        GameOverDialog(
            score = state.score, bestScore = bestScore,
            level = state.level, bestLevel = bestLevel,
            isLightTheme = isLightTheme,
            onRestart = { game.reset(); isPaused = false; gameId++; refreshUi() },
            onSettings = { showSettings = true }
        )
    }

    if (isPaused) {
        PauseDialog(
            bestScore = bestScore, bestLevel = bestLevel,
            currentScore = state.score, currentLevel = state.level,
            isLightTheme = isLightTheme,
            onResume = { isPaused = false },
            onRestart = { game.reset(); isPaused = false; gameId++; refreshUi() },
            onSettings = { showSettings = true }
        )
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

    if (showUpdateDialog && oldRecordsOnUpdate != null) {
        UpdateDialog(
            oldRecords = oldRecordsOnUpdate,
            isLightTheme = isLightTheme,
            onClose = {
                showUpdateDialog = false
                onUpdateDialogClose()
                bestScore = 0
                bestLevel = 1
            }
        )
    }
}