package com.example.tetrisandroid.model

class Board {
    companion object {
        const val WIDTH = 10
        const val VISIBLE_HEIGHT = 20
        const val HIDDEN_ROWS = 4
        const val TOTAL_HEIGHT = VISIBLE_HEIGHT + HIDDEN_ROWS // 24
    }

    val grid: Array<IntArray> = Array(TOTAL_HEIGHT) { IntArray(WIDTH) }

    init {
        for (c in 0 until WIDTH) {
            grid[TOTAL_HEIGHT - 1][c] = 3
        }
    }

    fun isValidPosition(shape: Shape): Boolean {
        for (block in shape.getBlocks()) {
            if (block.x < 0 || block.x >= WIDTH) return false
            if (block.y >= TOTAL_HEIGHT) return false
            if (block.y >= 0 && grid[block.y][block.x] != 0) return false
        }
        return true
    }

    fun tryMove(shape: Shape, dx: Int, dy: Int): Boolean {
        shape.move(dx, dy)
        if (isValidPosition(shape)) return true
        shape.move(-dx, -dy)
        return false
    }

    fun canMove(shape: Shape, dx: Int, dy: Int): Boolean {
        val oldX = shape.x
        val oldY = shape.y
        shape.x += dx
        shape.y += dy
        val valid = isValidPosition(shape)
        shape.x = oldX
        shape.y = oldY
        return valid
    }

    fun tryRotate(shape: Shape): Boolean {
        val fromRotation = shape.rotationIndex
        val toRotation = (fromRotation + 1) % 4

        val backupMatrix = Array(shape.matrix.size) { r -> shape.matrix[r].clone() }
        val backupX = shape.x
        val backupY = shape.y
        val backupRotation = shape.rotationIndex

        shape.rotate()

        for ((dx, dy) in shape.getKickOffsets(fromRotation, toRotation)) {
            shape.x = backupX + dx
            shape.y = backupY + dy
            if (isValidPosition(shape)) return true
        }

        shape.matrix = backupMatrix
        shape.x = backupX
        shape.y = backupY
        shape.rotationIndex = backupRotation
        return false
    }

    fun lockPiece(shape: Shape): Boolean {
        var overflow = false
        for (block in shape.getBlocks()) {
            if (block.y >= 0 && block.y < TOTAL_HEIGHT && block.x >= 0 && block.x < WIDTH) {
                grid[block.y][block.x] = 2
                if (block.y < HIDDEN_ROWS) overflow = true
            }
        }
        return overflow
    }

    fun spawnShape(): Shape {
        return when ((0..6).random()) {
            0 -> ShapeI(3, 0)
            1 -> ShapeO(4, 0)
            2 -> ShapeT(3, 0)
            3 -> ShapeS(3, 0)
            4 -> ShapeZ(3, 0)
            5 -> ShapeJ(3, 0)
            else -> ShapeL(3, 0)
        }
    }

    fun revealShape(shape: Shape) {
        val matrixHeight = shape.matrix.size
        val targetY = HIDDEN_ROWS - matrixHeight + 1
        if (shape.y < targetY) {
            shape.y = targetY
        }
    }

    fun clearFullLines(): Int {
        val remainingRows = mutableListOf<IntArray>()
        var cleared = 0

        for (r in 0 until TOTAL_HEIGHT - 1) {
            val isFull = grid[r].all { it != 0 }
            if (isFull) {
                cleared++
            } else {
                remainingRows.add(grid[r].clone())
            }
        }

        if (cleared == 0) return 0

        val emptyCount = TOTAL_HEIGHT - 1 - remainingRows.size
        for (r in 0 until emptyCount) {
            grid[r] = IntArray(WIDTH)
        }
        for (i in remainingRows.indices) {
            grid[emptyCount + i] = remainingRows[i]
        }

        return cleared
    }
}