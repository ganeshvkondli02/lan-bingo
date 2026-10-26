package com.example.model

data class MatchRecord(
    val id: String,
    val timestamp: Long,
    val modeName: String,
    val isWin: Boolean,
    val linesCompleted: Int,
    val targetLines: Int,
    val playerCount: Int,
    val durationSeconds: Int
)

data class PlayerCareerStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalLinesCompleted: Int = 0
) {
    val winRatePercent: Int
        get() = if (gamesPlayed > 0) ((gamesWon.toFloat() / gamesPlayed) * 100).toInt() else 0
}
