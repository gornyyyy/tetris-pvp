package com.example.tetrisandroid.model

data class Point(val x: Int, val y: Int)

abstract class Shape(
    var x: Int, var y: Int
) {
    abstract val colorArgb: Int
    var matrix: Array<IntArray> = emptyArray()

    var rotationIndex: Int = 0
        internal set

    abstract fun rotate()

    fun move(dx: Int, dy: Int) {
        x += dx
        y += dy
    }

    fun moveLeft() = move(-1, 0)
    fun moveRight() = move(1, 0)
    open fun moveDown() = move(0, 1)

    protected fun rotateMatrixClockwise() {
        val rows = matrix.size
        val cols = matrix[0].size
        val rotated = Array(cols) { IntArray(rows) }
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                rotated[c][rows - 1 - r] = matrix[r][c]
            }
        }
        matrix = rotated
    }

    fun getBlocks(): List<Point> {
        val blocks = mutableListOf<Point>()
        for (r in matrix.indices) {
            for (c in matrix[r].indices) {
                if (matrix[r][c] == 1) blocks.add(Point(x + c, y + r))
            }
        }
        return blocks
    }

    open fun getKickOffsets(fromRotation: Int, toRotation: Int): List<Pair<Int, Int>> {

        val table = when (fromRotation to toRotation) {
            0 to 1 -> listOf(0 to 0, -1 to 0, -1 to 1, 0 to -2, -1 to -2)
            1 to 0 -> listOf(0 to 0, 1 to 0, 1 to -1, 0 to 2, 1 to 2)
            1 to 2 -> listOf(0 to 0, 1 to 0, 1 to -1, 0 to 2, 1 to 2)
            2 to 1 -> listOf(0 to 0, -1 to 0, -1 to 1, 0 to -2, -1 to -2)
            2 to 3 -> listOf(0 to 0, 1 to 0, 1 to 1, 0 to -2, 1 to -2)
            3 to 2 -> listOf(0 to 0, -1 to 0, -1 to -1, 0 to 2, -1 to 2)
            3 to 0 -> listOf(0 to 0, -1 to 0, -1 to -1, 0 to 2, -1 to 2)
            0 to 3 -> listOf(0 to 0, 1 to 0, 1 to 1, 0 to -2, 1 to -2)
            else -> listOf(0 to 0)
        }
        return table
    }
}