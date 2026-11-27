package com.example.tetrisandroid.pvp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocalPvpManager(private val context: Context) {

    companion object {
        private const val TAG = "LocalPvp"
    }

    private val manager: WifiP2pManager? =
        context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private var channel: WifiP2pManager.Channel? = null

    private val _peers = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    val peers: StateFlow<List<WifiP2pDevice>> = _peers

    private val _isGroupOwner = MutableStateFlow(false)
    val isGroupOwner: StateFlow<Boolean> = _isGroupOwner

    private val _groupOwnerIp = MutableStateFlow<String?>(null)
    val groupOwnerIp: StateFlow<String?> = _groupOwnerIp

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager?.requestPeers(channel) { deviceList ->
                        _peers.value = deviceList?.deviceList?.toList() ?: emptyList()
                        Log.d(TAG, "Peers updated: ${_peers.value.size}")
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    manager?.requestConnectionInfo(channel) { info ->
                        if (info.groupFormed) {
                            _isGroupOwner.value = info.isGroupOwner
                            val ip = info.groupOwnerAddress?.hostAddress
                            if (ip != null) {
                                _groupOwnerIp.value = ip
                                Log.d(TAG, "IP получен: $ip (isGO=${info.isGroupOwner})")
                            }
                        }
                    }
                }
            }
        }
    }

    fun init() {
        channel = manager?.initialize(context, context.mainLooper, null)
        context.registerReceiver(receiver, intentFilter)
    }

    fun createGroup(onSuccess: () -> Unit) {
        manager?.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Group created successfully")
                _isGroupOwner.value = true
                _groupOwnerIp.value = "192.168.49.1" // Стандартный IP GO в Android
                onSuccess()
            }
            override fun onFailure(reason: Int) {
                Log.e(TAG, "createGroup failed: $reason")
                _connectionError.value = "Не удалось создать группу (код $reason)"
            }
        })
    }

    fun discoverPeers() {
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Peer discovery started")
            }
            override fun onFailure(reason: Int) {
                Log.e(TAG, "discoverPeers failed: $reason")
                _connectionError.value = "Поиск не удался (код $reason)"
            }
        })
    }

    /** Клиент: подключается к выбранному устройству. Вызывает onInitiated при успешном запросе. */
    fun connectToDevice(device: WifiP2pDevice, onInitiated: () -> Unit) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            groupOwnerIntent = 0
        }
        manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Connection initiated to ${device.deviceName}")
                onInitiated() // <-- Вызываем колбэк при успехе
            }
            override fun onFailure(reason: Int) {
                Log.e(TAG, "connect failed: $reason")
                _connectionError.value = "Подключение не удалось (код $reason)"
            }
        })
    }

    fun disconnect() {
        manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() { Log.d(TAG, "Group removed") }
            override fun onFailure(reason: Int) { Log.e(TAG, "removeGroup failed: $reason") }
        })
    }

    fun release() {
        context.unregisterReceiver(receiver)
        channel?.close()
    }
}