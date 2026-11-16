package com.example.tetrisandroid.ui.layout

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PreviewFieldGrid() {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            repeat(9) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(0.3.dp, Color(0xFFB0B8C4).copy(alpha = 0.4f))
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight())
        }
    }
}