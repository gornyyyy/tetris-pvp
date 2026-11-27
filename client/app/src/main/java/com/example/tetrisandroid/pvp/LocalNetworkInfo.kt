package com.example.tetrisandroid.pvp

import java.net.Inet4Address
import java.net.NetworkInterface

data class NetworkInfo(
    val ipAddress: String,
    val interfaceName: String,
    val displayName: String
)

/**
 * Возвращает список всех активных IPv4 адресов с указанием их источника
 * (Wi-Fi, Мобильная сеть, ZeroTier, WireGuard и т.д.)
 */
fun getLocalNetworkInfo(): List<NetworkInfo> {
    val networks = mutableListOf<NetworkInfo>()
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces().asSequence()
        for (intf in interfaces) {
            if (!intf.isUp || intf.isLoopback) continue

            val addrs = intf.inetAddresses.asSequence()
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    networks.add(NetworkInfo(
                        ipAddress = addr.hostAddress,
                        interfaceName = intf.name,
                        displayName = getFriendlyInterfaceName(intf.name)
                    ))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return networks.distinctBy { it.ipAddress }
}

/**
 * Преобразует техническое имя интерфейса (wlan0, zt0, tun0) в понятное название
 */
private fun getFriendlyInterfaceName(name: String): String {
    return when {
        name.startsWith("wlan") -> "Wi-Fi"
        name.startsWith("eth") -> "Ethernet (LAN)"
        name.startsWith("rmnet") || name.startsWith("ccmni") || name.startsWith("pdp") -> "Мобильная сеть"
        name.startsWith("zt") -> "ZeroTier (zt)"
        name.startsWith("tailscale") -> "Tailscale"
        name.startsWith("tun") -> "VPN / WireGuard (tun)"
        name.startsWith("tap") -> "VPN / Tap (tap)"
        name.startsWith("p2p") -> "Wi-Fi Direct / Точка доступа"
        name.startsWith("dummy") -> "Системный (dummy)"
        else -> name.uppercase()
    }
}