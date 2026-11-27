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
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.time.Duration.Companion.seconds

class LocalSocketTransport(
    private val isHost: Boolean,
    private val hostIp: String? = null,
    private val tcpPort: Int = 8988,
    private val startLevel: Int = 1,
    private val playerName: String = "Игрок"
) : PvpTransport {

    companion object {
        private const val TAG = "LocalSocket"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: BufferedWriter? = null

    private val _events = MutableSharedFlow<PvpEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<PvpEvent> = _events

    private var lastSentScore = 0

    override fun connect() {
        if (isHost && serverSocket != null && !serverSocket!!.isClosed) return
        if (!isHost && clientSocket != null && !clientSocket!!.isClosed) return

        scope.launch {
            try {
                if (isHost) {
                    Log.d(TAG, "=== ХОСТ: TCP-сервер на порту $tcpPort ===")

                    val ss = ServerSocket()
                    ss.reuseAddress = true
                    ss.bind(InetSocketAddress("0.0.0.0", tcpPort))
                    serverSocket = ss

                    Log.d(TAG, "Хост: ServerSocket открыт. Ожидание клиента...")
                    val socket = serverSocket!!.accept()
                    Log.d(TAG, "✅ Хост: клиент подключился с ${socket.inetAddress.hostAddress}")
                    setupTcpSocket(socket)

                } else {
                    if (hostIp == null) {
                        Log.e(TAG, "Клиент: hostIp не задан")
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
                            setupTcpSocket(socket)
                            break
                        } catch (e: Exception) {
                            attempts++
                            Log.w(TAG, "Клиент: попытка $attempts не удалась: ${e.message}")
                            delay(2.seconds)
                        }
                    }
                    if (attempts >= 10) {
                        Log.e(TAG, "❌ Клиент: не удалось подключиться к хосту")
                        _events.tryEmit(PvpEvent.Error("Не удалось подключиться к хосту"))
                    }
                }
            } catch (e: Exception) {
                if (scope.isActive) {
                    Log.e(TAG, "Ошибка транспорта", e)
                    _events.tryEmit(PvpEvent.Error("Ошибка: ${e.message}"))
                }
            }
        }
    }

    private fun setupTcpSocket(socket: Socket) {
        clientSocket = socket
        writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (scope.isActive) {
                    val line = reader.readLine() ?: break
                    Log.d(TAG, "< $line")
                    parse(line)
                }
            } catch (e: Exception) {
                if (scope.isActive) {
                    Log.e(TAG, "Ошибка чтения", e)
                    _events.tryEmit(PvpEvent.OpponentDisconnected)
                }
            }
        }

        if (isHost) {
            val seed = (1..2147483647).random().toLong()
            sendRaw(JSONObject().apply {
                put("type", "start")
                put("seed", seed)
                put("startLevel", startLevel)
            })
            _events.tryEmit(PvpEvent.Joined("", "p1", true, "Соперник"))
            _events.tryEmit(PvpEvent.Start(seed, startLevel))
        } else {
            sendRaw(JSONObject().apply {
                put("type", "hello")
                put("name", playerName)
            })
            _events.tryEmit(PvpEvent.Joined("", "p2", true, "Соперник"))
        }
    }

    private fun parse(text: String) {
        val json = JSONObject(text)
        when (json.getString("type")) {
            "hello" -> {
                val name = json.optString("name", "Соперник")
                _events.tryEmit(PvpEvent.Joined("", "p2", true, name))
                sendRaw(JSONObject().apply {
                    put("type", "hello_reply")
                    put("name", playerName)
                })
            }
            "hello_reply" -> {
                val name = json.optString("name", "Соперник")
                _events.tryEmit(PvpEvent.Joined("", "p1", true, name))
            }
            "start" -> {
                _events.tryEmit(
                    PvpEvent.Start(
                        seed = json.optLong("seed"),
                        startLevel = json.optInt("startLevel", 1)
                    )
                )
            }
            "joined" -> _events.tryEmit(
                PvpEvent.Joined(
                    roomId = json.optString("roomId", ""),
                    playerId = json.optString("playerId", ""),
                    opponentConnected = json.optBoolean("opponentConnected", true),
                    opponentName = json.optString("opponentName").takeIf { it.isNotEmpty() && it != "null" }
                )
            )
            "score_update" -> {
                _events.tryEmit(
                    PvpEvent.OpponentScore(
                        score = json.optInt("score"),
                        level = json.optInt("level"),
                        lines = json.optInt("lines"),
                        gained = json.optInt("gained")
                    )
                )
            }
            "line_clear" -> {
                _events.tryEmit(PvpEvent.AddGarbage(json.optInt("lines")))
            }
            "game_over" -> {
                _events.tryEmit(PvpEvent.OpponentGameOver)
            }
            "win" -> {
                _events.tryEmit(PvpEvent.OpponentWin)
            }
            "add_garbage" -> _events.tryEmit(PvpEvent.AddGarbage(json.optInt("lines")))
            "opponent_game_over" -> _events.tryEmit(PvpEvent.OpponentGameOver)
            "opponent_win" -> _events.tryEmit(PvpEvent.OpponentWin)
            "opponent_disconnected" -> _events.tryEmit(PvpEvent.OpponentDisconnected)
            "error" -> _events.tryEmit(PvpEvent.Error(json.optString("message")))
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
        sendRaw(JSONObject().apply {
            put("type", "line_clear")
            put("lines", lines)
        })
    }

    override fun sendGameOver() = sendRaw(JSONObject().put("type", "game_over"))
    override fun sendWin() = sendRaw(JSONObject().put("type", "win"))

    override fun disconnect() {
        scope.launch {
            try {
                writer?.close()
                clientSocket?.close()
                serverSocket?.close()
                Log.d(TAG, "✅ Все сокеты закрыты")
            } catch (e: Exception) {
                Log.w(TAG, "Ошибка при закрытии сокетов", e)
            } finally {
                scope.cancel()
            }
        }
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