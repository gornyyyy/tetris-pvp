package com.example.tetrisandroid.pvp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

class PvpClient(private val serverUrl: String) {

    companion object {
        private const val TAG = "PvpClient"
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _events = MutableSharedFlow<PvpEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val events: SharedFlow<PvpEvent> = _events

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    fun createRoom(roomId: String, name: String) {
        send(JSONObject().apply {
            put("type", "create_room")
            put("roomId", roomId)
            put("name", name)
        })
    }

    fun joinRoom(roomId: String, name: String) {
        send(JSONObject().apply {
            put("type", "join")
            put("roomId", roomId)
            put("name", name)
        })
    }

    /** Подключиться к серверу и войти в комнату. */
    fun connect() {
        if (webSocket != null) {
            Log.w(TAG, "connect() called while already connected")
            return
        }

        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "onOpen")
                _connected.value = true
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "< $text")
                runCatching { parse(text) }
                    .onFailure { Log.e(TAG, "parse error", it) }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "onFailure", t)
                _connected.value = false
                webSocket = null
                _events.tryEmit(PvpEvent.Disconnected)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "onClosed $code $reason")
                _connected.value = false
                webSocket = null
                _events.tryEmit(PvpEvent.Disconnected)
            }
        })
    }

    /**
     * Проверяет доступность сервера: открывает отдельный WebSocket и ждёт onOpen/onFailure.
     * Возвращает true, если сервер ответил за [timeoutMs] мс.
     */
    suspend fun ping(timeoutMs: Long = 3000L): Boolean {
        return withContext(Dispatchers.IO) {
            val latch = CountDownLatch(1)
            val result = AtomicBoolean(false)

            val request = Request.Builder().url(serverUrl).build()
            val ws = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(ws: WebSocket, response: Response) {
                    result.set(true)
                    latch.countDown()
                    ws.close(1000, "ping")
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    result.set(false)
                    latch.countDown()
                }
            })

            val ok = latch.await(timeoutMs, TimeUnit.MILLISECONDS)
            if (!ok) {
                ws.cancel()
            }
            result.get() && ok
        }
    }

    private fun parse(text: String) {
        val json = JSONObject(text)
        when (val type = json.getString("type")) {
            "room_created" -> _events.tryEmit(
                PvpEvent.RoomCreated(json.optString("roomId"))
            )
            "joined" -> _events.tryEmit(
                PvpEvent.Joined(
                    roomId = json.optString("roomId"),
                    playerId = json.optString("playerId"),
                    opponentConnected = json.optBoolean("opponentConnected"),
                    opponentName = json.optString("opponentName").takeIf { it.isNotEmpty() && it != "null" }
                )
            )
            "start" -> _events.tryEmit(PvpEvent.Start)
            "opponent_score" -> _events.tryEmit(
                PvpEvent.OpponentScore(
                    score = json.optInt("score"),
                    level = json.optInt("level"),
                    lines = json.optInt("lines")
                )
            )
            "add_garbage" -> _events.tryEmit(
                PvpEvent.AddGarbage(json.optInt("lines"))
            )
            "opponent_game_over" -> _events.tryEmit(PvpEvent.OpponentGameOver)
            "opponent_win" -> _events.tryEmit(PvpEvent.OpponentWin)
            "opponent_disconnected" -> _events.tryEmit(PvpEvent.OpponentDisconnected)
            "error" -> _events.tryEmit(
                PvpEvent.Error(json.optString("message"))
            )
            else -> Log.w(TAG, "unknown type: $type")
        }
    }

    // --- Отправка событий ---

    fun sendScore(score: Int, level: Int, lines: Int) {
        send(JSONObject().apply {
            put("type", "score_update")
            put("score", score)
            put("level", level)
            put("lines", lines)
        })
    }

    fun sendLineClear(lines: Int) {
        send(JSONObject().apply {
            put("type", "line_clear")
            put("lines", lines)
        })
    }

    fun sendGameOver() = send(JSONObject().put("type", "game_over"))

    fun sendWin() = send(JSONObject().put("type", "win"))

    /** Мягкое отключение (сервер получит leave). */
    fun disconnect() {
        runCatching { send(JSONObject().put("type", "leave")) }
        webSocket?.close(1000, "bye")
        webSocket = null
        _connected.value = false
    }

    private fun send(json: JSONObject) {
        val payload = json.toString()
        Log.d(TAG, "> $payload")
        webSocket?.send(payload) ?: Log.e(TAG, "webSocket is null, can't send")
    }
}