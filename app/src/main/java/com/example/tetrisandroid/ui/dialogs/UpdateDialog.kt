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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tetrisandroid.data.BestScoreStorage

@Composable
fun UpdateDialog(
    oldRecords: BestScoreStorage.OldRecords,
    isLightTheme: Boolean,
    onClose: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Обновление!", color = txt, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "А вот и новое обновление, добавилось всякого, \nнапример пара новых режимов",
                    color = txt,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Ваш лучший скор в classic: ${oldRecords.score}", color = Color(0xFFFFD700), style = MaterialTheme.typography.titleMedium)
                Text("Ваш лучший лвл в classic: ${oldRecords.level}", color = Color(0xFFFFD700), style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "А теперь время ставить новые рекорды!",
                    color = txt,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFB0C4D8) else Color(0xFF415A77),
                        contentColor = txt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("🤟🤟🤟", style = MaterialTheme.typography.titleLarge) }
            }
        }
    }
}