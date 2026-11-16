package com.example.tetrisandroid.pvp

sealed class PvpEvent {

    data class RoomCreated(val roomId: String) : PvpEvent()
    data object Connected : PvpEvent()

    /** Сервер подтвердил вход в комнату. */
    data class Joined(
        val roomId: String,
        val playerId: String,
        val opponentConnected: Boolean,
        val opponentName: String?
    ) : PvpEvent()

    /** Оба игрока на месте — можно стартовать. */
    data class Start(val seed: Long, val startLevel: Int) : PvpEvent()

    /** Очки соперника (для отображения разницы). */
    data class OpponentScore(
        val score: Int,
        val level: Int,
        val lines: Int,
        val gained: Int = 0
    ) : PvpEvent()

    /** Соперник очистил линии — добавить мусор. */
    data class AddGarbage(val lines: Int) : PvpEvent()

    /** Соперник проиграл. */
    object OpponentGameOver : PvpEvent()

    /** Соперник победил. */
    object OpponentWin : PvpEvent()

    /** Соперник отключился. */
    object OpponentDisconnected : PvpEvent()

    /** Ошибка от сервера. */
    data class Error(val message: String) : PvpEvent()

    /** WebSocket закрыт/недоступен. */
    object Disconnected : PvpEvent()
}