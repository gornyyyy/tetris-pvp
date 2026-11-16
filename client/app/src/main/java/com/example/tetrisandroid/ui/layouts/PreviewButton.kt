package com.example.tetrisandroid.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PreviewButton(
    btnWidth: Float,
    btnHeight: Float,
    scaleFactor: Float
) {
    val w = (btnWidth * scaleFactor).dp
    val h = (btnHeight * scaleFactor).dp
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(w, h)
            .background(Color(0xFF9AA8B8).copy(alpha = 0.6f), RoundedCornerShape(2.dp))
    )
}