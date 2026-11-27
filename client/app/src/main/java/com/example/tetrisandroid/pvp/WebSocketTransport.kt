package com.example.tetrisandroid.pvp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class WebSocketTransport(private val serverUrl: String) : PvpTransport {

    companion object {
        private const val TAG = "WebSocketTransport"
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val pendingMessages = ArrayDeque<String>()

    private val _events = MutableSharedFlow<PvpEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<PvpEvent> = _events

    private var lastSentScore = 0

    // --- Методы для ручного управления комнатой (используются в PvpRoomScreen) ---
    fun createRoom(roomId: String, name: String, startLevel: Int = 1) {
        sendRaw(JSONObject().apply {
            put("type", "create_room")
            put("roomId", roomId)
            put("name", name)
            put("startLevel", startLevel)
        })
    }

    fun joinRoom(roomId: String, name: String) {
        sendRaw(JSONObject().apply {
            put("type", "join")
            put("roomId", roomId)
            put("name", name)
        })
    }

    // --- Реализация PvpTransport ---
    override fun connect() {
        if (webSocket != null) return

        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "onOpen")
                _connected = true
                while (pendingMessages.isNotEmpty()) {
                    ws.send(pendingMessages.removeFirst())
                }
                _events.tryEmit(PvpEvent.Connected)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "< $text")
                parse(text)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "onFailure", t)
                _connected = false
                webSocket = null
                _events.tryEmit(PvpEvent.Disconnected)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "onClosed $code $reason")
                _connected = false
                webSocket = null
                _events.tryEmit(PvpEvent.Disconnected)
            }
        })
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
        runCatching { sendRaw(JSONObject().put("type", "leave")) }
        pendingMessages.clear()
        webSocket?.close(1000, "bye")
        webSocket = null
        _connected = false
    }

    // --- Ping (для MenuScreen) ---
    suspend fun ping(timeoutMs: Long = 5000L): Boolean {
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
            if (!ok) ws.cancel()
            result.get() && ok
        }
    }

    // --- Вспомогательные ---
    private var _connected = false

    private fun sendRaw(json: JSONObject) {
        val payload = json.toString()
        val ws = webSocket
        if (ws == null) {
            pendingMessages.addLast(payload)
        } else {
            ws.send(payload)
        }
    }

    private fun parse(text: String) {
        val json = JSONObject(text)
        when (val type = json.getString("type")) {
            "room_created" -> _events.tryEmit(PvpEvent.RoomCreated(json.optString("roomId")))
            "joined" -> _events.tryEmit(
                PvpEvent.Joined(
                    roomId = json.optString("roomId"),
                    playerId = json.optString("playerId"),
                    opponentConnected = json.optBoolean("opponentConnected"),
                    opponentName = json.optString("opponentName").takeIf { it.isNotEmpty() && it != "null" }
                )
            )
            "start" -> _events.tryEmit(
                PvpEvent.Start(
                    seed = json.optLong("seed"),
                    startLevel = json.optInt("startLevel", 1)
                )
            )
            "opponent_score" -> _events.tryEmit(
                PvpEvent.OpponentScore(
                    score = json.optInt("score"),
                    level = json.optInt("level"),
                    lines = json.optInt("lines"),
                    gained = json.optInt("gained")
                )
            )
            "add_garbage" -> _events.tryEmit(PvpEvent.AddGarbage(json.optInt("lines")))
            "opponent_game_over" -> _events.tryEmit(PvpEvent.OpponentGameOver)
            "opponent_win" -> _events.tryEmit(PvpEvent.OpponentWin)
            "opponent_disconnected" -> _events.tryEmit(PvpEvent.OpponentDisconnected)
            "error" -> _events.tryEmit(PvpEvent.Error(json.optString("message")))
        }
    }
}