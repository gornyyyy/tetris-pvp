package com.example.tetrisandroid.model

class ShapeI(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFF00FFFF.toInt()  // Cyan
    init {
        matrix = arrayOf(intArrayOf(1, 1, 1, 1))
    }
    override fun rotate() = rotateMatrixClockwise()
}

class ShapeO(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFFFFFF00.toInt()  // Yellow
    init {
        matrix = arrayOf(intArrayOf(1, 1), intArrayOf(1, 1))
    }
    override fun rotate() { /* квадрат не вращается */ }
}

class ShapeT(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFF800080.toInt()  // Purple
    init {
        matrix = arrayOf(intArrayOf(0, 1, 0), intArrayOf(1, 1, 1))
    }
    override fun rotate() = rotateMatrixClockwise()
}

class ShapeS(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFF00FF00.toInt()  // Green
    init {
        matrix = arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 0))
    }
    override fun rotate() = rotateMatrixClockwise()
}

class ShapeZ(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFFFF0000.toInt()  // Red
    init {
        matrix = arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 1))
    }
    override fun rotate() = rotateMatrixClockwise()
}

class ShapeJ(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFF0000FF.toInt()  // Blue
    init {
        matrix = arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 1, 1))
    }
    override fun rotate() = rotateMatrixClockwise()
}

class ShapeL(x: Int, y: Int) : Shape(x, y) {
    override val colorArgb = 0xFFFFA500.toInt()  // Orange
    init {
        matrix = arrayOf(intArrayOf(0, 0, 1), intArrayOf(1, 1, 1))
    }
    override fun rotate() = rotateMatrixClockwise()
}