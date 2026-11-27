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
import com.example.tetrisandroid.data.PatchNote
import com.example.tetrisandroid.data.VersionHandler
import com.example.tetrisandroid.data.getUpdateNotes
import com.example.tetrisandroid.game.GameMode
import com.example.tetrisandroid.pvp.PvpTransport
import com.example.tetrisandroid.ui.screens.MenuScreen
import com.example.tetrisandroid.ui.screens.PvpRoomScreen
import com.example.tetrisandroid.ui.screens.TetrisScreen
import com.example.tetrisandroid.ui.theme.TetrisAndroidTheme

sealed interface Screen {
    object Menu : Screen
    object PvpRoom : Screen
    data class Game(
        val mode: GameMode,
        val transport: PvpTransport?,
        val roomId: String?,
        val playerName: String,
        val startLevel: Int = 1,
        val enableGarbage: Boolean = true
    ) : Screen
}

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

        setContent {
            TetrisAndroidTheme {
                val audioManager = remember { GameAudioManager(applicationContext, audioSettings) }

                // Состояние навигации
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
                var gameSessionKey by remember { mutableStateOf(0) }
                var openLayoutEditorAfterMenu by remember { mutableStateOf(false) }

                var updateNotes by remember { mutableStateOf<List<PatchNote>>(emptyList()) }
                var updateRecords by remember { mutableStateOf<VersionHandler.LegacyRecords?>(null) }

                LaunchedEffect(Unit) {
                    val savedVersion = versionHandler.getSavedVersion()
                    val currentVersion = BuildConfig.VERSION_CODE

                    if (savedVersion != -1 && savedVersion < currentVersion) {
                        updateNotes = getUpdateNotes(savedVersion, currentVersion)
                        updateRecords = versionHandler.getOldRecords(statsStorage)
                    } else {
                        versionHandler.saveCurrentVersionIfFirstRun()
                    }
                }

                when (val screen = currentScreen) {
                    is Screen.Menu -> {
                        MenuScreen(
                            gameSettings = gameSettings,
                            layoutSettings = layoutSettings,
                            statsStorage = statsStorage,
                            audioSettings = audioSettings,
                            audioManager = audioManager,
                            oldRecordsOnUpdate = updateRecords,
                            updateNotes = updateNotes,
                            onUpdateDialogClose = {
                                versionHandler.saveCurrentVersion()
                                updateRecords = null
                                updateNotes = emptyList()
                            },
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
                            audioManager = audioManager,
                            onStartGameWithTransport = { transport, roomId, playerName, startLevel, enableGarbage ->
                                currentScreen = Screen.Game(
                                    mode = GameMode.COMPETITIVE,
                                    transport = transport,
                                    roomId = roomId,
                                    playerName = playerName,
                                    startLevel = startLevel,
                                    enableGarbage = enableGarbage
                                )
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
                                startLevel = screen.startLevel,
                                enableGarbage = screen.enableGarbage,
                                onExitToMenu = { currentScreen = Screen.Menu },
                                onRestart = { gameSessionKey++ },
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