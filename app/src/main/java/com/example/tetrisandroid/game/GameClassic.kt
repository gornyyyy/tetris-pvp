package com.example.tetrisandroid.game

import kotlin.math.pow

class GameClassic(
    onUpdate: () -> Unit,
    onGameOver: (Int) -> Unit
) : Game(onUpdate, onGameOver) {

    override val modeName = "Classic"
    override val modeId = "classic"

    override fun calculateFallDelay(level: Int): Long {
        val lvl = level.coerceAtLeast(1)
        val millis: Double = when {
            lvl <= 9 -> 600.0 - (lvl - 1) * 50.0
            lvl <= 16 -> 200.0 * 0.7937.pow(lvl - 10)
            lvl <= 19 -> 50.0
            else -> 50.0 - (lvl - 20) * (34.0 / 9.0)
        }
        return millis.toLong().coerceAtLeast(MIN_FALL_DELAY)
    }

    // Очки по умолчанию (как в базовом классе)
}