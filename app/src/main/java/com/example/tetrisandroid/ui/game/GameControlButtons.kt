package com.example.tetrisandroid.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tetrisandroid.R
import com.example.tetrisandroid.model.PlayerAction
import com.example.tetrisandroid.ui.components.ActionButton
import com.example.tetrisandroid.ui.components.SimpleButton

@Composable
fun GameControlButtons(
    enabled: Boolean,
    buttonColor: Color,
    buttonPressed: Color,
    textColor: Color,
    btnWidth: Dp,
    btnHeight: Dp,
    onAction: (PlayerAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Левая группа: ← → и ↓ под ними
        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                ActionButton(
                    iconRes = R.drawable.ic_right_arrow,
                    enabled = enabled,
                    rotationDeg = 180f,
                    buttonColor = buttonColor,
                    buttonPressed = buttonPressed,
                    textColor = textColor,
                    buttonWidth = btnWidth,
                    buttonHeight = btnHeight
                ) { onAction(PlayerAction.MOVE_LEFT) }
                Spacer(modifier = Modifier.width(8.dp))
                ActionButton(
                    iconRes = R.drawable.ic_right_arrow,
                    enabled = enabled,
                    rotationDeg = 0f,
                    buttonColor = buttonColor,
                    buttonPressed = buttonPressed,
                    textColor = textColor,
                    buttonWidth = btnWidth,
                    buttonHeight = btnHeight
                ) { onAction(PlayerAction.MOVE_RIGHT) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ActionButton(
                iconRes = R.drawable.ic_right_arrow,
                enabled = enabled,
                rotationDeg = 90f,
                buttonColor = buttonColor,
                buttonPressed = buttonPressed,
                textColor = textColor,
                buttonWidth = btnWidth,
                buttonHeight = btnHeight
            ) { onAction(PlayerAction.MOVE_DOWN) }
        }

        // Правая группа: ↻ и ⤓
        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SimpleButton(
                iconRes = R.drawable.rotate_arrow,
                enabled = enabled,
                rotationDeg = 0f,
                buttonColor = buttonColor,
                buttonPressed = buttonPressed,
                textColor = textColor,
                buttonWidth = btnWidth,
                buttonHeight = btnHeight
            ) { onAction(PlayerAction.ROTATE) }
            Spacer(modifier = Modifier.height(8.dp))
            SimpleButton(
                iconRes = R.drawable.hd_arrow,
                enabled = enabled,
                rotationDeg = 90f,
                buttonColor = buttonColor,
                buttonPressed = buttonPressed,
                textColor = textColor,
                buttonWidth = btnWidth,
                buttonHeight = btnHeight
            ) { onAction(PlayerAction.HARD_DROP) }
        }
    }
}