package com.example.tetrisandroid.game

import kotlin.math.pow

class GameCompetitive(
    onUpdate: () -> Unit,
    onGameOver: (Int) -> Unit,
    private val onAttack: (lines: Int) -> Unit = {}
) : Game(onUpdate, onGameOver) {

    override val modeName = "Competitive"
    override val modeId = "competitive"

    override fun calculateFallDelay(level: Int): Long {
        val lvl = level.coerceAtLeast(1)
        val millis = 500.0 * 0.766.pow(lvl - 1)
        return millis.toLong().coerceAtLeast(MIN_FALL_DELAY)
    }

    override fun calculateScore(cleared: Int, level: Int): Int {
        val points = when (cleared) {
            1 -> 100; 2 -> 300; 3 -> 500; 4 -> 800; else -> 0
        }

        if (cleared >= 2) {
            onAttack(cleared - 1)
        }
        return points * level
    }
}