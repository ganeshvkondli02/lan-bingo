package com.example.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.example.model.GameMode
import com.example.model.LobbyDiscoveryInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

class DiscoveryEngine(
    private val context: Context
) {
    private val tag = "DiscoveryEngine"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val nsdManager: NsdManager? =
        context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private var multicastLock: WifiManager.MulticastLock? = null

    private val _discoveredLobbies = MutableStateFlow<List<LobbyDiscoveryInfo>>(emptyList())
    val discoveredLobbies: StateFlow<List<LobbyDiscoveryInfo>> = _discoveredLobbies.asStateFlow()

    private val lobbyMap = ConcurrentHashMap<String, LobbyDiscoveryInfo>()

    // Host advertising state
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var udpBroadcasterJob: Job? = null

    // Client discovery state
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var udpListenerJob: Job? = null
    private var pruneJob: Job? = null
    private var udpReceiverSocket: DatagramSocket? = null

    private var currentHostedGameId: String? = null

    /**
     * Start advertising a hosted game via both NSD and UDP Broadcast
     */
    fun startHosting(
        gameId: String,
        roomCode: String = "",
        hostName: String,
        port: Int,
        mode: GameMode,
        playerCount: Int,
        maxPlayers: Int,
        hasPassword: Boolean = false
    ) {
        stopDiscovery()
        stopHosting()

        currentHostedGameId = gameId
        acquireMulticastLock()

        // 1. NSD Registration
        try {
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = "${NetworkConstants.NSD_SERVICE_NAME_PREFIX}$hostName-$gameId"
                serviceType = NetworkConstants.NSD_SERVICE_TYPE
                setPort(port)
            }

            val listener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                    Log.d(tag, "NSD Service registered: ${NsdServiceInfo.serviceName}")
                }
                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.e(tag, "NSD Registration failed: $errorCode")
                }
                override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                    Log.d(tag, "NSD Service unregistered")
                }
                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.e(tag, "NSD Unregistration failed: $errorCode")
                }
            }
            registrationListener = listener
            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (e: Exception) {
            Log.e(tag, "Failed to register NSD service", e)
        }

        // 2. UDP Subnet Beacon
        udpBroadcasterJob = scope.launch {
            var broadcastSocket: DatagramSocket? = null
            try {
                broadcastSocket = DatagramSocket()
                broadcastSocket.broadcast = true

                while (isActive) {
                    val localIp = NetworkUtils.getLocalIpAddress() ?: "127.0.0.1"
                    val payload = JSONObject().apply {
                        put("type", "ROOM_BEACON")
                        put("gameId", gameId)
                        put("roomCode", roomCode)
                        put("hostName", hostName)
                        put("hostIp", localIp)
                        put("port", port)
                        put("mode", mode.name)
                        put("playerCount", playerCount)
                        put("maxPlayers", maxPlayers)
                        put("hasPassword", hasPassword)
                    }.toString()

                    val bytes = payload.toByteArray(Charsets.UTF_8)
                    val targets = NetworkUtils.getBroadcastAddresses()

                    for (target in targets) {
                        try {
                            val packet = DatagramPacket(
                                bytes,
                                bytes.size,
                                target,
                                NetworkConstants.DISCOVERY_PORT
                            )
                            broadcastSocket.send(packet)
                        } catch (_: Exception) {}
                    }

                    delay(NetworkConstants.DISCOVERY_BROADCAST_INTERVAL_MS)
                }
            } catch (e: Exception) {
                Log.e(tag, "UDP broadcast error", e)
            } finally {
                broadcastSocket?.close()
            }
        }
    }

    /**
     * Stop advertising as host
     */
    fun stopHosting() {
        val gameId = currentHostedGameId
        if (gameId != null) {
            scope.launch {
                try {
                    val broadcastSocket = DatagramSocket()
                    broadcastSocket.broadcast = true
                    val payload = JSONObject().apply {
                        put("type", "ROOM_CLOSED")
                        put("gameId", gameId)
                    }.toString()
                    val bytes = payload.toByteArray(Charsets.UTF_8)
                    val targets = NetworkUtils.getBroadcastAddresses()
                    for (target in targets) {
                        try {
                            val packet = DatagramPacket(bytes, bytes.size, target, NetworkConstants.DISCOVERY_PORT)
                            broadcastSocket.send(packet)
                        } catch (_: Exception) {}
                    }
                    broadcastSocket.close()
                } catch (_: Exception) {}
            }
            currentHostedGameId = null
        }

        udpBroadcasterJob?.cancel()
        udpBroadcasterJob = null

        registrationListener?.let { listener ->
            try {
                nsdManager?.unregisterService(listener)
            } catch (e: Exception) {
                Log.e(tag, "Error unregistering NSD service", e)
            }
        }
        registrationListener = null
        releaseMulticastLock()
    }

    /**
     * Start discovering available LAN lobbies
     */
    fun startDiscovery() {
        stopDiscovery()
        stopHosting()

        lobbyMap.clear()
        _discoveredLobbies.value = emptyList()

        acquireMulticastLock()

        // 1. NSD Discovery
        try {
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {
                    Log.d(tag, "NSD Discovery started for $regType")
                }

                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    Log.d(tag, "NSD Service found: ${serviceInfo.serviceName}")
                    if (serviceInfo.serviceType == NetworkConstants.NSD_SERVICE_TYPE ||
                        serviceInfo.serviceType.contains("lanbingo")
                    ) {
                        resolveNsdService(serviceInfo)
                    }
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                    Log.d(tag, "NSD Service lost: ${serviceInfo.serviceName}")
                    val keyToRemove = lobbyMap.keys.find { it.contains(serviceInfo.serviceName) }
                    if (keyToRemove != null) {
                        lobbyMap.remove(keyToRemove)
                        _discoveredLobbies.value = lobbyMap.values.toList()
                    }
                }

                override fun onDiscoveryStopped(serviceType: String) {
                    Log.d(tag, "NSD Discovery stopped")
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e(tag, "NSD Discovery start failed: $errorCode")
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e(tag, "NSD Discovery stop failed: $errorCode")
                }
            }
            discoveryListener = listener
            nsdManager?.discoverServices(
                NetworkConstants.NSD_SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD,
                listener
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to start NSD discovery", e)
        }

        // 2. UDP Broadcast Listener
        udpListenerJob = scope.launch {
            try {
                val socket = DatagramSocket(NetworkConstants.DISCOVERY_PORT).apply {
                    broadcast = true
                    reuseAddress = true
                }
                udpReceiverSocket = socket
                val buffer = ByteArray(2048)

                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)

                    val text = String(packet.data, 0, packet.length, Charsets.UTF_8)
                    try {
                        val json = JSONObject(text)
                        val msgType = json.optString("type", "ROOM_BEACON")
                        val gameId = json.getString("gameId")

                        if (msgType == "ROOM_CLOSED" || msgType == "DISBAND") {
                            if (lobbyMap.remove(gameId) != null) {
                                _discoveredLobbies.value = lobbyMap.values.toList()
                            }
                        } else {
                            val hostName = json.getString("hostName")
                            val reportedIp = json.optString("hostIp", packet.address.hostAddress ?: "")
                            val hostIp = if (reportedIp.isNotEmpty() && reportedIp != "127.0.0.1") {
                                reportedIp
                            } else {
                                packet.address.hostAddress ?: "127.0.0.1"
                            }
                            val port = json.getInt("port")
                            val modeStr = json.optString("mode", GameMode.CLASSIC_5X5.name)
                            val mode = try { GameMode.valueOf(modeStr) } catch (_: Exception) { GameMode.CLASSIC_5X5 }
                            val pCount = json.optInt("playerCount", 1)
                            val maxP = json.optInt("maxPlayers", mode.maxPlayers)
                            val hasPassword = json.optBoolean("hasPassword", false)
                            val roomCode = json.optString("roomCode", "")

                            val info = LobbyDiscoveryInfo(
                                gameId = gameId,
                                roomCode = roomCode,
                                hostName = hostName,
                                hostIp = hostIp,
                                port = port,
                                mode = mode,
                                playerCount = pCount,
                                maxPlayers = maxP,
                                hasPassword = hasPassword,
                                lastSeenTimestamp = System.currentTimeMillis()
                            )

                            lobbyMap[gameId] = info
                            _discoveredLobbies.value = lobbyMap.values.toList()
                        }
                    } catch (e: Exception) {
                        // ignore malformed UDP packet
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    Log.e(tag, "UDP discovery listener error", e)
                }
            } finally {
                udpReceiverSocket?.close()
                udpReceiverSocket = null
            }
        }

        // 3. Fast Stale Lobby Pruner (Prunes rooms absent for > 3.5s)
        pruneJob = scope.launch {
            while (isActive) {
                delay(2000)
                val now = System.currentTimeMillis()
                val iterator = lobbyMap.entries.iterator()
                var changed = false
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (now - entry.value.lastSeenTimestamp > 3500) {
                        iterator.remove()
                        changed = true
                    }
                }
                if (changed) {
                    _discoveredLobbies.value = lobbyMap.values.toList()
                }
            }
        }
    }

    private fun resolveNsdService(serviceInfo: NsdServiceInfo) {
        try {
            nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.e(tag, "NSD Resolve failed: $errorCode")
                }

                override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                    val host: InetAddress? = resolvedInfo.host
                    val hostIp = host?.hostAddress ?: return
                    val port = resolvedInfo.port
                    val name = resolvedInfo.serviceName

                    val parts = name.removePrefix(NetworkConstants.NSD_SERVICE_NAME_PREFIX).split("-")
                    val hostName = parts.getOrNull(0) ?: "Host"
                    val gameId = parts.getOrNull(1) ?: name

                    val info = LobbyDiscoveryInfo(
                        gameId = gameId,
                        hostName = hostName,
                        hostIp = hostIp,
                        port = port,
                        mode = GameMode.CLASSIC_5X5,
                        playerCount = 1,
                        maxPlayers = 4,
                        lastSeenTimestamp = System.currentTimeMillis()
                    )
                    lobbyMap[gameId] = info
                    _discoveredLobbies.value = lobbyMap.values.toList()
                }
            })
        } catch (e: Exception) {
            Log.e(tag, "Error initiating NSD resolve", e)
        }
    }

    /**
     * Stop discovering LAN lobbies
     */
    fun stopDiscovery() {
        udpListenerJob?.cancel()
        udpListenerJob = null

        pruneJob?.cancel()
        pruneJob = null

        try {
            udpReceiverSocket?.close()
        } catch (_: Exception) {}
        udpReceiverSocket = null

        discoveryListener?.let { listener ->
            try {
                nsdManager?.stopServiceDiscovery(listener)
            } catch (e: Exception) {
                Log.e(tag, "Error stopping NSD discovery", e)
            }
        }
        discoveryListener = null

        releaseMulticastLock()
    }

    private fun acquireMulticastLock() {
        if (multicastLock == null) {
            multicastLock = wifiManager?.createMulticastLock("LANBingoMulticastLock")?.apply {
                setReferenceCounted(true)
                acquire()
            }
        }
    }

    private fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
            multicastLock = null
        } catch (e: Exception) {
            Log.e(tag, "Error releasing multicast lock", e)
        }
    }
}
