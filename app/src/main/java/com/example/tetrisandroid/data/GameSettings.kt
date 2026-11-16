package com.example.tetrisandroid.data

import android.content.Context
import com.example.tetrisandroid.game.GameMode

enum class ThemeType(val id: String, val displayName: String) {
    BASIC("basic", "Basic"),
    CLASSIC("classic", "Classic"),
    MODERN("modern", "Modern")
}

class GameSettings(context: Context) {
    private val prefs = context.getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)

    var lastGameMode: GameMode
        get() = GameMode.values().firstOrNull { it.id == prefs.getString("last_game_mode", "classic") } ?: GameMode.CLASSIC
        set(value) = prefs.edit().putString("last_game_mode", value.id).apply()

    var isLightTheme: Boolean
        get() = prefs.getBoolean("light_theme", false)
        set(value) = prefs.edit().putBoolean("light_theme", value).apply()

    var highlightColumn: Boolean
        get() = prefs.getBoolean("highlight_column", false)
        set(value) = prefs.edit().putBoolean("highlight_column", value).apply()

    var themeType: ThemeType
        get() = ThemeType.values().firstOrNull { it.id == prefs.getString("theme_type", "modern") } ?: ThemeType.BASIC
        set(value) = prefs.edit().putString("theme_type", value.id).apply()

    var keepFallenColor: Boolean
        get() = prefs.getBoolean("keep_fallen_color", true)
        set(value) = prefs.edit().putBoolean("keep_fallen_color", value).apply()
}