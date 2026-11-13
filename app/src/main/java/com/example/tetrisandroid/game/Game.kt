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

    // --- Lock Delay ---
    protected var isLocking = false
    protected var lockTimer = 0L
    protected val lockDelayMs = 500L
    protected var lockResets = 0
    protected val maxLockResets = 15

    protected var lastFallTimer = 0L
    var linesClearedTotal = 0
        private set

    val grid: Array<IntArray> get() = board.grid

    /** Отображаемое имя режима (для UI) */
    abstract val modeName: String
    abstract val modeId: String

    /** Формула задержки падения в зависимости от уровня */
    protected abstract fun calculateFallDelay(level: Int): Long

    /** Начисление очков за очищенные линии */
    protected open fun calculateScore(cleared: Int, level: Int): Int {
        val points = when (cleared) {
            1 -> 100; 2 -> 300; 3 -> 500; 4 -> 800; else -> 0
        }
        return points * level
    }

    /** Условие завершения игры помимо проигрыша (для GameWithFinish) */
    protected open fun checkExtraGameOver(): Boolean = false

    init {
        board.revealShape(currentShape)
        board.revealShape(nextShape)
    }

    fun start() = onUpdate()

    fun tick() {
        if (!isRunning) return
        val now = System.currentTimeMillis()

        if (isLocking) {
            if (lockResets >= maxLockResets) { lockAndSpawn(); return }
            if (now - lockTimer >= lockDelayMs) lockAndSpawn()
        } else {
            if (now - lastFallTimer >= fallDelay) {
                if (!board.tryMove(currentShape, 0, 1)) {
                    isLocking = true
                    lockTimer = now
                    lockResets = 0
                }
                lastFallTimer = now
            }
        }
        onUpdate()
    }

    fun handleInput(action: PlayerAction) {
        if (!isRunning) return
        var moved = false

        when (action) {
            PlayerAction.MOVE_LEFT  -> moved = board.tryMove(currentShape, -1, 0)
            PlayerAction.MOVE_RIGHT -> moved = board.tryMove(currentShape, 1, 0)
            PlayerAction.MOVE_DOWN  -> {
                if (board.tryMove(currentShape, 0, 1)) {
                    isLocking = false; lockResets = 0; moved = true
                } else {
                    lockAndSpawn(); onUpdate(); return
                }
            }
            PlayerAction.ROTATE     -> moved = board.tryRotate(currentShape)
            PlayerAction.HARD_DROP  -> { hardDrop(); return }
            PlayerAction.QUIT       -> { isRunning = false; return }
        }

        if (moved) {
            val canFall = board.canMove(currentShape, 0, 1)
            if (canFall) {
                isLocking = false; lockResets = 0
            } else if (isLocking) {
                lockResets++; lockTimer = System.currentTimeMillis()
            } else {
                isLocking = true; lockTimer = System.currentTimeMillis(); lockResets = 1
            }
        }
        onUpdate()
    }

    private fun hardDrop() {
        var dropped = 0
        while (board.tryMove(currentShape, 0, 1)) dropped++
        score += dropped * 2
        lockAndSpawn()
    }

    private fun lockAndSpawn() {
        val overflow = board.lockPiece(currentShape)
        val cleared = board.clearFullLines()
        linesClearedTotal += cleared

        if (cleared > 0) {
            score += calculateScore(cleared, level)
        }

        level = (linesClearedTotal / 10) + 1
        fallDelay = calculateFallDelay(level)

        currentShape = nextShape
        nextShape = board.spawnShape()
        board.revealShape(currentShape)

        if (!board.isValidPosition(currentShape)) {
            isRunning = false; onGameOver(score); return
        }

        isLocking = false; lockTimer = 0L; lockResets = 0

        if (overflow || !board.canShapeMoveAnywhere(currentShape) || checkExtraGameOver()) {
            isRunning = false; onGameOver(score)
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
        onUpdate()
    }

    companion object {
        protected const val MIN_FALL_DELAY = 16L
    }
}