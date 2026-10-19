package com.example.tetrisandroid.data

import android.content.Context

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

    companion object {
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_BEST_LEVEL = "best_level"
    }
}