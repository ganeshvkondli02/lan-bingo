package com.example.network

import android.util.Log
import com.example.model.GameMode
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

class BingoServer(
    val port: Int = NetworkConstants.DEFAULT_SERVER_PORT,
    val hostPlayer: PlayerState,
    gameMode: GameMode = GameMode.CLASSIC_5X5,
    val roomPassword: String = ""
) {
    var gameMode: GameMode = gameMode
        private set
    private val tag = "BingoServer"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var serverSocket: ServerSocket? = null
    private val clients = ConcurrentHashMap<String, ClientSession>()
    private val mutex = Mutex()

    // Game State
    private val _players = MutableStateFlow<List<PlayerState>>(listOf(hostPlayer))
    val players: StateFlow<List<PlayerState>> = _players.asStateFlow()

    private val _events = MutableSharedFlow<NetworkPacket>(extraBufferCapacity = 64)
    val events: SharedFlow<NetworkPacket> = _events.asSharedFlow()

    private val playerLinesCount = ConcurrentHashMap<String, Int>()
    private val calledNumbers = mutableSetOf<Int>()
    private var turnOrder: List<String> = emptyList()
    private var currentTurnIndex: Int = 0
    private var isGameInProgress: Boolean = false

    private var heartbeatJob: Job? = null

    init {
        playerLinesCount[hostPlayer.id] = 0
    }

    private class ClientSession(
        val playerId: String,
        val socket: Socket,
        val writer: PrintWriter,
        val outChannel: Channel<String> = Channel(Channel.UNLIMITED)
    )

    fun start() {
        scope.launch {
            try {
                val server = ServerSocket(port).apply {
                    reuseAddress = true
                }
                serverSocket = server
                Log.d(tag, "BingoServer started on port $port")

                startHeartbeat()

                while (isActive && !server.isClosed) {
                    val clientSocket = server.accept()
                    scope.launch {
                        handleNewConnection(clientSocket)
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    Log.e(tag, "Server error", e)
                }
            }
        }
    }

    private suspend fun handleNewConnection(socket: Socket) {
        var registeredPlayerId: String? = null
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
            val writer = PrintWriter(socket.getOutputStream(), true)

            // Read initial handshake line
            val handshakeLine = reader.readLine() ?: return
            val packet = NetworkPacket.fromJsonString(handshakeLine)

            if (packet is NetworkPacket.Handshake) {
                // Validate Room Password
                if (roomPassword.isNotEmpty() && roomPassword != packet.password) {
                    val rejectPacket = NetworkPacket.RoomDisbanded("Incorrect room password.")
                    writer.println(rejectPacket.toJsonString())
                    socket.close()
                    return
                }

                registeredPlayerId = packet.playerId
                val session = ClientSession(packet.playerId, socket, writer)
                clients[packet.playerId] = session

                // Start writer coroutine
                val writeJob = scope.launch {
                    try {
                        for (msg in session.outChannel) {
                            writer.println(msg)
                        }
                    } catch (_: Exception) {}
                }

                mutex.withLock {
                    val newPlayer = PlayerState(
                        id = packet.playerId,
                        name = packet.playerName,
                        isHost = false,
                        avatarIndex = packet.avatarIndex,
                        completedLinesCount = 0,
                        isReady = false,
                        isOffline = false,
                        isEliminated = false
                    )
                    playerLinesCount[packet.playerId] = 0
                    val currentList = _players.value.filterNot { it.id == packet.playerId } + newPlayer
                    _players.value = currentList

                    broadcastLobbyUpdate()
                }

                _events.emit(packet)

                // Read incoming loop
                while (scope.isActive && socket.isConnected && !socket.isClosed) {
                    val line = reader.readLine() ?: break
                    val incoming = NetworkPacket.fromJsonString(line) ?: continue
                    handleIncomingPacket(session.playerId, incoming)
                }

                writeJob.cancel()
            }
        } catch (e: Exception) {
            Log.d(tag, "Client connection ended: ${e.message}")
        } finally {
            registeredPlayerId?.let { pid ->
                handleClientDisconnected(pid)
            }
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private suspend fun handleIncomingPacket(senderId: String, packet: NetworkPacket) {
        when (packet) {
            is NetworkPacket.ReadyToggle -> {
                mutex.withLock {
                    val updated = _players.value.map { p ->
                        if (p.id == packet.playerId) p.copy(isReady = packet.isReady) else p
                    }
                    _players.value = updated
                    broadcastLobbyUpdate()
                }
                _events.emit(packet)
            }
            is NetworkPacket.PlayerForfeited -> {
                handlePlayerForfeit(packet.playerId)
            }
            is NetworkPacket.NumberMarked -> {
                onPlayerMarkedNumber(packet.playerId, packet.number)
            }
            is NetworkPacket.LineCompleted -> {
                onPlayerCompletedLine(packet.playerId, packet.totalLinesCount)
            }
            is NetworkPacket.ChatReaction -> {
                broadcast(packet)
                _events.emit(packet)
            }
            is NetworkPacket.Ping -> {
                sendToClient(senderId, NetworkPacket.Pong(packet.timestamp))
            }
            is NetworkPacket.RematchRequest -> {
                _events.emit(packet)
            }
            is NetworkPacket.Heartbeat -> {
                // Heartbeat ack
            }
            else -> {
                _events.emit(packet)
            }
        }
    }

    suspend fun onPlayerMarkedNumber(playerId: String, number: Int) {
        mutex.withLock {
            if (!isGameInProgress) return@withLock
            if (turnOrder.isEmpty()) return@withLock

            val expectedTurnPlayerId = turnOrder.getOrNull(currentTurnIndex)
            if (expectedTurnPlayerId != playerId) {
                Log.w(tag, "Rejected move from $playerId, expected $expectedTurnPlayerId")
                return@withLock
            }

            if (calledNumbers.contains(number)) {
                Log.w(tag, "Number $number already called!")
                return@withLock
            }

            calledNumbers.add(number)

            // Reset missed turns for this player
            _players.value = _players.value.map {
                if (it.id == playerId) it.copy(consecutiveMissedTurns = 0) else it
            }

            // Advance turn in circular order to next non-eliminated player
            advanceToNextActiveTurn()
            val nextTurnPlayerId = turnOrder.getOrNull(currentTurnIndex) ?: ""

            val broadcastPacket = NetworkPacket.NumberMarked(
                playerId = playerId,
                number = number,
                timestamp = System.currentTimeMillis(),
                nextTurnPlayerId = nextTurnPlayerId
            )
            broadcast(broadcastPacket)
            _events.emit(broadcastPacket)

            // Check simultaneous win condition
            checkGameWinners()
        }
    }

    private fun advanceToNextActiveTurn() {
        if (turnOrder.isEmpty()) return
        val total = turnOrder.size
        for (i in 1..total) {
            val candidateIndex = (currentTurnIndex + i) % total
            val candidateId = turnOrder[candidateIndex]
            val player = _players.value.find { it.id == candidateId }
            if (player != null && !player.isEliminated) {
                currentTurnIndex = candidateIndex
                return
            }
        }
    }

    fun processPlayerMove(playerId: String, number: Int) {
        scope.launch {
            onPlayerMarkedNumber(playerId, number)
        }
    }

    suspend fun onPlayerCompletedLine(playerId: String, totalLines: Int) {
        mutex.withLock {
            playerLinesCount[playerId] = totalLines
            val updated = _players.value.map { p ->
                if (p.id == playerId) p.copy(completedLinesCount = totalLines) else p
            }
            _players.value = updated

            val currentTurnId = turnOrder.getOrNull(currentTurnIndex)
            broadcast(NetworkPacket.LobbyUpdate(_players.value, gameMode, currentTurnId))

            checkGameWinners()
        }
    }

    fun reportLineCompleted(playerId: String, totalLines: Int) {
        scope.launch {
            onPlayerCompletedLine(playerId, totalLines)
        }
    }

    private suspend fun checkGameWinners() {
        val activePlayers = _players.value.filter { !it.isEliminated }
        if (activePlayers.size <= 1 && _players.value.size > 1 && isGameInProgress) {
            // Only 1 player remains! Remaining active player wins
            val lastStanding = activePlayers.firstOrNull() ?: _players.value.first()
            declareWinner(listOf(lastStanding))
            return
        }

        val winners = mutableListOf<PlayerState>()
        for (player in activePlayers) {
            val lines = playerLinesCount[player.id] ?: 0
            if (lines >= gameMode.linesToWin) {
                winners.add(player)
            }
        }

        if (winners.isNotEmpty()) {
            declareWinner(winners)
        }
    }

    private suspend fun declareWinner(winners: List<PlayerState>) {
        isGameInProgress = false
        val winnerIds = winners.map { it.id }
        val winnerNames = winners.map { it.name }
        val scores = playerLinesCount.toMap()

        _players.value = _players.value.map {
            if (it.isHost || it.isAi) it.copy(isReady = true) else it.copy(isReady = false)
        }

        val gameOverPacket = NetworkPacket.GameOver(
            winnerIds = winnerIds,
            winnerNames = winnerNames,
            finalScores = scores
        )
        broadcast(gameOverPacket)
        broadcastLobbyUpdate()
        _events.emit(gameOverPacket)
    }

    fun startGame(mode: GameMode = this.gameMode) {
        scope.launch {
            mutex.withLock {
                this@BingoServer.gameMode = mode
                isGameInProgress = true
                calledNumbers.clear()

                for (p in _players.value) {
                    playerLinesCount[p.id] = 0
                }
                _players.value = _players.value.map {
                    it.copy(
                        completedLinesCount = 0,
                        isWinner = false,
                        isEliminated = false,
                        consecutiveMissedTurns = 0
                    )
                }

                val seed = SecureRandom().nextLong()
                turnOrder = _players.value.map { it.id }.shuffled(kotlin.random.Random(seed))
                currentTurnIndex = 0
                val firstTurnPlayerId = turnOrder.firstOrNull() ?: hostPlayer.id

                val startPacket = NetworkPacket.GameStart(
                    seed = seed,
                    mode = mode,
                    turnOrder = turnOrder,
                    currentTurnPlayerId = firstTurnPlayerId
                )

                broadcast(startPacket)
                _events.emit(startPacket)
            }
        }
    }

    fun startRematch() {
        startGame(this.gameMode)
    }

    fun returnToLobby() {
        scope.launch {
            mutex.withLock {
                isGameInProgress = false
                broadcastLobbyUpdate()
            }
        }
    }

    fun updateGameMode(mode: GameMode) {
        this.gameMode = mode
        scope.launch {
            mutex.withLock {
                broadcastLobbyUpdate()
            }
        }
    }

    fun addAiPlayer(aiName: String = "Bot Alpha", avatarIdx: Int = 1) {
        scope.launch {
            mutex.withLock {
                val aiId = "ai_${System.currentTimeMillis()}"
                val aiPlayer = PlayerState(
                    id = aiId,
                    name = aiName,
                    isHost = false,
                    avatarIndex = avatarIdx,
                    completedLinesCount = 0,
                    isReady = true,
                    isAi = true
                )
                playerLinesCount[aiId] = 0
                _players.value = _players.value + aiPlayer
                broadcastLobbyUpdate()
            }
        }
    }

    fun removeAiPlayer(aiId: String) {
        scope.launch {
            mutex.withLock {
                playerLinesCount.remove(aiId)
                _players.value = _players.value.filterNot { it.id == aiId }
                broadcastLobbyUpdate()
            }
        }
    }

    fun kickPlayer(playerId: String, reason: String = "You were removed from the room by the host.") {
        scope.launch {
            mutex.withLock {
                val kickPacket = NetworkPacket.KickPlayer(playerId, reason)
                sendToClient(playerId, kickPacket)
                val session = clients.remove(playerId)
                try {
                    session?.socket?.close()
                } catch (_: Exception) {}

                playerLinesCount.remove(playerId)
                _players.value = _players.value.filterNot { it.id == playerId }
                broadcastLobbyUpdate()
            }
        }
    }

    fun handlePlayerForfeit(playerId: String) {
        scope.launch {
            mutex.withLock {
                if (isGameInProgress) {
                    val pName = _players.value.find { it.id == playerId }?.name ?: "Player"
                    _players.value = _players.value.map {
                        if (it.id == playerId) it.copy(isEliminated = true, isOffline = true) else it
                    }

                    // Advance turn if it was their turn
                    val currentTurnPlayerId = turnOrder.getOrNull(currentTurnIndex)
                    if (currentTurnPlayerId == playerId) {
                        advanceToNextActiveTurn()
                    }

                    val forfeitPacket = NetworkPacket.PlayerForfeited(playerId, pName)
                    broadcast(forfeitPacket)
                    _events.emit(forfeitPacket)
                    broadcastLobbyUpdate()

                    checkGameWinners()
                } else {
                    removeClient(playerId)
                }
            }
        }
    }

    private suspend fun handleClientDisconnected(playerId: String) {
        mutex.withLock {
            if (isGameInProgress) {
                _players.value = _players.value.map {
                    if (it.id == playerId) it.copy(isOffline = true) else it
                }
                broadcastLobbyUpdate()
            } else {
                clients.remove(playerId)
                playerLinesCount.remove(playerId)
                _players.value = _players.value.filterNot { it.id == playerId }
                broadcastLobbyUpdate()
            }
        }
    }

    private suspend fun removeClient(playerId: String) {
        clients.remove(playerId)
        playerLinesCount.remove(playerId)
        _players.value = _players.value.filterNot { it.id == playerId }
        broadcastLobbyUpdate()
    }

    private suspend fun broadcastLobbyUpdate() {
        val currentTurnId = turnOrder.getOrNull(currentTurnIndex)
        val packet = NetworkPacket.LobbyUpdate(_players.value, gameMode, currentTurnId)
        broadcast(packet)
    }

    fun broadcast(packet: NetworkPacket) {
        val json = packet.toJsonString()
        for ((_, session) in clients) {
            session.outChannel.trySend(json)
        }
    }

    private fun sendToClient(playerId: String, packet: NetworkPacket) {
        val session = clients[playerId] ?: return
        session.outChannel.trySend(packet.toJsonString())
    }

    private fun startHeartbeat() {
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(NetworkConstants.HEARTBEAT_INTERVAL_MS)
                val hb = NetworkPacket.Heartbeat(System.currentTimeMillis())
                broadcast(hb)
            }
        }
    }

    fun stop() {
        heartbeatJob?.cancel()
        isGameInProgress = false
        val disbandPacket = NetworkPacket.RoomDisbanded("The host has closed the room.")
        broadcast(disbandPacket)
        for ((_, session) in clients) {
            try {
                session.socket.close()
            } catch (_: Exception) {}
        }
        clients.clear()
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        scope.cancel()
    }
}
