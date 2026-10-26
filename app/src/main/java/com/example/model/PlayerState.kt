package com.example.model

data class PlayerState(
    val id: String,
    val name: String,
    val isHost: Boolean = false,
    val avatarIndex: Int = 0,
    val customAvatarPath: String? = null,
    val completedLinesCount: Int = 0,
    val isReady: Boolean = false,
    val isWinner: Boolean = false,
    val pingMs: Long = 0L,
    val isAi: Boolean = false,
    val isOffline: Boolean = false,
    val isEliminated: Boolean = false,
    val consecutiveMissedTurns: Int = 0
)
