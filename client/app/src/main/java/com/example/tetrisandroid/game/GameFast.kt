package com.example.tetrisandroid.game

import kotlin.math.pow

class GameFast(
    onUpdate: () -> Unit,
    onGameOver: (Int) -> Unit,
    onLineClear: (Int) -> Unit = {}
) : Game(onUpdate, onGameOver, onLineClear) {

    override val modeName = "Fast"
    override val modeId = "fast"

    override fun calculateFallDelay(level: Int): Long {
        val lvl = level.coerceAtLeast(1)
        val millis = 150.0 * 0.7782.pow(lvl - 1)
        return millis.toLong().coerceAtLeast(MIN_FALL_DELAY)
    }

}