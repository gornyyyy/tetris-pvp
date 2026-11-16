package com.example.tetrisandroid.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PreviewLine(widthFactor: Float, color: Color) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth(widthFactor)
            .height(3.dp)
            .background(color, RoundedCornerShape(1.dp))
    )
}