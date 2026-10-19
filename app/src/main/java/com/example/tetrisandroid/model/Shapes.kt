package com.example.tetrisandroid.model

class ShapeI(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFF00FFFF.toInt()
    init {
        matrix = arrayOf(
            intArrayOf(0, 0, 0, 0),
            intArrayOf(1, 1, 1, 1),
            intArrayOf(0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0)
        )
    }
    override fun rotate() {
        rotateMatrixClockwise()
        rotationIndex = (rotationIndex + 1) % 4
    }
    override fun getKickOffsets(fromRotation: Int, toRotation: Int): List<Pair<Int, Int>> {
        return when (fromRotation to toRotation) {
            0 to 1 -> listOf(0 to 0, -2 to 0, 1 to 0, -2 to -1, 1 to 2)
            1 to 0 -> listOf(0 to 0, 2 to 0, -1 to 0, 2 to 1, -1 to -2)
            1 to 2 -> listOf(0 to 0, -1 to 0, 2 to 0, -1 to 2, 2 to -1)
            2 to 1 -> listOf(0 to 0, 1 to 0, -2 to 0, 1 to -2, -2 to 1)
            2 to 3 -> listOf(0 to 0, 2 to 0, -1 to 0, 2 to 1, -1 to -2)
            3 to 2 -> listOf(0 to 0, -2 to 0, 1 to 0, -2 to -1, 1 to 2)
            3 to 0 -> listOf(0 to 0, 1 to 0, -2 to 0, 1 to -2, -2 to 1)
            0 to 3 -> listOf(0 to 0, -1 to 0, 2 to 0, -1 to 2, 2 to -1)
            else -> listOf(0 to 0)
        }
    }
}

class ShapeO(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFFFFFF00.toInt()
    init {
        matrix = arrayOf(intArrayOf(1, 1), intArrayOf(1, 1))
    }
    override fun rotate() {
        rotationIndex = (rotationIndex + 1) % 4
    }
}

class ShapeT(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFF800080.toInt()
    init {
        matrix = arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 0)
        )
    }
    override fun rotate() {
        rotateMatrixClockwise()
        rotationIndex = (rotationIndex + 1) % 4
    }
}

class ShapeS(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFF00FF00.toInt()
    init {
        matrix = arrayOf(
            intArrayOf(0, 1, 1),
            intArrayOf(1, 1, 0),
            intArrayOf(0, 0, 0)
        )
    }
    override fun rotate() {
        rotateMatrixClockwise()
        rotationIndex = (rotationIndex + 1) % 4
    }
}

class ShapeZ(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFFFF0000.toInt()
    init {
        matrix = arrayOf(
            intArrayOf(1, 1, 0),
            intArrayOf(0, 1, 1),
            intArrayOf(0, 0, 0)
        )
    }
    override fun rotate() {
        rotateMatrixClockwise()
        rotationIndex = (rotationIndex + 1) % 4
    }
}

class ShapeJ(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFF0000FF.toInt()
    init {
        matrix = arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 0)
        )
    }
    override fun rotate() {
        rotateMatrixClockwise()
        rotationIndex = (rotationIndex + 1) % 4
    }
}

class ShapeL(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFFFFA500.toInt()
    init {
        matrix = arrayOf(
            intArrayOf(0, 0, 1),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 0)
        )
    }
    override fun rotate() {
        rotateMatrixClockwise()
        rotationIndex = (rotationIndex + 1) % 4
    }
}