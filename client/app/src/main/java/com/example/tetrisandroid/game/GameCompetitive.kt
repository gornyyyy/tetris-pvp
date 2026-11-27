package com.example.tetrisandroid.game

import kotlin.math.pow

class GameCompetitive(
    onUpdate: () -> Unit,
    onGameOver: (Int) -> Unit,
    private val onAttack: (lines: Int) -> Unit = {},
    private val onLineClearEvent: (lines: Int) -> Unit = {},
    private val onScoreChanged: (score: Int, level: Int, lines: Int) -> Unit = { _, _, _ -> },
    private val onGameOverEvent: () -> Unit = {},
    private val onWinEvent: () -> Unit = {},
    onLineClear: (Int) -> Unit = {}
) : Game(onUpdate, onGameOver, onLineClear) {

    override val modeName = "Competitive"
    override val modeId = "competitive"

    var enableGarbage: Boolean = true

    private var lastReportedScore = -1
    private var lastReportedLevel = -1
    private var lastReportedLines = -1

    private var queuedGarbage = 0

    override fun calculateFallDelay(level: Int): Long {
        val lvl = level.coerceAtLeast(1)
        val millis = 500.0 * 0.766.pow(lvl - 1)
        return millis.toLong().coerceAtLeast(MIN_FALL_DELAY)
    }

    override fun calculateScore(cleared: Int, level: Int): Int {
        val points = when (cleared) {
            1 -> 100; 2 -> 300; 3 -> 500; 4 -> 800; else -> 0
        }

        if (enableGarbage && cleared >= 2) {
            onAttack(cleared - 1)
            onLineClearEvent(cleared)
        }
        return points * level
    }

    override fun onAfterLockOrClear() {
        if (queuedGarbage > 0) {
            startGarbageAnimation(queuedGarbage)
            queuedGarbage = 0
        }
        reportIfChanged()
    }

    fun reportIfChanged() {
        if (score != lastReportedScore ||
            level != lastReportedLevel ||
            linesClearedTotal != lastReportedLines
        ) {
            lastReportedScore = score
            lastReportedLevel = level
            lastReportedLines = linesClearedTotal
            onScoreChanged(score, level, linesClearedTotal)
        }
    }

    fun receiveGarbage(lines: Int) {
        if (!isRunning || lines <= 0) return
        queuedGarbage += lines
    }

    fun notifyGameOver() {
        onGameOverEvent()
    }

    fun notifyWin() {
        onWinEvent()
    }
}