package com.example.tetrisandroid.game

import com.example.tetrisandroid.model.Board
import com.example.tetrisandroid.model.PlayerAction

abstract class Game(
    private val onUpdate: () -> Unit,
    private val onGameOver: (Int) -> Unit
) {
    protected val board = Board()

    var currentShape = board.spawnShape()
        private set
    var nextShape = board.spawnShape()
        private set

    var score = 0
        private set
    var level = 1
        private set

    @Volatile var isRunning = true
        private set

    var fallDelay = calculateFallDelay(1)
        private set

    // --- Lock Delay (динамический) ---
    protected var isLocking = false
    protected var lockTimer = 0L
    protected val lockDelayMs = 500L
    protected var lockResets = 0
    protected val maxLockResets = 15

    protected var lastFallTimer = 0L
    var linesClearedTotal = 0
        private set

    // --- Анимация очистки ---
    var clearingLines: List<Int> = emptyList()
        private set
    var clearProgress: Float = 0f
        private set
    var isClearing: Boolean = false
        private set

    private var clearStartTime = 0L
    private val clearAnimDurationMs = 300L

    val grid: Array<IntArray> get() = board.grid

    abstract val modeName: String
    abstract val modeId: String

    protected abstract fun calculateFallDelay(level: Int): Long

    protected open fun calculateScore(cleared: Int, level: Int): Int {
        val points = when (cleared) {
            1 -> 100; 2 -> 300; 3 -> 500; 4 -> 800; else -> 0
        }
        return points * level
    }

    protected open fun checkExtraGameOver(): Boolean = false

    init {
        board.revealShape(currentShape)
        board.revealShape(nextShape)
    }

    fun start() = onUpdate()

    fun tick() {
        if (!isRunning) return

        if (isClearing) {
            val elapsed = System.currentTimeMillis() - clearStartTime
            clearProgress = (elapsed.toFloat() / clearAnimDurationMs).coerceIn(0f, 1f)
            if (clearProgress >= 1f) finishClearing()
            onUpdate()
            return
        }

        val now = System.currentTimeMillis()

        if (isLocking) {
            if (lockResets >= maxLockResets) { lockAndCheck(); return }
            if (now - lockTimer >= lockDelayMs) lockAndCheck()
        } else {
            if (now - lastFallTimer >= fallDelay) {
                if (!board.tryMove(currentShape, 0, 1)) {
                    lockTimer = now
                    if (lockResets == 0) lockResets = 1
                    isLocking = true
                }
                lastFallTimer = now
            }
        }
        onUpdate()
    }

    fun handleInput(action: PlayerAction) {
        if (!isRunning || isClearing) return
        var moved = false

        when (action) {
            PlayerAction.MOVE_LEFT  -> moved = board.tryMove(currentShape, -1, 0)
            PlayerAction.MOVE_RIGHT -> moved = board.tryMove(currentShape, 1, 0)
            PlayerAction.MOVE_DOWN  -> {
                if (board.tryMove(currentShape, 0, 1)) {
                    // Фигура упала вниз — сбрасываем счётчик, потому что она «спустилась»
                    isLocking = false
                    lockResets = 0
                    moved = true
                } else {
                    lockAndCheck(); onUpdate(); return
                }
            }
            PlayerAction.ROTATE     -> moved = board.tryRotate(currentShape)
            PlayerAction.HARD_DROP  -> { hardDrop(); return }
            PlayerAction.QUIT       -> { isRunning = false; return }
        }

        if (moved) {
            val canFall = board.canMove(currentShape, 0, 1)
            if (canFall) {
                isLocking = false
            } else if (isLocking) {
                lockResets++
                lockTimer = System.currentTimeMillis()
            } else {
                // Только что коснулась дна
                isLocking = true
                lockTimer = System.currentTimeMillis()
                lockResets = 1
            }
        }
        onUpdate()
    }

    private fun hardDrop() {
        var dropped = 0
        while (board.tryMove(currentShape, 0, 1)) dropped++
        score += dropped * 2
        lockAndCheck()
    }

    private fun lockAndCheck() {
        val overflow = board.lockPiece(currentShape)
        val fullLines = board.findFullLines()

        isLocking = false
        lockTimer = 0L
        lockResets = 0

        if (overflow) {
            isRunning = false
            onGameOver(score)
            onUpdate()
            return
        }

        if (fullLines.isNotEmpty()) {
            clearingLines = fullLines
            isClearing = true
            clearProgress = 0f
            clearStartTime = System.currentTimeMillis()
            onUpdate()
        } else {
            spawnAfterLock()
        }
    }

    private fun finishClearing() {
        val cleared = board.clearFullLines()
        linesClearedTotal += cleared

        if (cleared > 0) {
            score += calculateScore(cleared, level)
        }

        level = (linesClearedTotal / 10) + 1
        fallDelay = calculateFallDelay(level)

        isClearing = false
        clearProgress = 0f
        clearingLines = emptyList()

        spawnAfterLock()
    }

    private fun spawnAfterLock() {
        currentShape = nextShape
        nextShape = board.spawnShape()
        board.revealShape(currentShape)

        if (!board.isValidPosition(currentShape)) {
            isRunning = false
            onGameOver(score)
            onUpdate()
            return
        }

        if (!board.canShapeMoveAnywhere(currentShape) || checkExtraGameOver()) {
            isRunning = false
            onGameOver(score)
        }
        onUpdate()
    }

    fun reset() {
        for (r in 0 until board.grid.size) {
            for (c in 0 until board.grid[0].size) {
                board.grid[r][c] = if (r == board.grid.size - 1) 3 else 0
            }
        }
        currentShape = board.spawnShape()
        nextShape = board.spawnShape()
        board.revealShape(currentShape)
        board.revealShape(nextShape)

        score = 0; level = 1; isRunning = true
        fallDelay = calculateFallDelay(1)
        isLocking = false; lockTimer = 0L; lockResets = 0
        lastFallTimer = 0L; linesClearedTotal = 0
        isClearing = false; clearProgress = 0f; clearingLines = emptyList()
        onUpdate()
    }

    companion object {
        protected const val MIN_FALL_DELAY = 16L
    }
}