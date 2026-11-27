package com.example.tetrisandroid.pvp

import kotlinx.coroutines.flow.SharedFlow
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Общий интерфейс для любого транспорта PvP (WebSocket, локальный сокет и т.д.).
 * TetrisScreen работает только с этим интерфейсом и не знает, что под ним.
 */
interface PvpTransport {
    /** Поток входящих событий (уже распарсенных или сырых). */
    val events: SharedFlow<PvpEvent>

    /** Подключиться (открыть сокет / установить соединение). */
    fun connect()

    /** Отправить очки. */
    fun sendScore(score: Int, level: Int, lines: Int)

    /** Отправить информацию об очистке линий (для атаки). */
    fun sendLineClear(lines: Int)

    /** Сообщить о проигрыше. */
    fun sendGameOver()

    /** Сообщить о победе. */
    fun sendWin()

    /** Мягко отключиться. */
    fun disconnect()
}

// Утилита для получения локального IP (нужна для хоста в локальной игре)
fun getLocalIpAddress(): String? {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces().asSequence()
        for (intf in interfaces) {
            val addrs = intf.inetAddresses.asSequence()
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    return addr.hostAddress
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}