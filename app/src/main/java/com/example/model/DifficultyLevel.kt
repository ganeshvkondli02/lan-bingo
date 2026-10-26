package com.example.model

enum class DifficultyLevel(
    val title: String,
    val subtitle: String,
    val description: String,
    val tag: String
) {
    LOW(
        title = "Low",
        subtitle = "Random Marking",
        description = "Bot picks random unmarked numbers for a casual, relaxing match.",
        tag = "RANDOM"
    ),
    MEDIUM(
        title = "Medium",
        subtitle = "Balanced Strategy",
        description = "Bot targets potential lines and balances its board moves.",
        tag = "BALANCED"
    ),
    HIGH(
        title = "High",
        subtitle = "Smart AI",
        description = "Bot aggressively targets line completions and strategic moves.",
        tag = "SMART"
    )
}
