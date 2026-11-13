package com.example.tetrisandroid.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tetrisandroid.ui.formatScore

@Composable
fun GameOverDialog(
    score: Int,
    bestScore: Int,
    level: Int,
    bestLevel: Int,
    isLightTheme: Boolean,
    onRestart: () -> Unit,
    onSettings: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White
    val muted = if (isLightTheme) Color(0xFF5A6B7B) else Color(0xFF778DA9)

    Dialog(onDismissRequest = { }) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("GAME OVER", color = txt, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Счёт: ${formatScore(score)}", color = txt, style = MaterialTheme.typography.titleMedium)
                Text("Уровень: $level", color = txt, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                if (score >= bestScore && score > 0) {
                    Text("Новый рекорд по очкам!", color = Color(0xFFFFD700), style = MaterialTheme.typography.titleMedium)
                } else {
                    Text("Лучший счёт: ${formatScore(bestScore)}", color = muted, style = MaterialTheme.typography.titleMedium)
                }

                if (level >= bestLevel && level > 1) {
                    Text("Новый рекорд по уровню!", color = Color(0xFFFFD700), style = MaterialTheme.typography.titleMedium)
                } else {
                    Text("Лучший уровень: $bestLevel", color = muted, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFB0C4D8) else Color(0xFF415A77),
                        contentColor = txt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Начать заново", style = MaterialTheme.typography.titleMedium) }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF1B263B),
                        contentColor = if (isLightTheme) Color(0xFF1A1A1A) else Color(0xFFB0B0B0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Настройки", style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}