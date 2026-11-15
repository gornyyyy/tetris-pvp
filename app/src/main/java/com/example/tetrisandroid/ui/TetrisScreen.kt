package com.example.tetrisandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.example.tetrisandroid.R
import com.example.tetrisandroid.audio.GameAudioManager
import com.example.tetrisandroid.data.AudioSettings
import com.example.tetrisandroid.data.GameSettings
import com.example.tetrisandroid.data.GameStatsStorage
import com.example.tetrisandroid.data.LayoutSettings
import com.example.tetrisandroid.data.ThemeType
import com.example.tetrisandroid.game.*
import com.example.tetrisandroid.model.PlayerAction
import com.example.tetrisandroid.ui.components.*
import com.example.tetrisandroid.ui.dialogs.*
import com.example.tetrisandroid.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

fun Color.darken(factor: Float): Color {
    return Color(
        red = red * factor,
        green = green * factor,
        blue = blue * factor,
        alpha = alpha
    )
}

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
    val clearProgress: Float = 0f,
    val linesClearedTotal: Int = 0
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
    layoutSettings: LayoutSettings,
    audioSettings: AudioSettings,
    audioManager: GameAudioManager,
    onExitToMenu: () -> Unit,
    onRestart: () -> Unit,
    onOpenLayoutEditor: () -> Unit
) {
    val topWeight = layoutSettings.topWeight
    val bottomWeight = layoutSettings.bottomWeight
    val fieldWeight = layoutSettings.fieldWeight
    val panelWeight = layoutSettings.panelWeight
    val btnWidth = layoutSettings.actionButtonWidth.dp
    val btnHeight = layoutSettings.actionButtonHeight.dp
    val hPad = layoutSettings.horizontalPadding.dp
    val nextSize = (136f * (topWeight / 0.75f)).coerceIn(80f, 160f).dp

    val game = remember {
        when (mode) {
            GameMode.CLASSIC -> GameClassic(
                onUpdate = { },
                onGameOver = { },
                onLineClear = { lines -> audioManager.playLineClear(lines) }
            )
            GameMode.FAST -> GameFast(
                onUpdate = { },
                onGameOver = { },
                onLineClear = { lines -> audioManager.playLineClear(lines) }
            )
            GameMode.FINISH -> GameWithFinish(
                onUpdate = { },
                onGameOver = { },
                onLineClear = { lines -> audioManager.playLineClear(lines) }
            )
            GameMode.COMPETITIVE -> GameCompetitive(
                onUpdate = { },
                onGameOver = { },
                onLineClear = { lines -> audioManager.playLineClear(lines) }
            )
        }
    }

    var isPaused by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var openedSettingsFromPause by remember { mutableStateOf(false) }
    var showLeavePauseConfirm by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    var themeType by remember { mutableStateOf(gameSettings.themeType) }
    var isLightTheme by remember { mutableStateOf(gameSettings.isLightTheme) }
    var highlightColumn by remember { mutableStateOf(gameSettings.highlightColumn) }

    var bestScore by remember { mutableStateOf(statsStorage.getBestScore(mode.id)) }
    var bestLevel by remember { mutableStateOf(statsStorage.getBestLevel(mode.id)) }

    var statsSaved by remember { mutableStateOf(false) }

    val isClassicOrModern = themeType == ThemeType.CLASSIC || themeType == ThemeType.MODERN

    val baseBackground = if (isLightTheme) LightBackground else DarkBackground
    val baseButtonColor = if (isLightTheme) LightButton else DarkButton
    val baseButtonPressed = if (isLightTheme) LightButtonPressed else DarkButtonPressed
    val baseTextColor = if (isLightTheme) LightText else DarkText
    val baseMutedColor = if (isLightTheme) LightMuted else DarkMuted

    val darkenFactor = if (isClassicOrModern) 0.7f else 1.0f

    val background = baseBackground.darken(darkenFactor)
    val buttonColor = baseButtonColor.darken(darkenFactor)
    val buttonPressed = baseButtonPressed.darken(darkenFactor)

    val textColor = baseTextColor
    val mutedColor = baseMutedColor

    var uiState by remember {
        mutableStateOf(
            UiState(
                grid = Array(24) { IntArray(10) },
                activeBlocks = emptyList(),
                activeColorArgb = 0xFF00FFFF.toInt(),
                nextBlocks = emptyList(),
                nextColorArgb = 0xFF00FFFF.toInt(),
                score = 0, level = 1, isRunning = true, linesClearedTotal = 0
            )
        )
    }

    val state = uiState

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
            clearProgress = game.clearProgress,
            linesClearedTotal = game.linesClearedTotal
        )

        if (!game.isRunning && !statsSaved) {
            statsSaved = true
            val won = (game as? GameWithFinish)?.isWon == true
            val isScoreHighScore = game.score > statsStorage.getBestScore(mode.id)
            val isLevelHighScore = game.level > statsStorage.getBestLevel(mode.id)
            val isAnyHighScore = isScoreHighScore || isLevelHighScore

            statsStorage.saveResult(mode.id, game.score, game.level, game.linesClearedTotal, won)
            bestScore = statsStorage.getBestScore(mode.id)
            bestLevel = statsStorage.getBestLevel(mode.id)

            audioManager.stopMusic()

            if (won) {
                audioManager.playWinSfx()
            } else {
                audioManager.playLoseSfx()
            }
        }
    }

    LaunchedEffect(Unit) {
        audioManager.updateGameLevel(uiState.level)
    }

    LaunchedEffect(uiState.level) {
        if (uiState.level > 1) {
            audioManager.playLevelUp()
        }
        audioManager.updateGameLevel(uiState.level)
    }

    DisposableEffect(isPaused) {
        if (isPaused) audioManager.pauseMusic()
        else audioManager.startGameMusic(game.level)
        onDispose { }
    }

    LaunchedEffect(isPaused) {
        if (isPaused) return@LaunchedEffect
        refreshUi()

        while (game.isRunning) {
            delay(16)
            game.tick()
            refreshUi()
        }
        refreshUi()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        audioManager.pauseMusic()
        audioManager.stopDialogSound()
        if (!isPaused && uiState.isRunning) isPaused = true
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (!isPaused && uiState.isRunning) audioManager.startGameMusic(game.level)
    }

    LaunchedEffect(uiState.isRunning) {
        if (!uiState.isRunning) {
            delay(2500)
            if ((game as? GameWithFinish)?.isWon == true) {
                showWinDialog = true
                audioManager.playDialogMusic(isWin = true, isHighScore = true)
            } else {
                val isAnyHighScore = state.score > bestScore || state.level > bestLevel
                showGameOverDialog = true
                audioManager.playDialogMusic(isWin = false, isHighScore = isAnyHighScore)
            }
        }
    }

    LaunchedEffect(themeType) {
        game.saveAsColor = (themeType == ThemeType.MODERN)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = hPad)
    ) {
        Row(modifier = Modifier.weight(topWeight).fillMaxWidth()) {
            Box(modifier = Modifier.weight(fieldWeight).fillMaxHeight()) {
                TetrisBoardCanvas(
                    grid = state.grid,
                    variantGrid = game.variantGrid,
                    activeBlocks = if (state.clearingLines.isEmpty()) state.activeBlocks else emptyList(),
                    activeColorArgb = state.activeColorArgb,
                    activeVariantIndex = game.currentShape.variantIndex,
                    highlightColumn = highlightColumn,
                    isLightTheme = isLightTheme,
                    themeType = themeType,
                    level = state.level,
                    clearingLines = state.clearingLines,
                    clearProgress = state.clearProgress
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(panelWeight).fillMaxHeight(),
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
                NextShapeCanvas(
                    nextBlocks = state.nextBlocks,
                    colorArgb = state.nextColorArgb,
                    themeType = themeType,
                    level = state.level,
                    variantIndex = game.nextShape.variantIndex,
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
                                detectTapGestures(onTap = { isPaused = true })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("II", color = textColor, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(bottomWeight).fillMaxWidth()) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    ActionButton(
                        iconRes = R.drawable.ic_right_arrow,
                        enabled = !isPaused,
                        rotationDeg = 180f,
                        buttonColor = buttonColor,
                        buttonPressed = buttonPressed,
                        textColor = textColor,
                        buttonWidth = btnWidth,
                        buttonHeight = btnHeight
                    ) { game.handleInput(PlayerAction.MOVE_LEFT); refreshUi() }
                    Spacer(modifier = Modifier.width(8.dp))
                    ActionButton(
                        iconRes = R.drawable.ic_right_arrow,
                        enabled = !isPaused,
                        rotationDeg = 0f,
                        buttonColor = buttonColor,
                        buttonPressed = buttonPressed,
                        textColor = textColor,
                        buttonWidth = btnWidth,
                        buttonHeight = btnHeight
                    ) { game.handleInput(PlayerAction.MOVE_RIGHT); refreshUi() }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(
                    iconRes = R.drawable.ic_right_arrow,
                    enabled = !isPaused,
                    rotationDeg = 90f,
                    buttonColor = buttonColor,
                    buttonPressed = buttonPressed,
                    textColor = textColor,
                    buttonWidth = btnWidth,
                    buttonHeight = btnHeight
                ) { game.handleInput(PlayerAction.MOVE_DOWN); refreshUi() }
            }
            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SimpleButton(
                    iconRes = R.drawable.rotate_arrow,
                    enabled = !isPaused,
                    rotationDeg = 0f,
                    buttonColor = buttonColor,
                    buttonPressed = buttonPressed,
                    textColor = textColor,
                    buttonWidth = btnWidth,
                    buttonHeight = btnHeight
                ) { game.handleInput(PlayerAction.ROTATE); refreshUi() }
                Spacer(modifier = Modifier.height(8.dp))
                SimpleButton(
                    iconRes = R.drawable.hd_arrow,
                    enabled = !isPaused,
                    rotationDeg = 90f,
                    buttonColor = buttonColor,
                    buttonPressed = buttonPressed,
                    textColor = textColor,
                    buttonWidth = btnWidth,
                    buttonHeight = btnHeight
                ) { game.handleInput(PlayerAction.HARD_DROP); refreshUi() }
            }
        }
    }

    if (isPaused) {
        PauseDialog(
            bestScore = bestScore, bestLevel = bestLevel,
            currentScore = state.score, currentLevel = state.level,
            isLightTheme = isLightTheme,
            onResume = { isPaused = false },
            onRestart = { onRestart() },
            onSettings = { openedSettingsFromPause = true; showSettings = true },
            onExitToMenu = onExitToMenu
        )
    }

    if (showGameOverDialog) {
        GameOverDialog(
            score = state.score, bestScore = bestScore,
            level = state.level, bestLevel = bestLevel,
            isLightTheme = isLightTheme,
            onRestart = {
                audioManager.stopDialogSound()
                onRestart()
            },
            onExitToMenu = {
                audioManager.stopDialogSound()
                onExitToMenu()
            },
            onSettings = { openedSettingsFromPause = false; showSettings = true }
        )
    }

    if (showWinDialog) {
        WinDialog(
            score = state.score, level = state.level,
            isLightTheme = isLightTheme,
            onRestart = {
                audioManager.stopDialogSound()
                onRestart()
            },
            onExitToMenu = {
                audioManager.stopDialogSound()
                onExitToMenu()
            }
        )
    }

    if (showSettings) {
        SettingsDialog(
            isLightTheme = isLightTheme,
            highlightColumn = highlightColumn,
            audioSettings = audioSettings,
            currentTheme = themeType,
            onThemeChange = { themeType = it; gameSettings.themeType = it },
            onLightThemeChange = { isLightTheme = it; gameSettings.isLightTheme = it },
            onHighlightChange = { highlightColumn = it; gameSettings.highlightColumn = it },
            onAudioSettingsChanged = { audioManager.updateMusicVolume() },
            onOpenLayoutEditor = {
                showSettings = false
                if (openedSettingsFromPause) showLeavePauseConfirm = true
                else onOpenLayoutEditor()
            },
            onClose = { showSettings = false }
        )
    }

    if (showLeavePauseConfirm) {
        ConfirmLeavePauseDialog(
            isLightTheme = isLightTheme,
            onConfirm = { showLeavePauseConfirm = false; isPaused = false; onOpenLayoutEditor() },
            onDismiss = { showLeavePauseConfirm = false; showSettings = true }
        )
    }
}