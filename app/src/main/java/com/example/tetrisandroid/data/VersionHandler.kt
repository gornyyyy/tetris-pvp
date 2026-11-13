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

    fun getLegacyRecords(): LegacyRecords? {
        val hasScore = prefs.contains(LEGACY_BEST_SCORE)
        val hasLevel = prefs.contains(LEGACY_BEST_LEVEL)
        if (!hasScore && !hasLevel) return null
        return LegacyRecords(
            prefs.getInt(LEGACY_BEST_SCORE, 0),
            prefs.getInt(LEGACY_BEST_LEVEL, 1)
        )
    }

    fun clearLegacyRecords() {
        prefs.edit()
            .remove(LEGACY_BEST_SCORE)
            .remove(LEGACY_BEST_LEVEL)
            .apply()
    }

    data class LegacyRecords(val score: Int, val level: Int)

    companion object {
        private const val KEY_APP_VERSION = "app_version"
        private const val LEGACY_BEST_SCORE = "best_score"
        private const val LEGACY_BEST_LEVEL = "best_level"
    }

}