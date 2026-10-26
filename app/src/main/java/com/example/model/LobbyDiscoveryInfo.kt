package com.example.model

data class LobbyDiscoveryInfo(
    val gameId: String,
    val roomCode: String = "",
    val hostName: String,
    val hostIp: String,
    val port: Int,
    val mode: GameMode,
    val playerCount: Int,
    val maxPlayers: Int,
    val isHotspotHost: Boolean = false,
    val hasPassword: Boolean = false,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)
