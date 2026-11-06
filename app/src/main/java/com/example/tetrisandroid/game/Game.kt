package com.example.tetrisandroid.game

import com.example.tetrisandroid.model.Board
import com.example.tetrisandroid.model.PlayerAction
import kotlin.math.pow

class Game(
    private val onUpdate: () -> Unit,
    private val onGameOver: (Int) -> Unit
) {
    private val board = Board()

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
    private var isLocking = false
    private var lockTimer = 0L
    private val lockDelayMs = 500L
    private var lockResets = 0
    private val maxLockResets = 15

    private var lastFallTimer = 0L
    private var linesClearedTotal = 0

    // Публичный доступ к сетке (для отрисовки)
    val grid: Array<IntArray> get() = board.grid

    init {
        // Сразу делаем стартовую фигуру видимой
        board.revealShape(currentShape)
        board.revealShape(nextShape)
    }

    fun start() {
        onUpdate()
    }

    fun tick() {
        if (!isRunning) return
        val now = System.currentTimeMillis()

        if (isLocking) {
            if (lockResets >= maxLockResets) {
                lockAndSpawn()
                return
            }
            if (now - lockTimer >= lockDelayMs) {
                lockAndSpawn()
            }
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
                    // Фигура упала — сбрасываем lock полностью
                    isLocking = false
                    lockResets = 0
                    moved = true
                }
            }
            PlayerAction.ROTATE     -> moved = board.tryRotate(currentShape)
            PlayerAction.HARD_DROP  -> { hardDrop(); return }
            PlayerAction.QUIT       -> { isRunning = false; return }
        }

        if (moved) {
            val canFall = board.canMove(currentShape, 0, 1)
            if (canFall) {
                // Отменяем lock - фигура падает
                isLocking = false
                lockResets = 0
            } else if (isLocking) {
                lockResets++
                lockTimer = System.currentTimeMillis()
            } else {
                isLocking = true
                lockTimer = System.currentTimeMillis()
                lockResets = 1
            }
        }

        onUpdate()
    }

    private fun hardDrop() {
        var dropped = 0
        while (board.tryMove(currentShape, 0, 1)) {
            dropped++
        }
        score += dropped * 2 // 2 очка за каждую клетку
        lockAndSpawn()
    }

    private fun lockAndSpawn() {
        val overflow = board.lockPiece(currentShape)
        val cleared = board.clearFullLines()
        linesClearedTotal += cleared

        if (cleared > 0) {
            val points = when (cleared) {
                1 -> 100
                2 -> 300
                3 -> 500
                4 -> 800
                else -> 0
            }
            score += points * level
        }

        // Уровень каждые 10 линий
        level = (linesClearedTotal / 10) + 1
        fallDelay = calculateFallDelay(level)

        currentShape = nextShape
        nextShape = board.spawnShape()

        board.revealShape(currentShape)

        if (!board.isValidPosition(currentShape)) {
            isRunning = false
            onGameOver(score)
            return
        }

        isLocking = false
        lockTimer = 0L
        lockResets = 0

        if (overflow || !board.canShapeMoveAnywhere(currentShape)) {
            isRunning = false
            onGameOver(score)
        }

        onUpdate()
    }

    fun reset() {
        // Очищаем поле
        for (r in 0 until board.grid.size) {
            for (c in 0 until board.grid[0].size) {
                board.grid[r][c] = if (r == board.grid.size - 1) 3 else 0
            }
        }

        currentShape = board.spawnShape()
        nextShape = board.spawnShape()
        board.revealShape(currentShape)
        board.revealShape(nextShape)

        score = 0
        level = 1
        isRunning = true
        fallDelay = calculateFallDelay(1)
        isLocking = false
        lockTimer = 0L
        lockResets = 0
        lastFallTimer = 0L
        linesClearedTotal = 0

        onUpdate()
    }

    private fun calculateFallDelay(level: Int): Long {
        val lvl = level.coerceAtLeast(1)
        val millis: Double = when {
            lvl <= 9 -> {
                600.0 - (lvl - 1) * 50.0
            }
            lvl <= 16 -> {
                200.0 * 0.7937.pow(lvl - 10)
            }
            lvl <= 19 -> {
                50.0
            }
            else -> {
                50.0 - (lvl - 20) * (34.0 / 9.0)
            }
        }
        return millis.toLong().coerceAtLeast(MIN_FALL_DELAY)
    }

    companion object {
        private const val MIN_FALL_DELAY = 16L
    }
}