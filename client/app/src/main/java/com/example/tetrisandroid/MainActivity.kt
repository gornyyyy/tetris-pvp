package com.example.tetrisandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.tetrisandroid.audio.GameAudioManager
import com.example.tetrisandroid.data.AudioSettings
import com.example.tetrisandroid.data.GameSettings
import com.example.tetrisandroid.data.GameStatsStorage
import com.example.tetrisandroid.data.LayoutSettings
import com.example.tetrisandroid.data.VersionHandler
import com.example.tetrisandroid.game.GameMode
import com.example.tetrisandroid.pvp.PvpTransport
import com.example.tetrisandroid.ui.screens.MenuScreen
import com.example.tetrisandroid.ui.screens.PvpRoomScreen
import com.example.tetrisandroid.ui.screens.TetrisScreen
import com.example.tetrisandroid.ui.theme.TetrisAndroidTheme

// Простая навигация без внешних библиотек
sealed interface Screen {
    object Menu : Screen
    object PvpRoom : Screen
    data class Game(
        val mode: GameMode,
        val transport: PvpTransport?,
        val roomId: String?,
        val playerName: String
    ) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val versionHandler = VersionHandler(applicationContext)
        versionHandler.saveCurrentVersion()

        val statsStorage = GameStatsStorage(applicationContext)
        val gameSettings = GameSettings(applicationContext)
        val layoutSettings = LayoutSettings(applicationContext)
        val audioSettings = AudioSettings(applicationContext)

        setContent {
            TetrisAndroidTheme {
                val audioManager = remember { GameAudioManager(applicationContext, audioSettings) }

                // Состояние навигации
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
                var gameSessionKey by remember { mutableStateOf(0) }
                var openLayoutEditorAfterMenu by remember { mutableStateOf(false) }

                when (val screen = currentScreen) {
                    is Screen.Menu -> {
                        MenuScreen(
                            gameSettings = gameSettings,
                            layoutSettings = layoutSettings,
                            statsStorage = statsStorage,
                            audioSettings = audioSettings,
                            audioManager = audioManager,
                            oldRecordsOnUpdate = null,
                            onUpdateDialogClose = {},
                            onNavigateToPvpRoom = { currentScreen = Screen.PvpRoom },
                            onStartSinglePlayerGame = { mode ->
                                currentScreen = Screen.Game(mode, null, null, "Player")
                                gameSessionKey++
                            },
                            initialShowLayoutEditor = openLayoutEditorAfterMenu,
                            onLayoutEditorClosed = { openLayoutEditorAfterMenu = false }
                        )
                    }

                    is Screen.PvpRoom -> {
                        PvpRoomScreen(
                            isLightTheme = gameSettings.isLightTheme,
                            onStartGameWithTransport = { transport, roomId, playerName ->
                                currentScreen = Screen.Game(GameMode.COMPETITIVE, transport, roomId, playerName)
                                gameSessionKey++
                            },
                            onBack = { currentScreen = Screen.Menu }
                        )
                    }

                    is Screen.Game -> {
                        key(gameSessionKey) {
                            TetrisScreen(
                                mode = screen.mode,
                                statsStorage = statsStorage,
                                gameSettings = gameSettings,
                                layoutSettings = layoutSettings,
                                audioSettings = audioSettings,
                                audioManager = audioManager,
                                transport = screen.transport,
                                roomId = screen.roomId,
                                playerName = screen.playerName,
                                onExitToMenu = {
                                    currentScreen = Screen.Menu
                                },
                                onRestart = {
                                    gameSessionKey++
                                },
                                onOpenLayoutEditor = {
                                    currentScreen = Screen.Menu
                                    openLayoutEditorAfterMenu = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}