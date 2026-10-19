package com.example.tetrisandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tetrisandroid.game.Game
import com.example.tetrisandroid.model.PlayerAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AppBackground = Color(0xFF0D1B2A)
private val ButtonColor = Color(0xFF1B263B)
private val ButtonPressedColor = Color(0xFF415A77)

data class UiState(
    val grid: Array<IntArray>,
    val activeBlocks: List<Pair<Int, Int>>,
    val activeColorArgb: Int,
    val nextBlocks: List<Pair<Int, Int>>,
    val nextColorArgb: Int,
    val score: Int,
    val level: Int,
    val isRunning: Boolean
)

@Composable
fun TetrisScreen() {
    val game = remember { Game(onUpdate = {}, onGameOver = {}) }

    var uiState by remember {
        mutableStateOf(
            UiState(
                grid = Array(24) { IntArray(10) },
                activeBlocks = emptyList(),
                activeColorArgb = 0xFF00FFFF.toInt(),
                nextBlocks = emptyList(),
                nextColorArgb = 0xFF00FFFF.toInt(),
                score = 0,
                level = 1,
                isRunning = true
            )
        )
    }

    fun refreshUi() {
        uiState = UiState(
            grid = Array(game.grid.size) { r -> game.grid[r].clone() },
            activeBlocks = game.currentShape.getBlocks().map { it.x to it.y },
            activeColorArgb = game.currentShape.colorArgb,
            nextBlocks = game.nextShape.getBlocks().map { it.x to it.y },
            nextColorArgb = game.nextShape.colorArgb,
            score = game.score,
            level = game.level,
            isRunning = game.isRunning
        )
    }

    LaunchedEffect(Unit) {
        refreshUi()
        while (game.isRunning) {
            delay(50)
            game.tick()
            refreshUi()
        }
        refreshUi()
    }

    val state = uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(0.75f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
            ) {
                TetrisBoardCanvas(
                    grid = state.grid,
                    activeBlocks = state.activeBlocks,
                    activeColorArgb = state.activeColorArgb
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("SCORE", color = Color(0xFF778DA9), style = MaterialTheme.typography.labelMedium)
                Text("${state.score}", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text("LEVEL", color = Color(0xFF778DA9), style = MaterialTheme.typography.labelMedium)
                Text("${state.level}", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(20.dp))
                Text("NEXT", color = Color(0xFF778DA9), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                NextShapeCanvas(
                    nextBlocks = state.nextBlocks,
                    colorArgb = state.nextColorArgb
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    ActionButton("←") { game.handleInput(PlayerAction.MOVE_LEFT); refreshUi() }
                    Spacer(modifier = Modifier.width(8.dp))
                    ActionButton("→") { game.handleInput(PlayerAction.MOVE_RIGHT); refreshUi() }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton("↓") { game.handleInput(PlayerAction.MOVE_DOWN); refreshUi() }
            }

            SimpleButton(
                label = "↻",
                onPress = { game.handleInput(PlayerAction.ROTATE); refreshUi() },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }

    if (!state.isRunning) {
        GameOverDialog(
            score = state.score,
            level = state.level,
            onRestart = {
                game.reset()
                refreshUi()
            }
        )
    }
}

@Composable
fun GameOverDialog(score: Int, level: Int, onRestart: () -> Unit) {
    Dialog(onDismissRequest = { /* нельзя закрыть */ }) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF1B263B), RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME OVER",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Счёт: $score",
                    color = Color(0xFFE0E0E0),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Уровень: $level",
                    color = Color(0xFFE0E0E0),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF415A77),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Начать заново", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun ActionButton(label: String, onPress: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    var pressJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .width(78.dp)
            .height(72.dp)
            .background(
                if (isPressed) ButtonPressedColor else ButtonColor,
                RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        isPressed = true
                        onPress()
                        pressJob = scope.launch {
                            delay(200)
                            while (isPressed) {
                                onPress()
                                delay(50)
                            }
                        }
                        waitForUpOrCancellation()
                        isPressed = false
                        pressJob?.cancel()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun SimpleButton(label: String, onPress: () -> Unit, modifier: Modifier = Modifier) {
    var isPressed by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .width(78.dp)
            .height(72.dp)
            .background(
                if (isPressed) ButtonPressedColor else ButtonColor,
                RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        isPressed = true
                        onPress()
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.headlineMedium)
    }
}

