package com.example.tetrisandroid.game

import kotlin.math.pow

class GameWithFinish(
    onUpdate: () -> Unit,
    onGameOver: (Int) -> Unit,
    onLineClear: (Int) -> Unit = {},
    private val targetLines: Int = 160
) : Game(onUpdate, onGameOver, onLineClear) {

    override val modeName = "Finish"
    override val modeId = "finish"

    var isWon = false
        private set


    override fun calculateFallDelay(level: Int): Long {
        val lvl = level.coerceAtLeast(1)
        val millis = 500.0 * 0.766.pow(lvl - 1)
        return millis.toLong().coerceAtLeast(MIN_FALL_DELAY)
    }

    override fun checkExtraGameOver(): Boolean {
        if (linesClearedTotal >= targetLines) {
            isWon = true
            return true
        }
        return false
    }

    val linesRemaining: Int get() = (targetLines - linesClearedTotal).coerceAtLeast(0)
    val targetLinesCount: Int get() = targetLines
}