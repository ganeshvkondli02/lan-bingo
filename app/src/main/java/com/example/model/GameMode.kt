package com.example.model

enum class GameMode(
    val title: String,
    val subtitle: String,
    val size: Int,
    val totalNumbers: Int,
    val targetWord: String,
    val linesToWin: Int,
    val minPlayers: Int,
    val maxPlayers: Int
) {
    CLASSIC_5X5(
        title = "Classic Bingo",
        subtitle = "5x5 Grid (1 to 25) • 2-4 Players",
        size = 5,
        totalNumbers = 25,
        targetWord = "BINGO",
        linesToWin = 5,
        minPlayers = 2,
        maxPlayers = 4
    ),
    EXTRA_10X10(
        title = "ExtraBingo",
        subtitle = "10x10 Grid (1 to 100) • 2-8 Players",
        size = 10,
        totalNumbers = 100,
        targetWord = "EXTRABINGO",
        linesToWin = 10,
        minPlayers = 2,
        maxPlayers = 8
    )
}
