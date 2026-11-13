package com.example.tetrisandroid.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun SettingsDialog(
    isLightTheme: Boolean,
    highlightColumn: Boolean,
    onLightThemeChange: (Boolean) -> Unit,
    onHighlightChange: (Boolean) -> Unit,
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
            Column {
                Text("НАСТРОЙКИ", color = txt, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Светлая тема", color = txt, style = MaterialTheme.typography.titleMedium)
                    Switch(checked = isLightTheme, onCheckedChange = onLightThemeChange)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Подсветка колонки\n(ах ты казуальщик))",
                        color = txt,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(checked = highlightColumn, onCheckedChange = onHighlightChange)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF415A77),
                        contentColor = txt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Закрыть", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}