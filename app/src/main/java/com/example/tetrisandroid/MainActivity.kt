package com.example.tetrisandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.tetrisandroid.data.BestScoreStorage
import com.example.tetrisandroid.data.GameSettings
import com.example.tetrisandroid.ui.TetrisScreen
import com.example.tetrisandroid.ui.theme.TetrisAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val storage = BestScoreStorage(applicationContext)

        storage.saveCurrentVersionIfFirstRun()

        val versionChanged = storage.isVersionChanged()
        val oldRecords = if (versionChanged) storage.getOldRecords() else null

        setContent {
            TetrisAndroidTheme {
                val gameSettings = remember { GameSettings(applicationContext) }
                TetrisScreen(
                    bestScoreStorage = storage,
                    gameSettings = gameSettings,
                    oldRecordsOnUpdate = oldRecords,
                    onUpdateDialogClose = {
                        storage.saveCurrentVersion()
                    }
                )
            }
        }
    }
}