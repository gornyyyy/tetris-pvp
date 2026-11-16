package com.example.tetrisandroid.ui.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreviewControls(
    btnWidth: Float,
    btnHeight: Float,
    scaleFactor: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PreviewButton(btnWidth, btnHeight, scaleFactor)
                PreviewButton(btnWidth, btnHeight, scaleFactor)
            }
            PreviewButton(btnWidth, btnHeight, scaleFactor)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PreviewButton(btnWidth, btnHeight, scaleFactor)
            PreviewButton(btnWidth, btnHeight, scaleFactor)
        }
    }
}