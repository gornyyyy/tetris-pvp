package com.example.tetrisandroid.data

import android.content.Context
import com.example.tetrisandroid.BuildConfig

/**
 * Отвечает только за миграцию версий.
 */
class VersionHandler(context: Context) {
    private val prefs = context.getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)

    fun isVersionChanged(): Boolean {
        val savedVersion = prefs.getInt(KEY_APP_VERSION, -1)
        val currentVersion = BuildConfig.VERSION_CODE
        return savedVersion != currentVersion && savedVersion != -1
    }

    fun saveCurrentVersion() {
        prefs.edit().putInt(KEY_APP_VERSION, BuildConfig.VERSION_CODE).apply()
    }

    fun saveCurrentVersionIfFirstRun() {
        if (prefs.getInt(KEY_APP_VERSION, -1) == -1) {
            prefs.edit().putInt(KEY_APP_VERSION, BuildConfig.VERSION_CODE).apply()
        }
    }

    /**
     * Возвращает текущие рекорды из GameStatsStorage.
     * (Используется для отображения в диалоге обновления).
     */
    fun getOldRecords(statsStorage: GameStatsStorage): LegacyRecords {
        return LegacyRecords(
            score = statsStorage.getGlobalBestScore(),
            level = statsStorage.getGlobalBestLevel()
        )
    }

    data class LegacyRecords(val score: Int, val level: Int)

    companion object {
        private const val KEY_APP_VERSION = "app_version"
    }
}