package com.example.tetrisandroid.data

import android.content.Context
import com.example.tetrisandroid.game.GameMode

class GameStatsStorage(context: Context) {
    private val prefs = context.getSharedPreferences("tetris_stats", Context.MODE_PRIVATE)

    // --- Глобальный BEST (лучший результат среди всех режимов) ---
    fun getGlobalBestScore(): Int = prefs.getInt("global_best_score", 0)
    fun getGlobalBestLevel(): Int = prefs.getInt("global_best_level", 1)

    private fun saveGlobalBest(score: Int, level: Int) {
        val editor = prefs.edit()
        if (score > getGlobalBestScore()) editor.putInt("global_best_score", score)
        if (level > getGlobalBestLevel()) editor.putInt("global_best_level", level)
        editor.apply()
    }

    // --- Статистика по конкретному режиму ---
    fun getBestScore(modeId: String): Int = prefs.getInt("${modeId}_best_score", 0)
    fun getBestLevel(modeId: String): Int = prefs.getInt("${modeId}_best_level", 1)
    fun getGamesPlayed(modeId: String): Int = prefs.getInt("${modeId}_games", 0)
    fun getTotalLines(modeId: String): Int = prefs.getInt("${modeId}_lines", 0)
    fun getWins(modeId: String): Int = prefs.getInt("${modeId}_wins", 0)

    fun saveResult(modeId: String, score: Int, level: Int, lines: Int, won: Boolean = false) {
        val editor = prefs.edit()
        if (score > getBestScore(modeId)) editor.putInt("${modeId}_best_score", score)
        if (level > getBestLevel(modeId)) editor.putInt("${modeId}_best_level", level)
        editor.putInt("${modeId}_games", getGamesPlayed(modeId) + 1)
        editor.putInt("${modeId}_lines", getTotalLines(modeId) + lines)
        if (won) editor.putInt("${modeId}_wins", getWins(modeId) + 1)
        editor.apply()

        saveGlobalBest(score, level)
    }

    /** Сброс всей статистики (например, при обновлении). */
    fun resetAllStats() {
        val editor = prefs.edit()
        GameMode.values().forEach { mode ->
            editor.remove("${mode.id}_best_score")
            editor.remove("${mode.id}_best_level")
            editor.remove("${mode.id}_games")
            editor.remove("${mode.id}_lines")
            editor.remove("${mode.id}_wins")
        }
        editor.remove("global_best_score")
        editor.remove("global_best_level")
        editor.apply()
    }

    data class OldRecords(val score: Int, val level: Int)

    /**
     * Возвращает текущие глобальные рекорды для диалога обновления.
     */
    fun getOldRecords(): OldRecords = OldRecords(getGlobalBestScore(), getGlobalBestLevel())
}