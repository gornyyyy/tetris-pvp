package com.example.tetrisandroid.data

import android.content.Context
import com.example.tetrisandroid.BuildConfig

// Модель для описания одного патча
data class PatchNote(val version: Int, val notes: List<String>)

/**
 * Возвращает список обновлений, которые нужно показать пользователю.
 * Показывает только те патчи, версия которых больше старой, но не больше текущей.
 */
fun getUpdateNotes(oldVersion: Int, currentVersion: Int): List<PatchNote> {
    val allPatches = listOf(
        PatchNote(4, listOf(
            "Добавлены скинчики",
            "Добавлены музыка и всякие приколюшки",
            "Исправлены старые штуки, баги и т.д.\nВ целом и не важно что конкретно"
        )),
        PatchNote(5, listOf(
            "PvP режим через сервер",
            "Но сервер может быть недоступен"
        )),
        PatchNote(6, listOf(
            "Добавлена возможность играть по локалке"
        )),
        PatchNote(7, listOf(
            "Оптимизация отрисовки поля",
            "Упрощено подключение к комнате при \nигре в локальной сети"
        )
        )
    )

    return allPatches.filter { it.version > oldVersion && it.version <= currentVersion }
}

class VersionHandler(context: Context) {
    private val prefs = context.getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)

    fun getSavedVersion(): Int = prefs.getInt(KEY_APP_VERSION, -1)

    fun isVersionChanged(): Boolean {
        val saved = getSavedVersion()
        val current = BuildConfig.VERSION_CODE
        return saved != -1 && saved < current
    }

    fun saveCurrentVersion() {
        prefs.edit().putInt(KEY_APP_VERSION, BuildConfig.VERSION_CODE).apply()
    }

    fun saveCurrentVersionIfFirstRun() {
        if (getSavedVersion() == -1) {
            saveCurrentVersion()
        }
    }

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