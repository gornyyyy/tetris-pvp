package com.example.tetrisandroid.game

import com.example.tetrisandroid.model.Board
import com.example.tetrisandroid.model.PlayerAction

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

    var fallDelay = 500L
        private set

    // --- Lock Delay ---
    private var isLocking = false
    private var lockTimer = 0L
    private val lockDelayMs = 500L

    // Публичный доступ к сетке (для отрисовки в Activity)
    val grid: Array<IntArray> get() = board.grid

    fun start() {
        // В Android стартовый экран — это задача Activity.
        // Здесь просто сообщаем, что готовы к отрисовке.
        onUpdate()
    }

    fun tick() {
        if (!isRunning) return
        val now = System.currentTimeMillis()

        if (isLocking) {
            if (now - lockTimer >= lockDelayMs) {
                lockAndSpawn()
            }
        } else {
            if (now - lastFallTimer >= fallDelay) {
                if (!board.tryMove(currentShape, 0, 1)) {
                    isLocking = true
                    lockTimer = now
                }
                lastFallTimer = now
            }
        }
        onUpdate()
    }

    // Отдельный таймер для гравитации, чтобы tick() можно было вызывать чаще
    private var lastFallTimer = 0L

    fun handleInput(action: PlayerAction) {
        if (!isRunning) return
        var moved = false

        when (action) {
            PlayerAction.MOVE_LEFT  -> moved = board.tryMove(currentShape, -1, 0)
            PlayerAction.MOVE_RIGHT -> moved = board.tryMove(currentShape, 1, 0)
            PlayerAction.MOVE_DOWN  -> {
                if (board.tryMove(currentShape, 0, 1)) {
                    isLocking = false // Сброс lock delay
                    moved = true
                }
            }
            PlayerAction.ROTATE     -> moved = board.tryRotate(currentShape)
            PlayerAction.QUIT       -> { isRunning = false; return }
        }

        if (moved && isLocking) {
            lockTimer = System.currentTimeMillis()
        }
        onUpdate()
    }

    private fun lockAndSpawn() {
        board.lockPiece(currentShape)
        val cleared = board.clearFullLines()

        if (cleared > 0) {
            val points = when (cleared) {
                1 -> 100; 2 -> 300; 3 -> 500; 4 -> 800; else -> 0
            }
            score += points * level
            level = (score / 1000) + 1
            fallDelay = (500L - (level - 1) * 50L).coerceAtLeast(100L)
        }

        currentShape = nextShape
        nextShape = board.spawnShape()
        isLocking = false
        lockTimer = 0L

        if (!board.isValidPosition(currentShape)) {
            isRunning = false
            onGameOver(score)
        }
    }

    fun reset() {
        for (r in 0 until board.grid.size) {
            for (c in 0 until board.grid[0].size) {
                board.grid[r][c] = if (r == board.grid.size - 1) 3 else 0
            }
        }

        currentShape = board.spawnShape()
        nextShape = board.spawnShape()
        score = 0
        level = 1
        isRunning = true
        fallDelay = 500L
        isLocking = false
        lockTimer = 0L
        lastFallTimer = 0L

        onUpdate()
    }
}