package com.example.tetrisandroid.pvp

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.time.Duration.Companion.seconds

class LocalSocketTransport(
    private val isHost: Boolean,
    private val hostIp: String? = null,
    private val tcpPort: Int = 8988,
    private val startLevel: Int = 1,
    private val playerName: String = "Игрок",
    private val enableGarbage: Boolean = true
) : PvpTransport {

    companion object {
        private const val TAG = "LocalSocket"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverSocket: ServerSocket? = null
    private var playerSocket: Socket? = null
    private var writer: BufferedWriter? = null

    private val _events = MutableSharedFlow<PvpEvent>(
        replay = 8,
        extraBufferCapacity = 64
    )
    override val events: SharedFlow<PvpEvent> = _events

    private var lastSentScore = 0
    private var matchStarted = false

    private var localReady = false
    private var remoteReady = false
    private var startSent = false

    private var opponentName: String? = null

    override fun connect() {
        if (isHost) {
            if (serverSocket != null && !serverSocket!!.isClosed) return
            scope.launch {
                try {
                    Log.d(TAG, "=== ХОСТ: TCP-сервер на порту $tcpPort ===")
                    val ss = ServerSocket()
                    ss.reuseAddress = true
                    ss.bind(InetSocketAddress("0.0.0.0", tcpPort))
                    serverSocket = ss
                    while (scope.isActive) {
                        val socket = ss.accept()
                        Log.d(TAG, "Хост: входящее подключение от ${socket.inetAddress.hostAddress}")
                        handleIncomingSocket(socket)
                    }
                } catch (e: Exception) {
                    if (scope.isActive) {
                        Log.e(TAG, "Ошибка сервера", e)
                        _events.tryEmit(PvpEvent.Error("Ошибка: ${e.message}"))
                    }
                }
            }
        } else {
            if (playerSocket != null && !playerSocket!!.isClosed) return
            scope.launch {
                if (hostIp == null) {
                    _events.tryEmit(PvpEvent.Error("Неизвестный IP хоста"))
                    return@launch
                }
                Log.d(TAG, "=== КЛИЕНТ: подключение к $hostIp:$tcpPort ===")
                var attempts = 0
                while (attempts < 10 && scope.isActive) {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(hostIp, tcpPort), 5000)
                        Log.d(TAG, "✅ Клиент: TCP-соединение установлено (попытка ${attempts + 1})")
                        setupClientSocket(socket)
                        break
                    } catch (e: Exception) {
                        attempts++
                        Log.w(TAG, "Клиент: попытка $attempts не удалась: ${e.message}")
                        delay(1.seconds)
                    }
                }
                if (attempts >= 10) {
                    Log.e(TAG, "❌ Клиент: не удалось подключиться")
                    _events.tryEmit(PvpEvent.Error("Не удалось подключиться к хосту"))
                }
            }
        }
    }

    suspend fun ping(hostIp: String, timeoutMs: Long = 3000L): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(hostIp, tcpPort), timeoutMs.toInt())
                socket.close()
                Log.d(TAG, "PING OK -> $hostIp:$tcpPort")
                true
            } catch (e: Exception) {
                Log.e(TAG, "PING FAIL -> $hostIp:$tcpPort: ${e.message}")
                false
            }
        }
    }

    override fun sendReady() {
        localReady = true
        sendReadyMessage()
        maybeStart()
        Log.d(TAG, "ready: local=$localReady remote=$remoteReady (host=$isHost)")
    }

    private fun sendReadyMessage() {
        if (writer != null) sendRaw(JSONObject().put("type", "ready"))
    }

    /** ✅ Старт только когда оба в игровой комнате. Решает только хост (у него есть сокет клиента). */
    private fun maybeStart() {
        if (!isHost || startSent || !localReady || !remoteReady) return
        startSent = true
        val seed = (1..2147483647).random().toLong()
        sendRaw(JSONObject().apply {
            put("type", "start")
            put("seed", seed)
            put("startLevel", startLevel)
            put("opponentName", playerName)
            put("enableGarbage", enableGarbage)
        })
        _events.tryEmit(PvpEvent.Start(seed, startLevel, enableGarbage))
        matchStarted = true
        Log.d(TAG, "▶ Матч запущен (оба игрока в комнате), seed=$seed")
    }

    private fun handleIncomingSocket(socket: Socket) {
        if (playerSocket != null && !playerSocket!!.isClosed) {
            Log.w(TAG, "Хост: слот занят, лишнее подключение отклонено")
            try { socket.close() } catch (_: Exception) {}
            return
        }

        scope.launch {
            var accepted = false
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val firstLine = reader.readLine()
                if (firstLine == null) {
                    Log.d(TAG, "Хост: пустое подключение (ping-зонд) — игнорируем")
                    try { socket.close() } catch (_: Exception) {}
                    return@launch
                }

                playerSocket = socket
                writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                accepted = true
                Log.d(TAG, "< $firstLine")
                parse(firstLine)

                sendReadyMessage()

                while (scope.isActive) {
                    val line = reader.readLine() ?: break
                    Log.d(TAG, "< $line")
                    parse(line)
                }
            } catch (e: Exception) {
                if (scope.isActive) Log.e(TAG, "Ошибка чтения", e)
            } finally {
                if (accepted) {
                    Log.d(TAG, "Хост: соперник отключился")
                    playerSocket = null
                    writer = null
                    matchStarted = false
                    remoteReady = false
                    if (scope.isActive) _events.tryEmit(PvpEvent.OpponentDisconnected)
                }
            }
        }
    }

    private fun setupClientSocket(socket: Socket) {
        playerSocket = socket
        writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                sendRaw(JSONObject().apply {
                    put("type", "hello")
                    put("name", playerName)
                })
                sendReadyMessage()

                while (scope.isActive) {
                    val line = reader.readLine() ?: break
                    Log.d(TAG, "< $line")
                    parse(line)
                }
                if (scope.isActive) _events.tryEmit(PvpEvent.OpponentDisconnected)
            } catch (e: Exception) {
                if (scope.isActive) {
                    Log.e(TAG, "Ошибка чтения", e)
                    _events.tryEmit(PvpEvent.OpponentDisconnected)
                }
            }
        }
    }

    private fun parse(text: String) {
        val json = JSONObject(text)
        when (json.getString("type")) {
            "hello" -> {
                val name = json.optString("name", "Соперник")
                opponentName = name
                _events.tryEmit(PvpEvent.Joined("", "p2", true, name))
                sendRaw(JSONObject().apply {
                    put("type", "hello_reply")
                    put("name", playerName)
                })
            }

            "hello_reply" -> {
                val name = json.optString("name", "Соперник")
                opponentName = name
                _events.tryEmit(PvpEvent.Joined("", "p1", true, name))
            }

            "ready" -> {
                remoteReady = true
                Log.d(TAG, "ready от соперника")
                maybeStart()
            }
            "start" -> {
                val garbage = json.optBoolean("enableGarbage", true)
                json.optString("opponentName").takeIf { it.isNotBlank() }?.let {
                    opponentName = it
                    _events.tryEmit(PvpEvent.Joined("", if (isHost) "p2" else "p1", true, it))
                }
                _events.tryEmit(
                    PvpEvent.Start(
                        seed = json.optLong("seed"),
                        startLevel = json.optInt("startLevel", 1),
                        enableGarbage = garbage
                    )
                )
                matchStarted = true
            }
            "score_update" -> _events.tryEmit(
                PvpEvent.OpponentScore(
                    score = json.optInt("score"),
                    level = json.optInt("level"),
                    lines = json.optInt("lines"),
                    gained = json.optInt("gained")
                )
            )
            "line_clear" -> _events.tryEmit(PvpEvent.AddGarbage(json.optInt("lines")))
            "game_over" -> _events.tryEmit(PvpEvent.OpponentGameOver)
            "win" -> _events.tryEmit(PvpEvent.OpponentWin)
        }
    }

    override fun sendScore(score: Int, level: Int, lines: Int) {
        val gained = (score - lastSentScore).coerceAtLeast(0)
        lastSentScore = score
        sendRaw(JSONObject().apply {
            put("type", "score_update")
            put("score", score)
            put("level", level)
            put("lines", lines)
            put("gained", gained)
        })
    }

    override fun sendLineClear(lines: Int) {
        val garbageLines = (lines - 1).coerceAtLeast(0)

        sendRaw(JSONObject().apply {
            put("type", "line_clear")
            put("lines", garbageLines)
        })
    }

    override fun sendGameOver() = sendRaw(JSONObject().put("type", "game_over"))
    override fun sendWin() = sendRaw(JSONObject().put("type", "win"))

    override fun disconnect() {
        try {
            writer?.close()
            playerSocket?.close()
            serverSocket?.close()
            Log.d(TAG, "✅ Все сокеты закрыты")
        } catch (e: Exception) {
            Log.w(TAG, "Ошибка при закрытии сокетов", e)
        }
        scope.cancel()
    }

    private fun sendRaw(json: JSONObject) {
        scope.launch {
            try {
                val w = writer ?: return@launch
                w.write(json.toString())
                w.newLine()
                w.flush()
                Log.d(TAG, "> $json")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка записи", e)
            }
        }
    }
}