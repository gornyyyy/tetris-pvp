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
import com.example.tetrisandroid.ui.theme.DarkAccent
import com.example.tetrisandroid.ui.theme.LightAccent

@Composable
fun WinDialog(
    score: Int,
    level: Int,
    isLightTheme: Boolean,
    onRestart: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White
    val btnBg = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF2E4A66)

    Dialog(onDismissRequest = { }) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ПОБЕДА!", color = if (isLightTheme) LightAccent else DarkAccent, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Счёт: ${formatScore(score)}", color = txt, style = MaterialTheme.typography.titleMedium)
                Text("Уровень: $level", color = txt, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = btnBg, contentColor = txt),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Играть снова", style = MaterialTheme.typography.titleMedium) }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onExitToMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = btnBg, contentColor = txt),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("В меню", style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}