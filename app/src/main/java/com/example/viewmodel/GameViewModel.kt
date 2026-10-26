package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioFeedbackEngine
import com.example.model.BingoBoard
import com.example.model.CompletedLine
import com.example.model.DifficultyLevel
import com.example.model.GameMode
import com.example.model.LobbyDiscoveryInfo
import com.example.model.NetworkPacket
import com.example.model.PlayerState
import com.example.network.BingoClient
import com.example.network.BingoServer
import com.example.network.DiscoveryEngine
import com.example.network.NetworkConstants
import com.example.network.NetworkUtils
import com.example.model.MatchRecord
import com.example.model.PlayerCareerStats
import com.example.model.ProfilePreferencesManager
import com.example.model.StatsManager
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class AppScreen {
    HOME,
    HOST_LOBBY,
    CLIENT_LOBBY,
    GAME_PLAY
}

data class ReactionPopup(
    val id: String = UUID.randomUUID().toString(),
    val playerName: String,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val localPlayer: PlayerState,
    val selectedGameMode: GameMode = GameMode.CLASSIC_5X5,
    val selectedDifficulty: DifficultyLevel = DifficultyLevel.LOW,
    val isHost: Boolean = false,
    val isSoloPractice: Boolean = false,
    val isLocalPlayerReady: Boolean = false,
    val roomAlertMessage: String? = null,

    // Networking
    val hostLocalIp: String? = null,
    val hostRoomCode: String = "",
    val hostRoomPassword: String = "",
    val discoveredLobbies: List<LobbyDiscoveryInfo> = emptyList(),
    val isSearchingLobbies: Boolean = false,
    val isConnecting: Boolean = false,
    val connectionError: String? = null,
    val pingMs: Long = 0L,

    // Lobby & Players
    val players: List<PlayerState> = emptyList(),

    // Board & Gameplay
    val board: BingoBoard? = null,
    val boardNumbers: List<Int> = emptyList(),
    val markedIndices: Set<Int> = emptySet(),
    val completedLines: List<CompletedLine> = emptyList(),
    val winningCellIndices: Set<Int> = emptySet(),
    val illuminatedLetters: String = "",

    // Turns, Timer & History
    val turnOrder: List<String> = emptyList(),
    val currentTurnPlayerId: String = "",
    val isMyTurn: Boolean = false,
    val turnTimerSeconds: Int = 10,
    val lastCalledNumber: Int? = null,
    val calledNumbersHistory: List<Int> = emptyList(),

    // Game Over & Celebration
    val isGameOver: Boolean = false,
    val winnerIds: List<String> = emptyList(),
    val winnerNames: List<String> = emptyList(),
    val isLocalPlayerWinner: Boolean = false,
    val isTieGame: Boolean = false,
    val finalScores: Map<String, Int> = emptyMap(),
    val isHostLeft: Boolean = false,

    // Reactions
    val activeReactions: List<ReactionPopup> = emptyList(),

    // Stats, History & Themes
    val careerStats: PlayerCareerStats = PlayerCareerStats(),
    val matchHistory: List<MatchRecord> = emptyList(),
    val selectedTheme: AppTheme = AppTheme.VIBRANT_LIME,

    // Settings
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val audioEngine = AudioFeedbackEngine(application.applicationContext)
    private val discoveryEngine = DiscoveryEngine(application.applicationContext)
    private val statsManager = StatsManager(application.applicationContext)
    private val profilePrefsManager = ProfilePreferencesManager(application.applicationContext)

    private var server: BingoServer? = null
    private var client: BingoClient? = null

    private var serverEventsJob: Job? = null
    private var clientEventsJob: Job? = null
    private var aiTurnJob: Job? = null
    private var moveTimerJob: Job? = null
    private var gameStartTime: Long = System.currentTimeMillis()
    private val aiBoards = ConcurrentHashMap<String, BingoBoard>()

    private val _uiState = MutableStateFlow(
        GameUiState(
            localPlayer = PlayerState(
                id = profilePrefsManager.getPlayerId(),
                name = profilePrefsManager.getPlayerName(),
                avatarIndex = profilePrefsManager.getAvatarIndex(),
                customAvatarPath = profilePrefsManager.getCustomAvatarPath()
            ),
            selectedGameMode = GameMode.CLASSIC_5X5,
            selectedDifficulty = DifficultyLevel.LOW,
            careerStats = statsManager.loadStats(),
            matchHistory = statsManager.loadMatchHistory(),
            selectedTheme = profilePrefsManager.getTheme(),
            isSoundEnabled = profilePrefsManager.isSoundEnabled(),
            isHapticsEnabled = profilePrefsManager.isHapticsEnabled()
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _lineStrikeTrigger = MutableSharedFlow<CompletedLine>(extraBufferCapacity = 8)
    val lineStrikeTrigger: SharedFlow<CompletedLine> = _lineStrikeTrigger.asSharedFlow()

    private val _letterUnlockTrigger = MutableSharedFlow<Char>(extraBufferCapacity = 8)
    val letterUnlockTrigger: SharedFlow<Char> = _letterUnlockTrigger.asSharedFlow()

    init {
        // Synchronize audio engine with saved settings
        audioEngine.isSoundEnabled = profilePrefsManager.isSoundEnabled()
        audioEngine.isHapticsEnabled = profilePrefsManager.isHapticsEnabled()

        // Observe discovered lobbies from DiscoveryEngine
        viewModelScope.launch {
            discoveryEngine.discoveredLobbies.collect { lobbies ->
                _uiState.update { it.copy(discoveredLobbies = lobbies) }
            }
        }
        initLocalBoard(GameMode.CLASSIC_5X5)
    }

    fun updatePlayerProfile(name: String, avatarIndex: Int) {
        val trimmed = name.trim().ifEmpty { "Player" }
        profilePrefsManager.savePlayerName(trimmed)
        profilePrefsManager.saveAvatarIndex(avatarIndex)
        if (avatarIndex >= 0) {
            profilePrefsManager.clearCustomAvatar()
        }
        _uiState.update { state ->
            val updatedLocal = state.localPlayer.copy(
                name = trimmed,
                avatarIndex = avatarIndex,
                customAvatarPath = if (avatarIndex >= 0) null else state.localPlayer.customAvatarPath
            )
            state.copy(localPlayer = updatedLocal)
        }
    }

    fun updatePlayerCustomPhoto(uri: Uri) {
        val savedPath = profilePrefsManager.saveCustomAvatarFromUri(uri)
        if (savedPath != null) {
            profilePrefsManager.saveAvatarIndex(-1)
            _uiState.update { state ->
                val updatedLocal = state.localPlayer.copy(
                    avatarIndex = -1,
                    customAvatarPath = savedPath
                )
                state.copy(localPlayer = updatedLocal)
            }
        }
    }

    fun selectCustomPhotoAvatar() {
        val path = profilePrefsManager.getCustomAvatarPath()
        if (path != null) {
            profilePrefsManager.saveAvatarIndex(-1)
            _uiState.update { state ->
                val updatedLocal = state.localPlayer.copy(
                    avatarIndex = -1,
                    customAvatarPath = path
                )
                state.copy(localPlayer = updatedLocal)
            }
        }
    }

    fun selectGameMode(mode: GameMode) {
        _uiState.update { it.copy(selectedGameMode = mode) }
        initLocalBoard(mode)
        server?.updateGameMode(mode)
    }

    fun shuffleBoard() {
        if (_uiState.value.currentScreen != AppScreen.GAME_PLAY) {
            initLocalBoard(_uiState.value.selectedGameMode)
            audioEngine.playMarkNumber()
        }
    }

    private fun initLocalBoard(mode: GameMode) {
        val newBoard = BingoBoard(mode)
        _uiState.update {
            it.copy(
                board = newBoard,
                boardNumbers = newBoard.numbers,
                markedIndices = emptySet(),
                completedLines = emptyList(),
                winningCellIndices = emptySet(),
                illuminatedLetters = ""
            )
        }
    }

    // ----------------------------------------------------
    // HOSTING FLOW
    // ----------------------------------------------------
    fun startHostingLobby(
        mode: GameMode = _uiState.value.selectedGameMode,
        difficulty: DifficultyLevel = _uiState.value.selectedDifficulty,
        password: String = ""
    ) {
        cleanupNetwork()
        initLocalBoard(mode)
        val currentLocal = _uiState.value.localPlayer.copy(isHost = true, isReady = true)
        val hostIp = NetworkUtils.getLocalIpAddress() ?: "127.0.0.1"

        val newServer = BingoServer(
            port = NetworkConstants.DEFAULT_SERVER_PORT,
            hostPlayer = currentLocal,
            gameMode = mode,
            roomPassword = password
        )
        server = newServer
        newServer.start()

        val generatedCode = "BNG-" + (1000..9999).random()

        discoveryEngine.startHosting(
            gameId = UUID.randomUUID().toString().take(6),
            roomCode = generatedCode,
            hostName = currentLocal.name,
            port = NetworkConstants.DEFAULT_SERVER_PORT,
            mode = mode,
            playerCount = 1,
            maxPlayers = mode.maxPlayers,
            hasPassword = password.isNotEmpty()
        )

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.HOST_LOBBY,
                isHost = true,
                isSoloPractice = false,
                isLocalPlayerReady = true,
                localPlayer = currentLocal,
                selectedGameMode = mode,
                selectedDifficulty = difficulty,
                hostLocalIp = hostIp,
                hostRoomCode = generatedCode,
                hostRoomPassword = password,
                players = listOf(currentLocal),
                connectionError = null,
                roomAlertMessage = null
            )
        }

        serverEventsJob = viewModelScope.launch {
            launch {
                newServer.players.collect { updatedPlayers ->
                    _uiState.update { it.copy(players = updatedPlayers) }
                }
            }
            launch {
                newServer.events.collect { packet ->
                    handleNetworkPacket(packet)
                }
            }
        }
    }

    fun addAiBot() {
        val currentCount = _uiState.value.players.size
        val maxAllowed = _uiState.value.selectedGameMode.maxPlayers
        if (currentCount < maxAllowed) {
            val botNames = listOf("Lucky Bot", "Blitz AI", "Bingo King", "Turbo", "Spark", "Shadow", "Ace")
            val botName = botNames.getOrElse(currentCount) { "Bot $currentCount" }
            server?.addAiPlayer(botName, (currentCount + 1) % 6)
            audioEngine.playMarkNumber()
        }
    }

    fun removeAiBot(aiId: String) {
        server?.removeAiPlayer(aiId)
    }

    fun kickPlayer(playerId: String) {
        if (_uiState.value.isHost) {
            server?.kickPlayer(playerId, "You were removed from the room by the host.")
        }
    }

    fun toggleReady() {
        val state = _uiState.value
        if (!state.isHost) {
            val newReady = !state.isLocalPlayerReady
            _uiState.update { it.copy(isLocalPlayerReady = newReady) }
            client?.sendPacket(NetworkPacket.ReadyToggle(state.localPlayer.id, newReady))
        }
    }

    fun startSoloPractice(
        mode: GameMode = _uiState.value.selectedGameMode,
        difficulty: DifficultyLevel = _uiState.value.selectedDifficulty
    ) {
        cleanupNetwork()
        initLocalBoard(mode)
        val currentLocal = _uiState.value.localPlayer.copy(isHost = true, isReady = true)

        val newServer = BingoServer(
            port = NetworkConstants.DEFAULT_SERVER_PORT,
            hostPlayer = currentLocal,
            gameMode = mode
        )
        server = newServer
        newServer.start()

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.HOST_LOBBY,
                isHost = true,
                isSoloPractice = true,
                isLocalPlayerReady = true,
                localPlayer = currentLocal,
                selectedGameMode = mode,
                selectedDifficulty = difficulty,
                hostLocalIp = "Solo Offline",
                hostRoomPassword = "",
                players = listOf(currentLocal),
                connectionError = null,
                roomAlertMessage = null
            )
        }

        serverEventsJob = viewModelScope.launch {
            launch {
                newServer.players.collect { updatedPlayers ->
                    _uiState.update { it.copy(players = updatedPlayers) }
                }
            }
            launch {
                newServer.events.collect { packet ->
                    handleNetworkPacket(packet)
                }
            }
        }

        // Auto-add an AI Bot for practice with the chosen difficulty tag
        val botName = when (difficulty) {
            DifficultyLevel.LOW -> "Casual Bot"
            DifficultyLevel.MEDIUM -> "Balanced Bot"
            DifficultyLevel.HIGH -> "Master Bot"
        }
        newServer.addAiPlayer(botName, 2)
    }

    fun updateDifficulty(difficulty: DifficultyLevel) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun startHostGame() {
        val state = _uiState.value
        if (state.players.size < state.selectedGameMode.minPlayers) return
        val allClientsReady = state.players.filterNot { it.isHost }.all { it.isReady }
        if (!allClientsReady) return
        server?.startGame(state.selectedGameMode)
    }

    private var discoveryTimeoutJob: Job? = null

    // ----------------------------------------------------
    // CLIENT JOINING FLOW
    // ----------------------------------------------------
    fun startSearchingLobbies(durationMs: Long = 5000L) {
        discoveryTimeoutJob?.cancel()
        cleanupNetwork()
        discoveryEngine.startDiscovery()
        _uiState.update {
            it.copy(
                isSearchingLobbies = true,
                connectionError = null
            )
        }
        discoveryTimeoutJob = viewModelScope.launch {
            kotlinx.coroutines.delay(durationMs)
            discoveryEngine.stopDiscovery()
            _uiState.update { it.copy(isSearchingLobbies = false) }
        }
    }

    fun refreshLobbies(durationMs: Long = 3000L) {
        discoveryTimeoutJob?.cancel()
        discoveryEngine.startDiscovery()
        _uiState.update {
            it.copy(
                isSearchingLobbies = true,
                connectionError = null
            )
        }
        discoveryTimeoutJob = viewModelScope.launch {
            kotlinx.coroutines.delay(durationMs)
            discoveryEngine.stopDiscovery()
            _uiState.update { it.copy(isSearchingLobbies = false) }
        }
    }

    fun stopSearchingLobbies() {
        discoveryTimeoutJob?.cancel()
        discoveryEngine.stopDiscovery()
        _uiState.update { it.copy(isSearchingLobbies = false) }
    }

    fun connectToLobby(
        hostIp: String,
        port: Int = NetworkConstants.DEFAULT_SERVER_PORT,
        password: String = ""
    ) {
        cleanupNetwork()
        _uiState.update {
            it.copy(
                isConnecting = true,
                connectionError = null,
                isLocalPlayerReady = false,
                roomAlertMessage = null
            )
        }

        val currentLocal = _uiState.value.localPlayer.copy(isHost = false, isReady = false)
        val newClient = BingoClient(
            hostIp = hostIp,
            port = port,
            localPlayer = currentLocal,
            password = password
        )
        client = newClient
        newClient.connect()

        clientEventsJob = viewModelScope.launch {
            launch {
                newClient.isConnecting.collect { connecting ->
                    _uiState.update { it.copy(isConnecting = connecting) }
                }
            }
            launch {
                var wasEverConnected = false
                newClient.isConnected.collect { connected ->
                    if (connected) {
                        wasEverConnected = true
                        _uiState.update {
                            it.copy(
                                currentScreen = if (it.currentScreen == AppScreen.HOME) AppScreen.CLIENT_LOBBY else it.currentScreen,
                                isHostLeft = false
                            )
                        }
                    } else if (wasEverConnected) {
                        val state = _uiState.value
                        if (state.currentScreen == AppScreen.GAME_PLAY) {
                            _uiState.update {
                                it.copy(
                                    isHostLeft = true,
                                    isLocalPlayerReady = false
                                )
                            }
                        } else if (state.currentScreen == AppScreen.CLIENT_LOBBY) {
                            cleanupNetwork()
                            _uiState.update {
                                it.copy(
                                    currentScreen = AppScreen.HOME,
                                    roomAlertMessage = "Host left or disconnected."
                                )
                            }
                        }
                    }
                }
            }
            launch {
                newClient.errorMessage.collect { errorMsg ->
                    if (errorMsg != null) {
                        _uiState.update { it.copy(connectionError = errorMsg, isConnecting = false) }
                    }
                }
            }
            launch {
                newClient.latencyMs.collect { ms ->
                    _uiState.update { it.copy(pingMs = ms) }
                }
            }
            launch {
                newClient.events.collect { packet ->
                    handleNetworkPacket(packet)
                }
            }
        }
    }

    fun connectByRoomCodeOrIp(
        rawInput: String,
        password: String = ""
    ) {
        val input = rawInput.trim()
        if (input.isEmpty()) {
            _uiState.update { it.copy(connectionError = "Please enter a valid room code or IP address.") }
            return
        }

        // 1. Check for QR Code URL format: bingo://join?ip=...&port=...&code=...
        if (input.startsWith("bingo://") || input.contains("ip=")) {
            try {
                val uri = Uri.parse(input)
                val ip = uri.getQueryParameter("ip")
                val port = uri.getQueryParameter("port")?.toIntOrNull() ?: NetworkConstants.DEFAULT_SERVER_PORT
                if (!ip.isNullOrBlank()) {
                    connectToLobby(hostIp = ip, port = port, password = password)
                    return
                }
            } catch (_: Exception) {}
        }

        // 2. Check if input matches any discovered lobby by roomCode, gameId, or hostIp
        val matchingLobby = _uiState.value.discoveredLobbies.firstOrNull { lobby ->
            lobby.roomCode.equals(input, ignoreCase = true) ||
            lobby.roomCode.removePrefix("BNG-").equals(input.removePrefix("BNG-"), ignoreCase = true) ||
            lobby.gameId.equals(input, ignoreCase = true) ||
            lobby.hostIp.equals(input, ignoreCase = true)
        }

        if (matchingLobby != null) {
            connectToLobby(
                hostIp = matchingLobby.hostIp,
                port = matchingLobby.port,
                password = password
            )
            return
        }

        // 3. Direct IP Address (e.g. 192.168.1.5 or 192.168.1.5:8888)
        if (input.contains(".") || input.split(".").size == 4) {
            val parts = input.split(":")
            val ip = parts[0].trim()
            val port = parts.getOrNull(1)?.toIntOrNull() ?: NetworkConstants.DEFAULT_SERVER_PORT
            connectToLobby(hostIp = ip, port = port, password = password)
            return
        }

        // 4. Subnet shortcut (e.g. "5" -> 192.168.1.5)
        val localIp = NetworkUtils.getLocalIpAddress()
        if (localIp != null && localIp.contains(".") && input.toIntOrNull() != null) {
            val prefix = localIp.substringBeforeLast(".")
            val targetIp = "$prefix.$input"
            connectToLobby(hostIp = targetIp, port = NetworkConstants.DEFAULT_SERVER_PORT, password = password)
            return
        }

        // 5. If not found in active discovery, trigger a scan and show feedback
        refreshLobbies(4000L)
        _uiState.update {
            it.copy(
                connectionError = "Room code '$input' not found nearby. Make sure the Host created a room on the same Wi-Fi."
            )
        }
    }

    fun dismissConnectionError() {
        _uiState.update { it.copy(connectionError = null) }
        client?.clearError()
    }

    fun dismissRoomAlert() {
        _uiState.update { it.copy(roomAlertMessage = null) }
        returnToHome()
    }

    // ----------------------------------------------------
    // PACKET HANDLING & GAMEPLAY SYNCHRONIZATION
    // ----------------------------------------------------
    private fun handleNetworkPacket(packet: NetworkPacket) {
        when (packet) {
            is NetworkPacket.LobbyUpdate -> {
                val state = _uiState.value
                if (state.selectedGameMode != packet.gameMode) {
                    initLocalBoard(packet.gameMode)
                }
                _uiState.update { s ->
                    s.copy(
                        players = packet.players,
                        selectedGameMode = packet.gameMode,
                        currentTurnPlayerId = packet.currentTurnPlayerId ?: s.currentTurnPlayerId
                    )
                }
            }
            is NetworkPacket.GameStart -> {
                onGameStarted(packet.mode, packet.turnOrder, packet.currentTurnPlayerId)
            }
            is NetworkPacket.NumberMarked -> {
                onNumberMarkedReceived(packet.playerId, packet.number, packet.nextTurnPlayerId)
            }
            is NetworkPacket.GameOver -> {
                onGameOverReceived(packet.winnerIds, packet.winnerNames, packet.finalScores)
            }
            is NetworkPacket.ChatReaction -> {
                onChatReactionReceived(packet.playerName, packet.emoji)
            }
            is NetworkPacket.RematchStart -> {
                onGameStarted(packet.mode, packet.turnOrder, packet.currentTurnPlayerId)
            }
            is NetworkPacket.RoomDisbanded -> {
                val state = _uiState.value
                if (state.currentScreen == AppScreen.GAME_PLAY) {
                    _uiState.update {
                        it.copy(
                            isHostLeft = true,
                            isLocalPlayerReady = false
                        )
                    }
                } else {
                    cleanupNetwork()
                    _uiState.update {
                        it.copy(
                            currentScreen = AppScreen.HOME,
                            roomAlertMessage = packet.reason ?: "Host disbanded the room."
                        )
                    }
                }
            }
            is NetworkPacket.KickPlayer -> {
                if (packet.playerId == _uiState.value.localPlayer.id) {
                    cleanupNetwork()
                    _uiState.update {
                        it.copy(
                            currentScreen = AppScreen.HOME,
                            roomAlertMessage = packet.reason
                        )
                    }
                }
            }
            is NetworkPacket.PlayerForfeited -> {
                onPlayerForfeitedReceived(packet.playerId, packet.playerName)
            }
            is NetworkPacket.PlayerOffline -> {
                onPlayerOfflineReceived(packet.playerId, packet.isOffline)
            }
            else -> {}
        }
    }

    private fun onPlayerForfeitedReceived(playerId: String, playerName: String) {
        onChatReactionReceived("Server", "⚠️ $playerName left match (Forfeit)")
    }

    private fun onPlayerOfflineReceived(playerId: String, isOffline: Boolean) {
        if (isOffline) {
            onChatReactionReceived("Server", "📡 A player is offline")
        }
    }

    private fun onGameStarted(mode: GameMode, turnOrder: List<String>, firstTurnPlayerId: String) {
        val newBoard = BingoBoard(mode)
        val isMyTurn = firstTurnPlayerId == _uiState.value.localPlayer.id
        gameStartTime = System.currentTimeMillis()

        aiBoards.clear()
        if (_uiState.value.isHost) {
            for (p in _uiState.value.players) {
                if (p.isAi) {
                    aiBoards[p.id] = BingoBoard(mode)
                }
            }
        }

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAME_PLAY,
                selectedGameMode = mode,
                board = newBoard,
                boardNumbers = newBoard.numbers,
                markedIndices = emptySet(),
                completedLines = emptyList(),
                winningCellIndices = emptySet(),
                illuminatedLetters = "",
                turnOrder = turnOrder,
                currentTurnPlayerId = firstTurnPlayerId,
                isMyTurn = isMyTurn,
                turnTimerSeconds = 10,
                lastCalledNumber = null,
                calledNumbersHistory = emptyList(),
                isGameOver = false,
                winnerIds = emptyList(),
                winnerNames = emptyList(),
                isLocalPlayerWinner = false,
                isTieGame = false,
                finalScores = emptyMap()
            )
        }

        if (isMyTurn) {
            audioEngine.playYourTurn()
        }

        startMoveTimer()
        checkAiTurn(firstTurnPlayerId)
    }

    fun onCellClicked(number: Int) {
        val state = _uiState.value
        if (state.currentScreen != AppScreen.GAME_PLAY) return
        if (state.isGameOver) return
        if (!state.isMyTurn) return
        if (state.calledNumbersHistory.contains(number)) return

        if (state.isHost) {
            // Host processes the move directly on the authoritative server
            server?.processPlayerMove(state.localPlayer.id, number)
        } else {
            // Client sends move packet to host
            val markedPacket = NetworkPacket.NumberMarked(
                playerId = state.localPlayer.id,
                number = number,
                timestamp = System.currentTimeMillis(),
                nextTurnPlayerId = ""
            )
            client?.sendPacket(markedPacket)
        }
    }

    private fun onNumberMarkedReceived(markingPlayerId: String, number: Int, nextTurnPlayerId: String) {
        val state = _uiState.value
        val board = state.board ?: return

        val previousLinesCount = board.getCompletedLines().size
        val wasMarked = board.markNumber(number)

        val newLines = board.getCompletedLines()
        val currentLinesCount = newLines.size
        val winningIndices = board.getWinningCellIndices()
        val illuminated = board.getIlluminatedLetters()

        val markedSet = board.numbers.mapIndexedNotNull { idx, num ->
            if (board.isMarkedAtIndex(idx)) idx else null
        }.toSet()

        // Also update AI boards on host
        if (state.isHost) {
            for ((aiId, aiBoard) in aiBoards) {
                val prevAiLines = aiBoard.getCompletedLines().size
                aiBoard.markNumber(number)
                val currentAiLines = aiBoard.getCompletedLines().size
                if (currentAiLines > prevAiLines) {
                    server?.reportLineCompleted(aiId, currentAiLines)
                }
            }
        }

        val isMyTurnNow = nextTurnPlayerId == state.localPlayer.id

        _uiState.update {
            it.copy(
                markedIndices = markedSet,
                completedLines = newLines,
                winningCellIndices = winningIndices,
                illuminatedLetters = illuminated,
                lastCalledNumber = number,
                calledNumbersHistory = it.calledNumbersHistory + number,
                currentTurnPlayerId = nextTurnPlayerId,
                isMyTurn = isMyTurnNow,
                turnTimerSeconds = 10
            )
        }

        // Sound / Haptics triggers
        if (wasMarked) {
            audioEngine.playMarkNumber()
        }

        // Check if any new lines were completed on this turn
        if (currentLinesCount > previousLinesCount) {
            val newlyCompleted = newLines.drop(previousLinesCount)
            for (line in newlyCompleted) {
                _lineStrikeTrigger.tryEmit(line)
                viewModelScope.launch {
                    audioEngine.playLineStrike()
                }
            }

            for (i in previousLinesCount until currentLinesCount) {
                val letterChar = state.selectedGameMode.targetWord.getOrNull(i)
                if (letterChar != null) {
                    _letterUnlockTrigger.tryEmit(letterChar)
                    viewModelScope.launch {
                        audioEngine.playLetterUnlock()
                    }
                }
            }

            // Report completed lines count to server
            if (state.isHost) {
                server?.reportLineCompleted(state.localPlayer.id, currentLinesCount)
            } else {
                val linePacket = NetworkPacket.LineCompleted(
                    playerId = state.localPlayer.id,
                    lineType = newlyCompleted.first().lineType,
                    lineIndex = newlyCompleted.first().index,
                    totalLinesCount = currentLinesCount
                )
                client?.sendPacket(linePacket)
            }
        }

        if (isMyTurnNow && !state.isGameOver) {
            audioEngine.playYourTurn()
        }

        startMoveTimer()
        checkAiTurn(nextTurnPlayerId)
    }

    private fun startMoveTimer() {
        moveTimerJob?.cancel()
        val state = _uiState.value
        if (state.isGameOver || state.currentScreen != AppScreen.GAME_PLAY) return

        _uiState.update { it.copy(turnTimerSeconds = 10) }

        moveTimerJob = viewModelScope.launch {
            for (sec in 9 downTo 0) {
                delay(1000)
                if (_uiState.value.isGameOver || _uiState.value.currentScreen != AppScreen.GAME_PLAY) return@launch
                _uiState.update { it.copy(turnTimerSeconds = sec) }
            }

            // 10s Timer expired: auto-pick a move if it's local player's turn
            val currentState = _uiState.value
            if (currentState.isGameOver || currentState.currentScreen != AppScreen.GAME_PLAY) return@launch

            if (currentState.isMyTurn) {
                val board = currentState.board
                val calledSet = currentState.calledNumbersHistory.toSet()
                val unmarked = board?.numbers?.filterIndexed { index, _ -> !board.isMarkedAtIndex(index) } ?: emptyList()
                val validPicks = unmarked.filterNot { calledSet.contains(it) }
                val pick = validPicks.firstOrNull() ?: (1..currentState.selectedGameMode.totalNumbers).filterNot { calledSet.contains(it) }.firstOrNull()
                if (pick != null) {
                    onCellClicked(pick)
                }
            }
        }
    }

    private fun checkAiTurn(turnPlayerId: String) {
        val state = _uiState.value
        val turnPlayer = state.players.find { it.id == turnPlayerId }
        if (turnPlayer != null && turnPlayer.isAi && state.isHost && !state.isGameOver) {
            aiTurnJob?.cancel()
            aiTurnJob = viewModelScope.launch {
                // Bots move within 2s (1.2s to 1.8s delay)
                delay(kotlin.random.Random.nextLong(1200, 1800))
                if (_uiState.value.isGameOver) return@launch

                val calledSet = _uiState.value.calledNumbersHistory.toSet()
                val availableNumbers = (1..state.selectedGameMode.totalNumbers).filterNot { calledSet.contains(it) }
                if (availableNumbers.isNotEmpty()) {
                    val pick = pickAiMove(turnPlayer.id, availableNumbers, state.selectedDifficulty, state.selectedGameMode)
                    server?.processPlayerMove(turnPlayer.id, pick)
                }
            }
        }
    }

    private fun pickAiMove(aiId: String, availableNumbers: List<Int>, difficulty: DifficultyLevel, mode: GameMode): Int {
        val board = aiBoards[aiId] ?: return availableNumbers.random()
        return when (difficulty) {
            DifficultyLevel.LOW -> {
                // Low = random marking
                availableNumbers.random()
            }
            DifficultyLevel.MEDIUM -> {
                // Medium = 50% strategic line build, 50% random
                if (kotlin.random.Random.nextFloat() < 0.5f) {
                    availableNumbers.random()
                } else {
                    findBestBoardMove(board, availableNumbers) ?: availableNumbers.random()
                }
            }
            DifficultyLevel.HIGH -> {
                // High = Smart AI prioritizing highest completion lines
                findBestBoardMove(board, availableNumbers) ?: availableNumbers.random()
            }
        }
    }

    private fun findBestBoardMove(board: BingoBoard, availableNumbers: List<Int>): Int? {
        val availableSet = availableNumbers.toSet()
        val size = board.size
        var bestScore = -1
        var bestNumber: Int? = null

        // Check rows
        for (r in 0 until size) {
            val rowIndices = (0 until size).map { c -> r * size + c }
            val markedCount = rowIndices.count { board.isMarkedAtIndex(it) }
            for (idx in rowIndices) {
                if (!board.isMarkedAtIndex(idx)) {
                    val num = board.numbers.getOrNull(idx)
                    if (num != null && availableSet.contains(num)) {
                        val score = markedCount * 10
                        if (score > bestScore) {
                            bestScore = score
                            bestNumber = num
                        }
                    }
                }
            }
        }

        // Check columns
        for (c in 0 until size) {
            val colIndices = (0 until size).map { r -> r * size + c }
            val markedCount = colIndices.count { board.isMarkedAtIndex(it) }
            for (idx in colIndices) {
                if (!board.isMarkedAtIndex(idx)) {
                    val num = board.numbers.getOrNull(idx)
                    if (num != null && availableSet.contains(num)) {
                        val score = markedCount * 10
                        if (score > bestScore) {
                            bestScore = score
                            bestNumber = num
                        }
                    }
                }
            }
        }

        return bestNumber
    }

    private fun onGameOverReceived(
        winnerIds: List<String>,
        winnerNames: List<String>,
        finalScores: Map<String, Int>
    ) {
        val state = _uiState.value
        val isLocalWinner = winnerIds.contains(state.localPlayer.id)
        val isTie = winnerIds.size > 1

        val durationSec = ((System.currentTimeMillis() - gameStartTime) / 1000).toInt().coerceAtLeast(5)
        val linesCount = state.completedLines.size
        val targetLines = state.selectedGameMode.linesToWin
        val totalPlayers = state.players.size.coerceAtLeast(2)

        val (updatedStats, record) = statsManager.recordMatch(
            isWin = isLocalWinner,
            modeName = state.selectedGameMode.title,
            linesCompleted = linesCount,
            targetLines = targetLines,
            playerCount = totalPlayers,
            durationSeconds = durationSec
        )

        _uiState.update {
            it.copy(
                isGameOver = true,
                winnerIds = winnerIds,
                winnerNames = winnerNames,
                isLocalPlayerWinner = isLocalWinner,
                isTieGame = isTie,
                finalScores = finalScores,
                careerStats = updatedStats,
                matchHistory = (listOf(record) + it.matchHistory).take(50),
                isLocalPlayerReady = it.isHost || it.isSoloPractice,
                isHostLeft = false
            )
        }

        if (isLocalWinner) {
            audioEngine.playVictory()
        }
    }

    fun updateTheme(theme: AppTheme) {
        profilePrefsManager.saveTheme(theme)
        _uiState.update { it.copy(selectedTheme = theme) }
    }

    fun resetCareerStats() {
        statsManager.resetStats()
        _uiState.update {
            it.copy(
                careerStats = PlayerCareerStats(),
                matchHistory = emptyList()
            )
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        profilePrefsManager.saveSoundEnabled(enabled)
        audioEngine.isSoundEnabled = enabled
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        profilePrefsManager.saveHapticsEnabled(enabled)
        audioEngine.isHapticsEnabled = enabled
        _uiState.update { it.copy(isHapticsEnabled = enabled) }
    }

    fun sendReaction(emoji: String) {
        val state = _uiState.value
        val packet = NetworkPacket.ChatReaction(
            playerId = state.localPlayer.id,
            playerName = state.localPlayer.name,
            emoji = emoji,
            timestamp = System.currentTimeMillis()
        )
        if (state.isHost) {
            server?.broadcast(packet)
            onChatReactionReceived(state.localPlayer.name, emoji)
        } else {
            client?.sendPacket(packet)
            // Client receives broadcasted reaction from server in event stream - no duplicate local call
        }
    }

    private fun onChatReactionReceived(playerName: String, emoji: String) {
        val reaction = ReactionPopup(
            playerName = playerName,
            emoji = emoji
        )
        _uiState.update {
            it.copy(activeReactions = (it.activeReactions + reaction).takeLast(6))
        }

        viewModelScope.launch {
            delay(3500)
            _uiState.update {
                it.copy(activeReactions = it.activeReactions.filterNot { r -> r.id == reaction.id })
            }
        }
    }

    fun forfeitAndLeaveGame() {
        val state = _uiState.value
        if (state.currentScreen == AppScreen.GAME_PLAY && !state.isGameOver) {
            val durationSec = ((System.currentTimeMillis() - gameStartTime) / 1000).toInt().coerceAtLeast(5)
            val linesCount = state.completedLines.size
            val targetLines = state.selectedGameMode.linesToWin
            val totalPlayers = state.players.size.coerceAtLeast(2)

            val (updatedStats, record) = statsManager.recordMatch(
                isWin = false,
                modeName = state.selectedGameMode.title,
                linesCompleted = linesCount,
                targetLines = targetLines,
                playerCount = totalPlayers,
                durationSeconds = durationSec
            )
            _uiState.update {
                it.copy(
                    careerStats = updatedStats,
                    matchHistory = (listOf(record) + it.matchHistory).take(50)
                )
            }

            if (!state.isHost) {
                client?.sendPacket(NetworkPacket.PlayerForfeited(state.localPlayer.id, state.localPlayer.name))
            } else {
                server?.handlePlayerForfeit(state.localPlayer.id)
            }
        }
        returnToHome()
    }

    fun requestRematch() {
        val state = _uiState.value
        if (state.isHost) {
            initLocalBoard(state.selectedGameMode)
            server?.startRematch()
        } else {
            initLocalBoard(state.selectedGameMode)
            client?.sendPacket(NetworkPacket.RematchRequest(state.localPlayer.id))
        }
    }

    fun returnToRoom() {
        val state = _uiState.value
        if (state.isHostLeft) {
            returnToHome()
            return
        }

        initLocalBoard(state.selectedGameMode)
        moveTimerJob?.cancel()
        aiTurnJob?.cancel()

        if (state.isHost) {
            server?.returnToLobby()
            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.HOST_LOBBY,
                    isGameOver = false,
                    calledNumbersHistory = emptyList(),
                    lastCalledNumber = null
                )
            }
        } else if (state.isSoloPractice) {
            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.HOST_LOBBY,
                    isGameOver = false,
                    calledNumbersHistory = emptyList(),
                    lastCalledNumber = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.CLIENT_LOBBY,
                    isGameOver = false,
                    calledNumbersHistory = emptyList(),
                    lastCalledNumber = null
                )
            }
        }
    }

    fun returnToHome() {
        cleanupNetwork()
        initLocalBoard(_uiState.value.selectedGameMode)
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.HOME,
                isHost = false,
                isSoloPractice = false,
                isGameOver = false,
                isLocalPlayerReady = false,
                isHostLeft = false,
                calledNumbersHistory = emptyList(),
                lastCalledNumber = null
            )
        }
    }

    fun toggleSound() {
        val newValue = !_uiState.value.isSoundEnabled
        profilePrefsManager.saveSoundEnabled(newValue)
        audioEngine.isSoundEnabled = newValue
        _uiState.update { it.copy(isSoundEnabled = newValue) }
    }

    fun toggleHaptics() {
        val newValue = !_uiState.value.isHapticsEnabled
        profilePrefsManager.saveHapticsEnabled(newValue)
        audioEngine.isHapticsEnabled = newValue
        _uiState.update { it.copy(isHapticsEnabled = newValue) }
    }

    private fun cleanupNetwork() {
        moveTimerJob?.cancel()
        aiTurnJob?.cancel()
        serverEventsJob?.cancel()
        clientEventsJob?.cancel()
        aiBoards.clear()
        discoveryEngine.stopHosting()
        discoveryEngine.stopDiscovery()
        server?.stop()
        server = null
        client?.disconnect()
        client = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanupNetwork()
    }
}
