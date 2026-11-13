package com.example.tetrisandroid.game

enum class GameMode(val id: String, val displayName: String) {
    CLASSIC("classic", "Classic"),
    FAST("fast", "Fast"),
    FINISH("finish", "Finish"),
    COMPETITIVE("competitive", "Competitive")
}