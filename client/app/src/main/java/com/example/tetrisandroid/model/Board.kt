package com.example.tetrisandroid.model

class Board {
    companion object {
        const val WIDTH = 10
        const val VISIBLE_HEIGHT = 20
        const val HIDDEN_ROWS = 4
        const val TOTAL_HEIGHT = VISIBLE_HEIGHT + HIDDEN_ROWS // 24
    }

    val grid: Array<IntArray> = Array(TOTAL_HEIGHT) { IntArray(WIDTH) }
    // Хранит variantIndex (0..2) для Classic ИЛИ colorArgb для Modern
    val variantGrid: Array<IntArray> = Array(TOTAL_HEIGHT) { IntArray(WIDTH) }

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

    /**
     * @param saveColorAsArgb true — сохраняем colorArgb (Modern), false — сохраняем variantIndex (Classic)
     */
    fun lockPiece(shape: Shape, saveColorAsArgb: Boolean): Boolean {
        var overflow = false
        for (block in shape.getBlocks()) {
            if (block.y >= 0 && block.y < TOTAL_HEIGHT && block.x >= 0 && block.x < WIDTH) {
                grid[block.y][block.x] = 2
                variantGrid[block.y][block.x] = if (saveColorAsArgb) {
                    shape.colorArgb
                } else {
                    shape.variantIndex
                }
                if (block.y < HIDDEN_ROWS) overflow = true
            }
        }
        return overflow
    }

    fun findFullLines(): List<Int> {
        val result = mutableListOf<Int>()
        for (r in 0 until TOTAL_HEIGHT - 1) {
            if (grid[r].all { it != 0 }) result.add(r)
        }
        return result
    }

    fun spawnShape(): Shape {
        val shape = when ((0..6).random()) {
            0 -> ShapeI(3, 0)
            1 -> ShapeO(4, 0)
            2 -> ShapeT(3, 0)
            3 -> ShapeS(3, 0)
            4 -> ShapeZ(3, 0)
            5 -> ShapeJ(3, 0)
            else -> ShapeL(3, 0)
        }
        shape.variantIndex = when (shape) {
            is ShapeJ, is ShapeZ -> 0
            is ShapeL, is ShapeS -> 1
            else -> 2
        }
        return shape
    }

    fun revealShape(shape: Shape) {
        if (!isValidPosition(shape)) return
        val blocks = shape.getBlocks()
        if (blocks.isEmpty()) return
        val lowestY = blocks.maxOf { it.y }
        val shift = HIDDEN_ROWS - lowestY
        if (shift <= 0) return
        val originalY = shape.y
        shape.y = originalY + shift
        if (isValidPosition(shape)) return
        shape.y = originalY
    }

    fun canShapeMoveAnywhere(shape: Shape): Boolean {
        return canMove(shape, 0, 1) ||
                canMove(shape, -1, 0) ||
                canMove(shape, 1, 0)
    }

    fun clearFullLines(): Int {
        val remainingRows = mutableListOf<IntArray>()
        val remainingVariants = mutableListOf<IntArray>()
        var cleared = 0

        for (r in 0 until TOTAL_HEIGHT - 1) {
            val isFull = grid[r].all { it != 0 }
            if (isFull) {
                cleared++
            } else {
                remainingRows.add(grid[r].clone())
                remainingVariants.add(variantGrid[r].clone())
            }
        }

        if (cleared == 0) return 0

        val emptyCount = TOTAL_HEIGHT - 1 - remainingRows.size
        for (r in 0 until emptyCount) {
            grid[r] = IntArray(WIDTH)
            variantGrid[r] = IntArray(WIDTH)
        }
        for (i in remainingRows.indices) {
            grid[emptyCount + i] = remainingRows[i]
            variantGrid[emptyCount + i] = remainingVariants[i]
        }

        return cleared
    }

    fun addGarbageLine() {
        for (r in 0 until grid.size - 1) {
            grid[r] = grid[r + 1].copyOf()
            variantGrid[r] = variantGrid[r + 1].copyOf()
        }
        val hole = (0 until grid[0].size).random()
        for (c in grid[0].indices) {
            grid[grid.size - 1][c] = if (c == hole) 0 else 2
            variantGrid[grid.size - 1][c] = 0
        }
    }
}