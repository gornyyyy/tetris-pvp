package com.example.tetrisandroid.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tetrisandroid.ui.game.formatScore

@Composable
fun PvpResultDialog(
    isVictory: Boolean,
    myScore: Int,
    opponentScore: Int,
    isLightTheme: Boolean,
    onExitToMenu: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White
    val muted = if (isLightTheme) Color(0xFF6B7280) else Color(0xFF9AA8B8)
    val accent = if (isVictory) Color(0xFF4CAF50) else Color(0xFFE53935)

    Dialog(onDismissRequest = onExitToMenu) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isVictory) "ПОБЕДА!" else "ПОРАЖЕНИЕ",
                    color = accent,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ВЫ", color = muted, style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = myScore.toString(),
                            color = txt,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = MaterialTheme.typography.titleLarge.fontSize * 0.8f
                            )
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("СОПЕРНИК", color = muted, style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = opponentScore.toString(),
                            color = txt,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = MaterialTheme.typography.titleLarge.fontSize * 0.8f
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onExitToMenu,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF415A77),
                        contentColor = txt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Вернуть в меню", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}