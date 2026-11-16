package com.example.tetrisandroid.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PvpPauseDialog(
    isLightTheme: Boolean,
    isWinner: Boolean,
    onResume: () -> Unit,
    onSurrender: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White

    Dialog(onDismissRequest = onResume) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isWinner) {
                    Text(
                        "Вы победили, но можете продолжать играть",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text("ПАУЗА", color = txt, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Продолжить", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isWinner) {
                    Button(
                        onClick = onExitToMenu,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF415A77),
                            contentColor = txt
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Выйти в меню", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Button(
                        onClick = onSurrender,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B3A3A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Сдаться", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}