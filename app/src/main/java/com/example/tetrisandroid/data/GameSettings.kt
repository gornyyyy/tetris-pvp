package com.example.tetrisandroid.data

import android.content.Context

enum class ThemeType(val id: String, val displayName: String) {
    BASIC("basic", "Basic"),
    CLASSIC("classic", "Classic"),
    MODERN("modern", "Modern")
}

class GameSettings(context: Context) {
    private val prefs = context.getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)

    var isLightTheme: Boolean
        get() = prefs.getBoolean("light_theme", false)
        set(value) = prefs.edit().putBoolean("light_theme", value).apply()

    var highlightColumn: Boolean
        get() = prefs.getBoolean("highlight_column", false)
        set(value) = prefs.edit().putBoolean("highlight_column", value).apply()

    var themeType: ThemeType
        get() = ThemeType.values().firstOrNull { it.id == prefs.getString("theme_type", "basic") } ?: ThemeType.BASIC
        set(value) = prefs.edit().putString("theme_type", value.id).apply()
}