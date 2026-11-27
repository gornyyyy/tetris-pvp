package com.example.tetrisandroid.pvp

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

data class DiscoveredRoom(val name: String, val ip: String, val port: Int)

class LocalRoomDiscovery {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _discoveredRooms = MutableStateFlow<List<DiscoveredRoom>>(emptyList())
    val discoveredRooms: StateFlow<List<DiscoveredRoom>> = _discoveredRooms

    private var broadcastSocket: DatagramSocket? = null
    private var listenSocket: DatagramSocket? = null
    private val PORT = 8989

    fun startHosting(roomName: String, hostIp: String, port: Int) {
        scope.launch {
            try {
                broadcastSocket = DatagramSocket().apply { broadcast = true }
                val json = JSONObject().apply {
                    put("type", "room_info")
                    put("name", roomName)
                    put("ip", hostIp)
                    put("port", port)
                }.toString().toByteArray(Charsets.UTF_8)

                while (scope.isActive) {
                    val packet = DatagramPacket(json, json.size, InetAddress.getByName("255.255.255.255"), PORT)
                    broadcastSocket?.send(packet)
                    delay(2000)
                }
            } catch (e: Exception) {
                Log.e("LocalRoomDiscovery", "Broadcast error", e)
            }
        }
    }

    fun startListening() {
        scope.launch {
            try {
                listenSocket = DatagramSocket(PORT).apply {
                    broadcast = true
                    reuseAddress = true
                }
                val buffer = ByteArray(1024)

                while (scope.isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    listenSocket?.receive(packet)
                    val data = String(packet.data, 0, packet.length)
                    val json = JSONObject(data)

                    if (json.optString("type") == "room_info") {
                        val room = DiscoveredRoom(
                            name = json.optString("name", "Неизвестная комната"),
                            ip = json.optString("ip", packet.address.hostAddress),
                            port = json.optInt("port", 8988)
                        )

                        // Обновляем список: если IP уже есть, обновляем, иначе добавляем
                        val currentList = _discoveredRooms.value.toMutableList()
                        val existingIndex = currentList.indexOfFirst { it.ip == room.ip }
                        if (existingIndex != -1) {
                            currentList[existingIndex] = room
                        } else {
                            currentList.add(room)
                        }
                        _discoveredRooms.value = currentList
                    }
                }
            } catch (e: Exception) {
                if (scope.isActive) Log.e("LocalRoomDiscovery", "Listen error", e)
            }
        }
    }

    fun stop() {
        scope.cancel()
        broadcastSocket?.close()
        listenSocket?.close()
        _discoveredRooms.value = emptyList()
    }
}