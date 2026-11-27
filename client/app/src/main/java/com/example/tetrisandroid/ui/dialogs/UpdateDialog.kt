package com.example.tetrisandroid.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.tetrisandroid.data.PatchNote
import com.example.tetrisandroid.data.VersionHandler
import com.example.tetrisandroid.ui.theme.DarkAccent
import com.example.tetrisandroid.ui.theme.LightAccent

@Composable
fun UpdateDialog(
    oldRecords: VersionHandler.LegacyRecords,
    updateNotes: List<PatchNote>,
    isLightTheme: Boolean,
    onClose: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White
    val accent = if (isLightTheme) LightAccent else DarkAccent

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f) // <-- Ограничиваем высоту, чтобы диалог не улетал за экран
                .background(bg, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // <-- Включаем скролл
            ) {
                Text("Обновление!", color = txt, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (updateNotes.isEmpty()) {
                    Text(
                        text = "Вы обновились до последней версии!",
                        color = txt,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                } else {
                    updateNotes.forEach { patch ->
                        // Заголовок патча
                        Text(
                            text = "Патч ${patch.version}",
                            color = accent,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Список изменений
                        patch.notes.forEach { note ->
                            Text(
                                text = "• $note",
                                color = txt,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ну и по традиции:",
                    color = txt,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Ваш лучший скор: ${oldRecords.score}", color = accent, style = MaterialTheme.typography.titleMedium)
                Text("Ваш лучший лвл: ${oldRecords.level}", color = accent, style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ваши рекорды сохранены. Продолжайте играть, побеждайте друзей и т.д.!",
                    color = txt,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onClose, // <-- Просто закрывает диалог, НИЧЕГО не сбрасывает
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFB0C4D8) else Color(0xFF415A77),
                        contentColor = txt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Жесткий хайп 🤟", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}