package com.example.tetrisandroid.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.tetrisandroid.BuildConfig
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.example.tetrisandroid.audio.GameAudioManager
import com.example.tetrisandroid.data.*
import com.example.tetrisandroid.game.*
import com.example.tetrisandroid.model.PlayerAction
import com.example.tetrisandroid.pvp.PvpClient
import com.example.tetrisandroid.pvp.PvpEvent
import com.example.tetrisandroid.ui.dialogs.*
import com.example.tetrisandroid.ui.theme.*
import kotlinx.coroutines.delay

enum class PvpMatchStatus { PLAYING, WAITING_OPPONENT, WINNER, LOSER }

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

    val isPvp = mode == GameMode.COMPETITIVE
    val pvpClient = remember(isPvp) {
        if (isPvp) PvpClient(BuildConfig.PVP_SERVER_URL) else null
    }
    var roomId by remember { mutableStateOf<String?>(null) }
    var playerName by remember { mutableStateOf("Player") }
    var roomCreated by remember { mutableStateOf(false) }
    var pendingRoomId by remember { mutableStateOf<String?>(null) }

    val game = remember(mode) {
        when (mode) {
            GameMode.CLASSIC -> GameClassic({}, {}, onLineClear = { lines -> audioManager.playLineClear(lines) })
            GameMode.FAST -> GameFast({}, {}, onLineClear = { lines -> audioManager.playLineClear(lines) })
            GameMode.FINISH -> GameWithFinish({}, {}, onLineClear = { lines -> audioManager.playLineClear(lines) })
            GameMode.COMPETITIVE -> GameCompetitive(
                onUpdate = { },
                onGameOver = { /* локальный game over — обработается в refreshUi */ },
                onAttack = { },
                onLineClearEvent = { lines -> pvpClient?.sendLineClear(lines) },
                onScoreChanged = { s, l, ln -> pvpClient?.sendScore(s, l, ln) },
                onGameOverEvent = { pvpClient?.sendGameOver() },
                onWinEvent = { pvpClient?.sendWin() },
                onLineClear = { lines -> audioManager.playLineClear(lines) }
            )
        }
    }

    var isPaused by remember { mutableStateOf(false) }
    var showPvpPause by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var openedSettingsFromPause by remember { mutableStateOf(false) }
    var showLeavePauseConfirm by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    var themeType by remember { mutableStateOf(gameSettings.themeType) }
    var isLightTheme by remember { mutableStateOf(gameSettings.isLightTheme) }
    var highlightColumn by remember { mutableStateOf(gameSettings.highlightColumn) }
    var keepFallenColor by remember { mutableStateOf(gameSettings.keepFallenColor) }

    var bestScore by remember { mutableStateOf(statsStorage.getBestScore(mode.id)) }
    var bestLevel by remember { mutableStateOf(statsStorage.getBestLevel(mode.id)) }
    var statsSaved by remember { mutableStateOf(false) }

    val isClassicOrModern = themeType == ThemeType.CLASSIC || themeType == ThemeType.MODERN
    val darkenFactor = if (isClassicOrModern) 0.7f else 1.0f

    var matchStarted by remember { mutableStateOf(!isPvp) }

    var opponentNickname by remember { mutableStateOf("Соперник") }
    var pvpMatchStatus by remember { mutableStateOf(PvpMatchStatus.PLAYING) }
    var myGameOver by remember { mutableStateOf(false) }
    var opponentGameOver by remember { mutableStateOf(false) }

    var opponentScore by remember { mutableStateOf(0) }
    var opponentLevel by remember { mutableStateOf(1) }
    var opponentLines by remember { mutableStateOf(0) }
    var opponentConnected by remember { mutableStateOf(false) }
    var pvpStatus by remember { mutableStateOf("Ожидание соперника...") }
    var showPvpSurrenderConfirm by remember { mutableStateOf(false) }
    var showPvpResult by remember { mutableStateOf(false) }
    var pvpResultVictory by remember { mutableStateOf(false) }

    val background = (if (isLightTheme) LightBackground else DarkBackground).darken(darkenFactor)
    val buttonColor = (if (isLightTheme) LightButton else DarkButton).darken(darkenFactor)
    val buttonPressed = (if (isLightTheme) LightButtonPressed else DarkButtonPressed).darken(darkenFactor)
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
                score = 0, level = 1, isRunning = true, linesClearedTotal = 0
            )
        )
    }
    val state = uiState

    if (isPvp && roomId == null) {
        PvpRoomScreen(
            isLightTheme = isLightTheme,
            onCreateRoom = { code, name ->
                playerName = name
                pvpClient?.connect()
                pvpClient?.createRoom(code, name)
                pendingRoomId = code
            },
            onJoinRoom = { code, name ->
                playerName = name
                roomId = code
            },
            onBack = onExitToMenu
        )
        return
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
            clearProgress = game.clearProgress,
            linesClearedTotal = game.linesClearedTotal
        )

        if (!game.isRunning && !statsSaved) {
            statsSaved = true
            val won = (game as? GameWithFinish)?.isWon == true
            statsStorage.saveResult(mode.id, game.score, game.level, game.linesClearedTotal, won)
            bestScore = statsStorage.getBestScore(mode.id)
            bestLevel = statsStorage.getBestLevel(mode.id)
            audioManager.stopMusic()
            if (won) audioManager.playWinSfx() else audioManager.playLoseSfx()

            if (game is GameCompetitive) {
                myGameOver = true
                game.notifyGameOver()

                when {
                    opponentGameOver -> {
                        val iWin = uiState.score > opponentScore
                        pvpMatchStatus = if (iWin) PvpMatchStatus.WINNER else PvpMatchStatus.LOSER
                        pvpResultVictory = iWin
                        showPvpResult = true
                    }
                    opponentScore > uiState.score -> {
                        pvpMatchStatus = PvpMatchStatus.LOSER
                        pvpResultVictory = false
                        showPvpResult = true
                    }
                    else -> {
                        pvpMatchStatus = PvpMatchStatus.WAITING_OPPONENT
                    }
                }
            }
        }
    }

    // --- Эффекты (музыка, таймер, диалоги) ---
    LaunchedEffect(isPvp) {
        if (!isPvp || pvpClient == null || roomId == null) return@LaunchedEffect
        pvpClient.joinRoom(roomId!!, playerName)

        pvpClient.events.collect { event ->
            when (event) {
                is PvpEvent.RoomCreated -> {
                    pendingRoomId = null
                }
                is PvpEvent.Joined -> {
                    opponentConnected = event.opponentConnected
                    event.opponentName?.let { opponentNickname = it }
                    pvpStatus = if (event.opponentConnected) "Матч начался" else "Ожидание соперника…"
                }

                PvpEvent.Start -> {
                    pvpStatus = "Матч начался"
                    matchStarted = true
                    if (myGameOver) {
                        when {
                            opponentGameOver -> {
                                val iWin = uiState.score > opponentScore
                                pvpMatchStatus = if (iWin) PvpMatchStatus.WINNER else PvpMatchStatus.LOSER
                                pvpResultVictory = iWin
                                showPvpResult = true
                            }
                            opponentScore > uiState.score -> {
                                pvpMatchStatus = PvpMatchStatus.LOSER
                                pvpResultVictory = false
                                showPvpResult = true
                            }
                            else -> pvpMatchStatus = PvpMatchStatus.WAITING_OPPONENT
                        }
                    }
                }

                is PvpEvent.OpponentScore -> {
                    opponentScore = event.score
                    opponentLevel = event.level
                    opponentLines = event.lines

                    if (myGameOver && pvpMatchStatus == PvpMatchStatus.WAITING_OPPONENT) {
                        if (event.score > uiState.score) {
                            pvpMatchStatus = PvpMatchStatus.LOSER
                            pvpResultVictory = false
                            showPvpResult = true
                        } else if (opponentGameOver) {
                            val iWin = uiState.score > event.score
                            pvpMatchStatus = if (iWin) PvpMatchStatus.WINNER else PvpMatchStatus.LOSER
                            pvpResultVictory = iWin
                            showPvpResult = true
                        }
                    }
                }

                PvpEvent.OpponentGameOver -> {
                    opponentGameOver = true
                    pvpStatus = "Соперник проиграл"
                    if (myGameOver) {
                        val iWin = uiState.score > opponentScore
                        pvpMatchStatus = if (iWin) PvpMatchStatus.WINNER else PvpMatchStatus.LOSER
                        pvpResultVictory = iWin
                        showPvpResult = true
                    } else {
                        pvpMatchStatus = PvpMatchStatus.WINNER
                    }
                }

                PvpEvent.OpponentWin -> {
                    opponentGameOver = true
                    if (myGameOver) {
                        pvpMatchStatus = PvpMatchStatus.LOSER
                        pvpResultVictory = false
                        showPvpResult = true
                    }
                }

                PvpEvent.OpponentDisconnected -> {
                    opponentConnected = false
                    opponentGameOver = true
                    pvpStatus = "Соперник отключился"
                    if (myGameOver) {
                        val iWin = uiState.score > opponentScore
                        pvpMatchStatus = if (iWin) PvpMatchStatus.WINNER else PvpMatchStatus.LOSER
                        pvpResultVictory = iWin
                        showPvpResult = true
                    } else {
                        pvpMatchStatus = if (uiState.score > opponentScore) PvpMatchStatus.WINNER else PvpMatchStatus.PLAYING
                    }
                }

                is PvpEvent.Error -> {
                    pvpStatus = "Ошибка: ${event.message}"
                }

                PvpEvent.Disconnected -> {
                    opponentConnected = false
                    pvpStatus = "Отключено"
                }

                is PvpEvent.AddGarbage -> {
                    (game as? GameCompetitive)?.receiveGarbage(event.lines)
                    refreshUi()
                }
            }
        }
    }
    DisposableEffect(isPvp) {
        onDispose { pvpClient?.disconnect() }
    }

    LaunchedEffect(Unit) { audioManager.updateGameLevel(uiState.level) }
    LaunchedEffect(uiState.level) {
        if (uiState.level > 1) audioManager.playLevelUp()
        audioManager.updateGameLevel(uiState.level)
    }
    DisposableEffect(isPaused) {
        if (isPaused) audioManager.pauseMusic() else audioManager.startGameMusic(game.level)
        onDispose { }
    }
    LaunchedEffect(isPaused,showPvpPause, matchStarted) {
        val shouldStop = if (isPvp) false else isPaused
        if (shouldStop || !matchStarted) return@LaunchedEffect
        refreshUi()
        while (game.isRunning) { delay(16); game.tick(); refreshUi() }
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
    LaunchedEffect(uiState.isRunning && !isPvp) {
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

    val onPlayerAction: (PlayerAction) -> Unit = { action ->
        game.handleInput(action)
        refreshUi()
    }

    // экран
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
                GameField(
                    state = state,
                    variantGrid = game.variantGrid,
                    currentVariantIndex = game.currentShape.variantIndex,
                    highlightColumn = highlightColumn,
                    isLightTheme = isLightTheme,
                    themeType = themeType,
                    keepFallenColor = keepFallenColor,
                    garbageLines = game.garbageLines,
                    garbageProgress = game.garbageProgress
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .weight(if (isPvp) panelWeight * 0.85f else panelWeight)
                    .fillMaxHeight()
            ) {
                if (isPvp) {
                    PvpGameInfoPanel(
                        state = state,
                        opponentNickname = opponentNickname,
                        opponentScore = opponentScore,
                        opponentConnected = opponentConnected,
                        pvpStatus = pvpStatus,
                        pvpMatchStatus = pvpMatchStatus,
                        themeType = themeType,
                        nextVariantIndex = game.nextShape.variantIndex,
                        isLightTheme = isLightTheme,
                        isPaused = isPaused,
                        buttonColor = buttonColor,
                        textColor = textColor,
                        mutedColor = mutedColor,
                        nextSize = nextSize,
                        onPause = { isPaused = true }
                    )
                } else {
                    GameInfoPanel(
                        mode = mode,
                        state = state,
                        bestScore = bestScore,
                        bestLevel = bestLevel,
                        isLightTheme = isLightTheme,
                        themeType = themeType,
                        nextVariantIndex = game.nextShape.variantIndex,
                        isPaused = isPaused,
                        buttonColor = buttonColor,
                        textColor = textColor,
                        mutedColor = mutedColor,
                        nextSize = nextSize,
                        onPause = { isPaused = true }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(bottomWeight).fillMaxWidth()) {
            GameControlButtons(
                enabled = !isPaused,
                buttonColor = buttonColor,
                buttonPressed = buttonPressed,
                textColor = textColor,
                btnWidth = btnWidth,
                btnHeight = btnHeight,
                onAction = onPlayerAction
            )
        }
    }

    // Диалоги
    if (isPaused && isPvp) {
        PvpPauseDialog(
            isLightTheme = isLightTheme,
            isWinner = pvpMatchStatus == PvpMatchStatus.WINNER,
            onResume = { isPaused = false },
            onSurrender = { showPvpSurrenderConfirm = true },
            onExitToMenu = {
                isPaused = false
                pvpClient?.disconnect()
                onExitToMenu()
            }
        )
    }else if (isPaused && !isPvp) {
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
    if (showPvpSurrenderConfirm) {
        ConfirmLeavePauseDialog(
            isLightTheme = isLightTheme,
            onConfirm = {
                showPvpSurrenderConfirm = false
                isPaused = false
                (game as? GameCompetitive)?.notifyGameOver()
                onExitToMenu()
            },
            onDismiss = { showPvpSurrenderConfirm = false }
        )
    }
    if (showPvpResult) {
        PvpResultDialog(
            isVictory = pvpResultVictory,
            myScore = state.score,
            opponentScore = opponentScore,
            isLightTheme = isLightTheme,
            onExitToMenu = {
                showPvpResult = false
                audioManager.stopDialogSound()
                onExitToMenu()
            }
        )
    }
    if (showGameOverDialog) {
        GameOverDialog(
            score = state.score, bestScore = bestScore,
            level = state.level, bestLevel = bestLevel,
            isLightTheme = isLightTheme,
            onRestart = { audioManager.stopDialogSound(); onRestart() },
            onExitToMenu = { audioManager.stopDialogSound(); onExitToMenu() },
            onSettings = { openedSettingsFromPause = false; showSettings = true }
        )
    }
    if (showWinDialog) {
        WinDialog(
            score = state.score, level = state.level,
            isLightTheme = isLightTheme,
            onRestart = { audioManager.stopDialogSound(); onRestart() },
            onExitToMenu = { audioManager.stopDialogSound(); onExitToMenu() }
        )
    }
    if (showSettings) {
        SettingsDialog(
            isLightTheme = isLightTheme,
            highlightColumn = highlightColumn,
            audioSettings = audioSettings,
            currentTheme = themeType,
            keepFallenColor = keepFallenColor,
            onThemeChange = { themeType = it; gameSettings.themeType = it },
            onLightThemeChange = { isLightTheme = it; gameSettings.isLightTheme = it },
            onHighlightChange = { highlightColumn = it; gameSettings.highlightColumn = it },
            onKeepFallenColorChange = { keepFallenColor = it; gameSettings.keepFallenColor = it },
            onAudioSettingsChanged = { audioManager.updateMusicVolume() },
            onOpenLayoutEditor = {
                showSettings = false
                if (openedSettingsFromPause) showLeavePauseConfirm = true else onOpenLayoutEditor()
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