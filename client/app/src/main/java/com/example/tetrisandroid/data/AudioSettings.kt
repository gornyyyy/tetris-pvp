package com.example.tetrisandroid.data

import android.content.Context

class AudioSettings(context: Context) {
    private val prefs = context.getSharedPreferences("tetris_audio", Context.MODE_PRIVATE)

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean("music_enabled", true)
        set(value) = prefs.edit().putBoolean("music_enabled", value).apply()

    var masterVolume: Float
        get() = prefs.getFloat("master_volume", 1.0f)
        set(value) = prefs.edit().putFloat("master_volume", value).apply()

    var sfxVolume: Float
        get() = prefs.getFloat("sfx_volume", 1.0f)
        set(value) = prefs.edit().putFloat("sfx_volume", value).apply()

    var musicVolume: Float
        get() = prefs.getFloat("music_volume", 0.3f)
        set(value) = prefs.edit().putFloat("music_volume", value).apply()
}