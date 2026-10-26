package com.example.network

import android.util.Log
import com.example.model.NetworkPacket
import com.example.model.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class BingoClient(
    val hostIp: String,
    val port: Int = NetworkConstants.DEFAULT_SERVER_PORT,
    val localPlayer: PlayerState,
    val password: String = ""
) {
    private val tag = "BingoClient"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var socket: Socket? = null
    private val outChannel = Channel<String>(Channel.UNLIMITED)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _latencyMs = MutableStateFlow(0L)
    val latencyMs: StateFlow<Long> = _latencyMs.asStateFlow()

    private val _events = MutableSharedFlow<NetworkPacket>(extraBufferCapacity = 64)
    val events: SharedFlow<NetworkPacket> = _events.asSharedFlow()

    private var pingJob: Job? = null

    fun connect() {
        scope.launch {
            _isConnecting.value = true
            _errorMessage.value = null
            try {
                val cleanHost = hostIp.trim()
                if (cleanHost.isBlank()) {
                    throw IllegalArgumentException("Host IP address cannot be empty.")
                }

                Log.d(tag, "Connecting to $cleanHost:$port...")
                val s = Socket()
                s.connect(InetSocketAddress(cleanHost, port), NetworkConstants.SOCKET_TIMEOUT_MS)
                s.soTimeout = 0 // continuous stream
                s.tcpNoDelay = true
                socket = s

                val reader = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.UTF_8))
                val writer = PrintWriter(s.getOutputStream(), true)

                _isConnected.value = true
                _isConnecting.value = false

                // Send Handshake
                val handshake = NetworkPacket.Handshake(
                    playerId = localPlayer.id,
                    playerName = localPlayer.name,
                    avatarIndex = localPlayer.avatarIndex,
                    isHost = false,
                    password = password
                )
                writer.println(handshake.toJsonString())

                // Writer Job
                val writeJob = launch {
                    for (msg in outChannel) {
                        try {
                            writer.println(msg)
                        } catch (e: Exception) {
                            Log.w(tag, "Failed to write packet", e)
                            break
                        }
                    }
                }

                // Start Latency Ping
                startPingLoop()

                // Reader loop
                while (isActive && s.isConnected && !s.isClosed) {
                    val line = reader.readLine() ?: break
                    val packet = NetworkPacket.fromJsonString(line) ?: continue

                    when (packet) {
                        is NetworkPacket.Pong -> {
                            val rtt = System.currentTimeMillis() - packet.timestamp
                            _latencyMs.value = (rtt / 2).coerceAtLeast(1)
                        }
                        else -> {
                            _events.emit(packet)
                        }
                    }
                }

                writeJob.cancel()
            } catch (e: Exception) {
                Log.w(tag, "Client connection error: ${e.javaClass.simpleName}: ${e.message}")
                val userMsg = when (e) {
                    is ConnectException -> "Could not connect to host at $hostIp:$port. Ensure the host has created a lobby and is on the same Wi-Fi or Hotspot."
                    is SocketTimeoutException -> "Connection timed out to $hostIp:$port. Please verify network connectivity."
                    is UnknownHostException -> "Host IP '$hostIp' is unreachable or invalid."
                    is IllegalArgumentException -> e.message ?: "Invalid host settings."
                    else -> "Connection failed: ${e.localizedMessage ?: "Network error"}"
                }
                _errorMessage.value = userMsg
            } finally {
                _isConnected.value = false
                _isConnecting.value = false
                pingJob?.cancel()
                try {
                    socket?.close()
                } catch (_: Exception) {}
            }
        }
    }

    fun sendPacket(packet: NetworkPacket) {
        outChannel.trySend(packet.toJsonString())
    }

    private fun startPingLoop() {
        pingJob = scope.launch {
            while (isActive) {
                delay(4000)
                if (_isConnected.value) {
                    sendPacket(NetworkPacket.Ping(System.currentTimeMillis()))
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun disconnect() {
        _isConnected.value = false
        _isConnecting.value = false
        pingJob?.cancel()
        try {
            socket?.close()
        } catch (_: Exception) {}
        scope.cancel()
    }
}
