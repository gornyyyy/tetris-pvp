package com.example.tetrisandroid.data

import android.content.Context

class LayoutSettings(context: Context) {
    private val prefs = context.getSharedPreferences("tetris_layout", Context.MODE_PRIVATE)

    // Пропорции верхней / нижней части
    var topWeight: Float
        get() = prefs.getFloat("top_weight", 0.75f)
        set(value) = prefs.edit().putFloat("top_weight", value).apply()

    var bottomWeight: Float
        get() = prefs.getFloat("bottom_weight", 0.25f)
        set(value) = prefs.edit().putFloat("bottom_weight", value).apply()

    // Поле / панель
    var fieldWeight: Float
        get() = prefs.getFloat("field_weight", 0.7f)
        set(value) = prefs.edit().putFloat("field_weight", value).apply()

    var panelWeight: Float
        get() = prefs.getFloat("panel_weight", 0.3f)
        set(value) = prefs.edit().putFloat("panel_weight", value).apply()

    // Размеры кнопок
    var actionButtonWidth: Float
        get() = prefs.getFloat("action_btn_width", 84f)
        set(value) = prefs.edit().putFloat("action_btn_width", value).apply()

    var actionButtonHeight: Float
        get() = prefs.getFloat("action_btn_height", 72f)
        set(value) = prefs.edit().putFloat("action_btn_height", value).apply()

    // Горизонтальный отступ
    var horizontalPadding: Float
        get() = prefs.getFloat("h_padding", 12f)
        set(value) = prefs.edit().putFloat("h_padding", value).apply()

    fun resetToDefaults() {
        prefs.edit()
            .putFloat("top_weight", 0.75f)
            .putFloat("bottom_weight", 0.25f)
            .putFloat("field_weight", 0.7f)
            .putFloat("panel_weight", 0.3f)
            .putFloat("action_btn_width", 84f)
            .putFloat("action_btn_height", 72f)
            .putFloat("h_padding", 12f)
            .apply()
    }
}