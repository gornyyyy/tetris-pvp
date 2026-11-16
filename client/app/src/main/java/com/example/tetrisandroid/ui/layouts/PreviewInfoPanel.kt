package com.example.tetrisandroid.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PreviewInfoPanel(
    txt: Color,
    muted: Color,
    fieldBg: Color,
    borderColor: Color,
    btnBg: Color
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        PreviewLine(widthFactor = 0.6f, color = muted.copy(alpha = 0.5f))
        PreviewLine(widthFactor = 0.9f, color = muted.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(2.dp))
        PreviewLine(widthFactor = 0.5f, color = muted.copy(alpha = 0.5f))
        PreviewLine(widthFactor = 0.8f, color = muted.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(2.dp))
        PreviewLine(widthFactor = 0.5f, color = muted.copy(alpha = 0.5f))
        PreviewLine(widthFactor = 0.4f, color = muted.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(2.dp))
        PreviewLine(widthFactor = 0.4f, color = muted.copy(alpha = 0.5f))
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(fieldBg, RoundedCornerShape(2.dp))
                .border(1.dp, borderColor, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(btnBg, RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("II", color = txt, style = MaterialTheme.typography.labelSmall)
        }
    }
}