package com.example.tetrisandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.example.tetrisandroid.R
import com.example.tetrisandroid.data.BestScoreStorage
import com.example.tetrisandroid.data.GameSettings
import com.example.tetrisandroid.game.Game
import com.example.tetrisandroid.model.PlayerAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// ----- Палитры -----
private val DarkBackground = Color(0xFF0D1B2A)
private val DarkButton = Color(0xFF1B263B)
private val DarkButtonPressed = Color(0xFF415A77)
private val DarkText = Color.White
private val DarkMuted = Color(0xFF778DA9)

private val LightBackground = Color(0xFFF3F6FB)
private val LightButton = Color(0xFFDCE3ED)
private val LightButtonPressed = Color(0xFFB0C4D8)
private val LightText = Color(0xFF1A1A1A)
private val LightMuted = Color(0xFF5A6B7B)

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
fun TetrisScreen(
    bestScoreStorage: BestScoreStorage,
    gameSettings: GameSettings,
    oldRecordsOnUpdate: BestScoreStorage.OldRecords? = null,
    onUpdateDialogClose: () -> Unit = {}
) {
    val game = remember { Game(onUpdate = {}, onGameOver = {}) }

    var bestScore by remember { mutableStateOf(bestScoreStorage.getBestScore()) }
    var bestLevel by remember { mutableStateOf(bestScoreStorage.getBestLevel()) }
    var isStarted by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var gameId by remember { mutableStateOf(0) }

    var showSettings by remember { mutableStateOf(false) }

    var isLightTheme by remember { mutableStateOf(gameSettings.isLightTheme) }
    var highlightColumn by remember { mutableStateOf(gameSettings.highlightColumn) }

    var showUpdateDialog by remember { mutableStateOf(oldRecordsOnUpdate != null) }

    // Палитра
    val background = if (isLightTheme) LightBackground else DarkBackground
    val buttonColor = if (isLightTheme) LightButton else DarkButton
    val buttonPressed = if (isLightTheme) LightButtonPressed else DarkButtonPressed
    val textColor = if (isLightTheme) LightText else DarkText
    val mutedColor = if (isLightTheme) LightMuted else DarkMuted

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
        if (!game.isRunning) {
            bestScoreStorage.saveResults(game.score, game.level)
            bestScore = bestScoreStorage.getBestScore()
            bestLevel = bestScoreStorage.getBestLevel()
        }
    }

    LaunchedEffect(gameId, isPaused) {
        if (!isStarted || isPaused) return@LaunchedEffect
        refreshUi()
        while (game.isRunning) {
            delay(16)
            game.tick()
            refreshUi()
        }
        refreshUi()
    }

    val state = uiState

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        if (isStarted && !isPaused && state.isRunning) {
            isPaused = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
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
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                if (!isStarted) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StartButton(
                            buttonColor = buttonColor,
                            textColor = textColor,
                            onClick = {
                                game.reset()
                                isStarted = true
                                isPaused = false
                                gameId++
                                refreshUi()
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsButton(
                            buttonColor = buttonColor,
                            textColor = mutedColor,
                            onClick = { showSettings = true }
                        )
                    }
                } else {
                    TetrisBoardCanvas(
                        grid = state.grid,
                        activeBlocks = state.activeBlocks,
                        activeColorArgb = state.activeColorArgb,
                        highlightColumn = highlightColumn,
                        isLightTheme = isLightTheme
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("SCORE", color = mutedColor, style = MaterialTheme.typography.labelMedium)
                Text(formatScore(state.score), color = textColor, style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(8.dp))
                Text("BEST", color = mutedColor, style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatScore(bestScore), color = Color(0xFFFFD700), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("lvl $bestLevel", color = mutedColor, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("LEVEL", color = mutedColor, style = MaterialTheme.typography.labelMedium)
                Text("${state.level}", color = textColor, style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(20.dp))
                Text("NEXT", color = mutedColor, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                NextShapeCanvas(
                    nextBlocks = state.nextBlocks,
                    colorArgb = state.nextColorArgb,
                    isLightTheme = isLightTheme
                )

                if (isStarted && state.isRunning && !isPaused) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { isPaused = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("II", style = MaterialTheme.typography.titleMedium)
                    }
                }
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
                    ActionButton(
                        iconRes = R.drawable.ic_right_arrow,
                        rotationDeg = 180f,
                        enabled = isStarted && !isPaused,
                        buttonColor = buttonColor,
                        buttonPressed = buttonPressed,
                        textColor = textColor
                    ) {
                        game.handleInput(PlayerAction.MOVE_LEFT); refreshUi()
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ActionButton(
                        iconRes = R.drawable.ic_right_arrow,
                        rotationDeg = 0f,
                        enabled = isStarted && !isPaused,
                        buttonColor = buttonColor,
                        buttonPressed = buttonPressed,
                        textColor = textColor
                    ) {
                        game.handleInput(PlayerAction.MOVE_RIGHT); refreshUi()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(
                    iconRes = R.drawable.ic_right_arrow,
                    rotationDeg = 90f,
                    enabled = isStarted && !isPaused,
                    buttonColor = buttonColor,
                    buttonPressed = buttonPressed,
                    textColor = textColor
                ) {
                    game.handleInput(PlayerAction.MOVE_DOWN); refreshUi()
                }
            }

            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SimpleButton(
                    iconRes = R.drawable.rotate_arrow,
                    rotationDeg = 0f,
                    enabled = isStarted && !isPaused,
                    buttonColor = buttonColor,
                    buttonPressed = buttonPressed,
                    textColor = textColor,
                    onPress = { game.handleInput(PlayerAction.ROTATE); refreshUi() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SimpleButton(
                    iconRes = R.drawable.hd_arrow,
                    rotationDeg = 90f,
                    enabled = isStarted && !isPaused,
                    buttonColor = buttonColor,
                    buttonPressed = buttonPressed,
                    textColor = textColor,
                    onPress = { game.handleInput(PlayerAction.HARD_DROP); refreshUi() }
                )
            }
        }
    }

    // Game Over
    if (isStarted && !state.isRunning) {
        GameOverDialog(
            score = state.score,
            bestScore = bestScore,
            level = state.level,
            bestLevel = bestLevel,
            isLightTheme = isLightTheme,
            onRestart = {
                game.reset()
                isPaused = false
                gameId++
                refreshUi()
            },
            onSettings = { showSettings = true }
        )
    }

    // Pause
    if (isPaused) {
        PauseDialog(
            bestScore = bestScore,
            bestLevel = bestLevel,
            currentScore = state.score,
            currentLevel = state.level,
            isLightTheme = isLightTheme,
            onResume = { isPaused = false },
            onRestart = {
                game.reset()
                isPaused = false
                gameId++
                refreshUi()
            },
            onSettings = {
                showSettings = true
            }
        )
    }

    // Настройки
    if (showSettings) {
        SettingsDialog(
            isLightTheme = isLightTheme,
            highlightColumn = highlightColumn,
            onLightThemeChange = {
                isLightTheme = it
                gameSettings.isLightTheme = it
            },
            onHighlightChange = {
                highlightColumn = it
                gameSettings.highlightColumn = it
            },
            onClose = {
                showSettings = false
            }
        )
    }

    if (showUpdateDialog && oldRecordsOnUpdate != null) {
        UpdateDialog(
            oldRecords = oldRecordsOnUpdate,
            isLightTheme = isLightTheme,
            onClose = {
                showUpdateDialog = false
                onUpdateDialogClose()
                bestScore = 0
                bestLevel = 1
            }
        )
    }
}

// ---------- Форматирование числа ----------
fun formatScore(score: Int): String {
    val s = score.toLong()
    return when {
        s >= 1_000_000_000L -> "${"%.1f".format(Locale.US, s / 1_000_000_000.0)}B"
        s >= 1_000_000L -> "${"%.1f".format(Locale.US, s / 1_000_000.0)}M"
        s >= 1_000L -> "${"%.1f".format(Locale.US, s / 1_000.0)}K"
        else -> s.toString()
    }
}

// ---------- Кнопки ----------
@Composable
fun StartButton(buttonColor: Color, textColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(64.dp)
    ) {
        Text("Начать игру", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun SettingsButton(buttonColor: Color, textColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(52.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.titleMedium)
    }
}

// ---------- Диалоги ----------
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
    val muted = if (isLightTheme) Color(0xFF5A6B7B) else Color(0xFF778DA9)

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
                    Text("Подсветка колонки\n(ах ты казуальщик))", color = txt, style = MaterialTheme.typography.titleMedium)
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

@Composable
fun PauseDialog(
    bestScore: Int,
    bestLevel: Int,
    currentScore: Int,
    currentLevel: Int,
    isLightTheme: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onSettings: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White

    // Цвет кнопок — одинаковый для всех трёх
    val btnBg = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF2E4A66)
    val btnTxt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White

    Dialog(onDismissRequest = onResume) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ПАУЗА", color = txt, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Счёт: ${formatScore(currentScore)}", color = txt, style = MaterialTheme.typography.titleMedium)
                Text("Уровень: $currentLevel", color = txt, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Лучший счёт: ${formatScore(bestScore)}", color = Color(0xFFFFD700), style = MaterialTheme.typography.titleMedium)
                Text("Лучший уровень: $bestLevel", color = Color(0xFFFFD700), style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(24.dp))

                // Продолжить
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = btnBg,
                        contentColor = btnTxt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Продолжить", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Настройки — тот же цвет
                Button(
                    onClick = onSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = btnBg,
                        contentColor = btnTxt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Настройки", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = btnBg,
                        contentColor = btnTxt
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Заново", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

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
                    Text("Лучший счёт: $bestScore", color = muted, style = MaterialTheme.typography.titleMedium)
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
                ) {
                    Text("Начать заново", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLightTheme) Color(0xFFDCE3ED) else Color(0xFF1B263B),
                        contentColor = if (isLightTheme) Color(0xFF1A1A1A) else Color(0xFFB0B0B0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Настройки", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

// ---------- Кнопки управления ----------
@Composable
fun ActionButton(
    iconRes: Int,
    enabled: Boolean = true,
    rotationDeg: Float = 0f,
    buttonColor: Color,
    buttonPressed: Color,
    textColor: Color,
    onPress: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var pressJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    val bg = when {
        !enabled -> buttonColor.copy(alpha = 0.4f)
        isPressed -> buttonPressed
        else -> buttonColor
    }
    val iconColor = if (enabled) textColor else textColor.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .width(84.dp)
            .height(72.dp)
            .background(bg, RoundedCornerShape(16.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
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
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp).rotate(rotationDeg)
        )
    }
}

@Composable
fun SimpleButton(
    iconRes: Int,
    enabled: Boolean = true,
    rotationDeg: Float = 0f,
    buttonColor: Color,
    buttonPressed: Color,
    textColor: Color,
    onPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val bg = when {
        !enabled -> buttonColor.copy(alpha = 0.4f)
        isPressed -> buttonPressed
        else -> buttonColor
    }
    val iconColor = if (enabled) textColor else textColor.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .width(84.dp)
            .height(72.dp)
            .background(bg, RoundedCornerShape(16.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
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
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp).rotate(rotationDeg)
        )
    }
}

@Composable
fun UpdateDialog(
    oldRecords: BestScoreStorage.OldRecords,
    isLightTheme: Boolean,
    onClose: () -> Unit
) {
    val bg = if (isLightTheme) Color.White else Color(0xFF1B263B)
    val txt = if (isLightTheme) Color(0xFF1A1A1A) else Color.White
    val muted = if (isLightTheme) Color(0xFF5A6B7B) else Color(0xFF778DA9)

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Обновление!",
                    color = txt,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "А вот и новое обновление, однако всякое поменялось и поэтому ваши рекорды больше ничего не значат)\nно если вдруг кто-то гордился ими или что-то в этом роде, то можете сделать скриншотик",
                    color = txt,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ваш лучший скор: ${oldRecords.score}",
                    color = Color(0xFFFFD700),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Ваш лучший лвл: ${oldRecords.level}",
                    color = Color(0xFFFFD700),
                    style = MaterialTheme.typography.titleMedium
                )

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
                ) {
                    Text("🤟🤟🤟", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}