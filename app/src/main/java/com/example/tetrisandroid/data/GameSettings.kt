package com.example.tetrisandroid.data

import android.content.Context

class GameSettings(context: Context) {
    private val prefs = context.getSharedPreferences("tetris_settings", Context.MODE_PRIVATE)

    var isLightTheme: Boolean
        get() = prefs.getBoolean("light_theme", false)
        set(value) = prefs.edit().putBoolean("light_theme", value).apply()

    var highlightColumn: Boolean
        get() = prefs.getBoolean("highlight_column", false)
        set(value) = prefs.edit().putBoolean("highlight_column", value).apply()
}