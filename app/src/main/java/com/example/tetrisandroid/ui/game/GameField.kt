package com.example.tetrisandroid.ui.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tetrisandroid.data.ThemeType
import com.example.tetrisandroid.ui.components.TetrisBoardCanvas

@Composable
fun GameField(
    state: UiState,
    variantGrid: Array<IntArray>,
    currentVariantIndex: Int,
    highlightColumn: Boolean,
    isLightTheme: Boolean,
    themeType: ThemeType,
    keepFallenColor: Boolean
) {
    TetrisBoardCanvas(
        grid = state.grid,
        variantGrid = variantGrid,
        activeBlocks = if (state.clearingLines.isEmpty()) state.activeBlocks else emptyList(),
        activeColorArgb = state.activeColorArgb,
        activeVariantIndex = currentVariantIndex,
        highlightColumn = highlightColumn,
        isLightTheme = isLightTheme,
        themeType = themeType,
        level = state.level,
        clearingLines = state.clearingLines,
        keepFallenColor = keepFallenColor,
        clearProgress = state.clearProgress,
        modifier = Modifier.fillMaxSize()
    )
}