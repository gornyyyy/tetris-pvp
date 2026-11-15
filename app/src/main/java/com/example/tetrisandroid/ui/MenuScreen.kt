package com.example.tetrisandroid.ui

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.tetrisandroid.audio.GameAudioManager
import com.example.tetrisandroid.data.AudioSettings
import com.example.tetrisandroid.data.GameSettings
import com.example.tetrisandroid.data.GameStatsStorage
import com.example.tetrisandroid.data.LayoutSettings
import com.example.tetrisandroid.data.VersionHandler
import com.example.tetrisandroid.game.GameMode
import com.example.tetrisandroid.ui.components.ModeSelector
import com.example.tetrisandroid.ui.components.SettingsButton
import com.example.tetrisandroid.ui.components.StartButton
import com.example.tetrisandroid.ui.dialogs.SettingsDialog
import com.example.tetrisandroid.ui.dialogs.UpdateDialog
import com.example.tetrisandroid.ui.theme.*

@Composable
fun MenuScreen(
    gameSettings: GameSettings,
    layoutSettings: LayoutSettings,
    statsStorage: GameStatsStorage,
    audioSettings: AudioSettings,
    audioManager: GameAudioManager,
    oldRecordsOnUpdate: VersionHandler.LegacyRecords?,
    onUpdateDialogClose: () -> Unit,
    onStartGame: (GameMode) -> Unit,
    initialShowLayoutEditor: Boolean = false,
    onLayoutEditorClosed: () -> Unit = {}
) {
    var isLightTheme by remember { mutableStateOf(gameSettings.isLightTheme) }
    var highlightColumn by remember { mutableStateOf(gameSettings.highlightColumn) }
    var selectedMode by remember { mutableStateOf(gameSettings.lastGameMode) }
    var showSettings by remember { mutableStateOf(false) }
    var showLayoutEditor by remember { mutableStateOf(initialShowLayoutEditor) }
    var showUpdateDialog by remember { mutableStateOf(oldRecordsOnUpdate != null) }

    val background = if (isLightTheme) LightBackground else DarkBackground
    val buttonColor = if (isLightTheme) LightButton else DarkButton
    val textColor = if (isLightTheme) LightText else DarkText
    val mutedColor = if (isLightTheme) LightMuted else DarkMuted

    var themeType by remember { mutableStateOf(gameSettings.themeType) }


    LaunchedEffect(Unit) {
        audioManager.playRandomMenuMusic()
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.stopMusic()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        audioManager.stopDialogSound()
        audioManager.stopMusic()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        audioManager.playRandomMenuMusic()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("TETRIS", color = textColor, style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Выбери режим", color = mutedColor, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))

        ModeSelector(
            selectedMode = selectedMode,
            onModeSelected = {
                selectedMode = it
                gameSettings.lastGameMode = it},
            buttonColor = buttonColor,
            textColor = textColor,
            mutedColor = mutedColor,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        StartButton(buttonColor, textColor) {
            audioManager.stopMusic()
            onStartGame(selectedMode)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsButton(buttonColor, mutedColor) { showSettings = true }

        Spacer(modifier = Modifier.height(24.dp))

        ModeStatsCard(
            mode = selectedMode,
            gameSettings = gameSettings,
            statsStorage = statsStorage,
            cardColor = buttonColor,
            textColor = textColor,
            mutedColor = mutedColor
        )

        Spacer(modifier = Modifier.height(32.dp))
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
                showLayoutEditor = true
            },
            onClose = { showSettings = false }
        )
    }

    if (showLayoutEditor) {
        LayoutEditorScreen(
            layoutSettings = layoutSettings,
            isLightTheme = isLightTheme,
            onBack = {
                showLayoutEditor = false
                onLayoutEditorClosed()
            }
        )
    }

    if (showUpdateDialog && oldRecordsOnUpdate != null) {
        UpdateDialog(
            oldRecords = oldRecordsOnUpdate,
            isLightTheme = isLightTheme,
            onClose = {
                showUpdateDialog = false
                onUpdateDialogClose()
            }
        )
    }
}

@Composable
fun ModeStatsCard(
    mode: GameMode,
    gameSettings: GameSettings,
    statsStorage: GameStatsStorage,
    cardColor: Color,
    textColor: Color,
    mutedColor: Color
) {
    var isLightTheme by remember { mutableStateOf(gameSettings.isLightTheme) }

    if (mode == GameMode.COMPETITIVE) {
        Text("Competitive — скоро", color = mutedColor, style = MaterialTheme.typography.bodyMedium)
        return
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Статистика: ${mode.displayName}", color = textColor, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Лучший счёт: ${formatScore(statsStorage.getBestScore(mode.id))}", color = textColor)
            Text("Лучший уровень: ${statsStorage.getBestLevel(mode.id)}", color = textColor)
            Text("Игр сыграно: ${statsStorage.getGamesPlayed(mode.id)}", color = mutedColor)
            Text("Линий всего: ${statsStorage.getTotalLines(mode.id)}", color = mutedColor)
            if (mode == GameMode.FINISH) {
                Text("Побед: ${statsStorage.getWins(mode.id)}", color = if (isLightTheme) LightAccent else DarkAccent)
            }
        }
    }
}