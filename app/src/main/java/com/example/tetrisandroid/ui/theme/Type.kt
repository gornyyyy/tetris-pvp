package com.example.tetrisandroid.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.tetrisandroid.R

val MbaSliceMonoFamily = FontFamily(
    Font(R.font.minecraft, FontWeight.Normal)
)

val FallbackFamily = FontFamily.Default
val AppFontFamily = MbaSliceMonoFamily

private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = AppFontFamily, fontSize = base.displayLarge.fontSize * 0.9f),
    displayMedium = base.displayMedium.copy(fontFamily = AppFontFamily, fontSize = base.displayMedium.fontSize * 0.9f),
    displaySmall = base.displaySmall.copy(fontFamily = AppFontFamily, fontSize = base.displaySmall.fontSize * 0.9f),

    headlineLarge = base.headlineLarge.copy(fontFamily = AppFontFamily, fontSize = base.headlineLarge.fontSize * 0.9f),
    headlineMedium = base.headlineMedium.copy(fontFamily = AppFontFamily, fontSize = base.headlineMedium.fontSize * 0.9f),
    headlineSmall = base.headlineSmall.copy(fontFamily = AppFontFamily, fontSize = base.headlineSmall.fontSize * 0.9f),

    titleLarge = base.titleLarge.copy(fontFamily = AppFontFamily, fontSize = base.titleLarge.fontSize * 0.9f),
    titleMedium = base.titleMedium.copy(fontFamily = AppFontFamily, fontSize = base.titleMedium.fontSize * 0.9f),
    titleSmall = base.titleSmall.copy(fontFamily = AppFontFamily, fontSize = base.titleSmall.fontSize * 0.9f),

    bodyLarge = base.bodyLarge.copy(fontFamily = AppFontFamily, fontSize = base.bodyLarge.fontSize * 0.9f),
    bodyMedium = base.bodyMedium.copy(fontFamily = AppFontFamily, fontSize = base.bodyMedium.fontSize * 0.9f),
    bodySmall = base.bodySmall.copy(fontFamily = AppFontFamily, fontSize = base.bodySmall.fontSize * 0.9f),

    labelLarge = base.labelLarge.copy(fontFamily = AppFontFamily, fontSize = base.labelLarge.fontSize * 0.9f),
    labelMedium = base.labelMedium.copy(fontFamily = AppFontFamily, fontSize = base.labelMedium.fontSize * 0.9f),
    labelSmall = base.labelSmall.copy(fontFamily = AppFontFamily, fontSize = base.labelSmall.fontSize * 0.9f)
)