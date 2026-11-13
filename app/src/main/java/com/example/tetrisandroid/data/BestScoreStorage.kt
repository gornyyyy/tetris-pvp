package com.example.tetrisandroid.data

import android.content.Context
import com.example.tetrisandroid.BuildConfig

class BestScoreStorage(context: Context) {
    private val prefs = context.getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)

    fun getBestScore(): Int = prefs.getInt(KEY_BEST_SCORE, 0)

    fun saveBestScore(score: Int) {
        if (score > getBestScore()) {
            prefs.edit().putInt(KEY_BEST_SCORE, score).apply()
        }
    }

    fun getBestLevel(): Int = prefs.getInt(KEY_BEST_LEVEL, 1)

    fun saveBestLevel(level: Int) {
        if (level > getBestLevel()) {
            prefs.edit().putInt(KEY_BEST_LEVEL, level).apply()
        }
    }

    fun saveResults(score: Int, level: Int) {
        val editor = prefs.edit()
        if (score > getBestScore()) editor.putInt(KEY_BEST_SCORE, score)
        if (level > getBestLevel()) editor.putInt(KEY_BEST_LEVEL, level)
        editor.apply()
    }

    /**
     * Возвращает true, если версия приложения изменилась (или это первый запуск).
     * НЕ сбрасывает рекорды — только сообщает, что нужно показать диалог.
     */
    fun isVersionChanged(): Boolean {
        val savedVersion = prefs.getInt(KEY_APP_VERSION, -1)
        val currentVersion = BuildConfig.VERSION_CODE
        return savedVersion != currentVersion && savedVersion != -1
    }

    /**
     * Возвращает текущие рекорды (для показа в диалоге обновления).
     */
    fun getOldRecords(): OldRecords {
        return OldRecords(getBestScore(), getBestLevel())
    }

    /**
     * Сбрасывает рекорды и сохраняет текущую версию.
     * Вызывается после нажатия кнопки в диалоге обновления.
     */
    fun resetRecordsAndSaveVersion() {
        prefs.edit()
            .remove(KEY_BEST_SCORE)
            .remove(KEY_BEST_LEVEL)
            .putInt(KEY_APP_VERSION, BuildConfig.VERSION_CODE)
            .apply()
    }
    fun saveCurrentVersion() {
        prefs.edit().putInt(KEY_APP_VERSION, BuildConfig.VERSION_CODE).apply()
    }

    /**
     * Вызывается при первом запуске (или если версия не сохранена),
     * чтобы просто записать текущую версию без диалога.
     */
    fun saveCurrentVersionIfFirstRun() {
        if (prefs.getInt(KEY_APP_VERSION, -1) == -1) {
            prefs.edit().putInt(KEY_APP_VERSION, BuildConfig.VERSION_CODE).apply()
        }
    }

    data class OldRecords(val score: Int, val level: Int)

    companion object {
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_BEST_LEVEL = "best_level"
        private const val KEY_APP_VERSION = "app_version"
    }
}