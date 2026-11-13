package com.example.tetrisandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.tetrisandroid.data.VersionHandler
import com.example.tetrisandroid.data.GameSettings
import com.example.tetrisandroid.data.GameStatsStorage
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

        val versionChanged = versionHandler.isVersionChanged()
        val legacyRecords = if (versionChanged) versionHandler.getLegacyRecords() else null

        setContent {
            TetrisAndroidTheme {
                var currentMode by remember { mutableStateOf<GameMode?>(null) }

                if (currentMode == null) {
                    MenuScreen(
                        gameSettings = gameSettings,
                        statsStorage = statsStorage,
                        oldRecordsOnUpdate = legacyRecords,
                        onUpdateDialogClose = {
                            versionHandler.saveCurrentVersion();
                            versionHandler.clearLegacyRecords()
                        },
                        onStartGame = { mode -> currentMode = mode }
                    )
                } else {
                    TetrisScreen(
                        mode = currentMode!!,
                        statsStorage = statsStorage,
                        gameSettings = gameSettings,
                        onExitToMenu = { currentMode = null }
                    )
                }
            }
        }
    }
}