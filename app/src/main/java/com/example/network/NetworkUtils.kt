package com.example.network

import java.net.Inet4Address
import java.net.InetAddress
import java.net.InterfaceAddress
import java.net.NetworkInterface
import java.util.Collections

object NetworkUtils {

    /**
     * Retrieves the most likely local IPv4 address (Wi-Fi, Hotspot AP, or Ethernet).
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            // First look for wlan, ap, rndis, eth interfaces
            val preferredInterfaces = interfaces.filter { iface ->
                !iface.isLoopback && iface.isUp && (
                    iface.name.contains("wlan", ignoreCase = true) ||
                    iface.name.contains("ap", ignoreCase = true) ||
                    iface.name.contains("rndis", ignoreCase = true) ||
                    iface.name.contains("eth", ignoreCase = true)
                )
            }

            for (iface in preferredInterfaces) {
                val addresses = Collections.list(iface.inetAddresses)
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }

            // Fallback to any non-loopback IPv4 interface
            for (iface in interfaces) {
                if (iface.isUp && !iface.isLoopback) {
                    val addresses = Collections.list(iface.inetAddresses)
                    for (addr in addresses) {
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            return addr.hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Retrieves all valid broadcast addresses for available network interfaces.
     */
    fun getBroadcastAddresses(): List<InetAddress> {
        val broadcastList = mutableListOf<InetAddress>()
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (iface in interfaces) {
                if (iface.isLoopback || !iface.isUp) continue
                for (interfaceAddress in iface.interfaceAddresses) {
                    val broadcast = interfaceAddress.broadcast
                    if (broadcast != null && broadcast is Inet4Address) {
                        broadcastList.add(broadcast)
                    }
                }
            }
            if (broadcastList.isEmpty()) {
                broadcastList.add(InetAddress.getByName("255.255.255.255"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                broadcastList.add(InetAddress.getByName("255.255.255.255"))
            } catch (_: Exception) {}
        }
        return broadcastList
    }
}
