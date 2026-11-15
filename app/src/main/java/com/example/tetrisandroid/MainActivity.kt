package com.example.tetrisandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.tetrisandroid.audio.GameAudioManager
import com.example.tetrisandroid.data.AudioSettings
import com.example.tetrisandroid.data.VersionHandler
import com.example.tetrisandroid.data.GameSettings
import com.example.tetrisandroid.data.GameStatsStorage
import com.example.tetrisandroid.data.LayoutSettings
import com.example.tetrisandroid.game.GameMode
import com.example.tetrisandroid.ui.MenuScreen
import com.example.tetrisandroid.ui.TetrisScreen
import com.example.tetrisandroid.ui.theme.TetrisAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val versionHandler = VersionHandler(applicationContext)
        versionHandler.saveCurrentVersionIfFirstRun()

        val statsStorage = GameStatsStorage(applicationContext)
        val gameSettings = GameSettings(applicationContext)
        val layoutSettings = LayoutSettings(applicationContext)
        val audioSettings = AudioSettings(applicationContext)

        val versionChanged = versionHandler.isVersionChanged()
        val initialOldRecords = if (versionChanged) versionHandler.getOldRecords(statsStorage) else null

        setContent {
            TetrisAndroidTheme {
                val audioManager = remember { GameAudioManager(applicationContext, audioSettings) }
                var currentMode by remember { mutableStateOf<GameMode?>(null) }
                var oldRecords by remember { mutableStateOf(initialOldRecords) }
                var openLayoutEditorAfterMenu by remember { mutableStateOf(false) }
                var gameSessionKey by remember { mutableStateOf(0) }

                if (currentMode == null) {
                    MenuScreen(
                        gameSettings = gameSettings,
                        layoutSettings = layoutSettings,
                        statsStorage = statsStorage,
                        audioSettings = audioSettings,
                        audioManager = audioManager,
                        oldRecordsOnUpdate = oldRecords,
                        onUpdateDialogClose = {
                            versionHandler.saveCurrentVersion()
                            oldRecords = null
                        },
                        onStartGame = { mode ->
                            currentMode = mode
                            gameSessionKey++
                        },
                        initialShowLayoutEditor = openLayoutEditorAfterMenu,
                        onLayoutEditorClosed = { openLayoutEditorAfterMenu = false }
                    )
                } else {
                    key(gameSessionKey) {
                        TetrisScreen(
                            mode = currentMode!!,
                            statsStorage = statsStorage,
                            gameSettings = gameSettings,
                            layoutSettings = layoutSettings,
                            audioSettings = audioSettings,
                            audioManager = audioManager,
                            onExitToMenu = { currentMode = null },
                            onRestart = { gameSessionKey++ },
                            onOpenLayoutEditor = {
                                currentMode = null
                                openLayoutEditorAfterMenu = true
                            }
                        )
                    }
                }
            }
        }
    }
}