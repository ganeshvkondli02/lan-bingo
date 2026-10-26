package com.example.model

import android.content.Context
import android.content.SharedPreferences
import com.example.model.MatchRecord
import com.example.model.PlayerCareerStats
import org.json.JSONArray
import org.json.JSONObject

class StatsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bingo_stats_prefs", Context.MODE_PRIVATE)

    fun loadStats(): PlayerCareerStats {
        val played = prefs.getInt("games_played", 0)
        val won = prefs.getInt("games_won", 0)
        val currentStreak = prefs.getInt("current_streak", 0)
        val bestStreak = prefs.getInt("best_streak", 0)
        val totalLines = prefs.getInt("total_lines", 0)
        return PlayerCareerStats(
            gamesPlayed = played,
            gamesWon = won,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalLinesCompleted = totalLines
        )
    }

    fun recordMatch(
        isWin: Boolean,
        modeName: String,
        linesCompleted: Int,
        targetLines: Int,
        playerCount: Int,
        durationSeconds: Int
    ): Pair<PlayerCareerStats, MatchRecord> {
        val current = loadStats()
        val newPlayed = current.gamesPlayed + 1
        val newWon = if (isWin) current.gamesWon + 1 else current.gamesWon
        val newStreak = if (isWin) current.currentStreak + 1 else 0
        val newBest = maxOf(current.bestStreak, newStreak)
        val newTotalLines = current.totalLinesCompleted + linesCompleted

        prefs.edit()
            .putInt("games_played", newPlayed)
            .putInt("games_won", newWon)
            .putInt("current_streak", newStreak)
            .putInt("best_streak", newBest)
            .putInt("total_lines", newTotalLines)
            .apply()

        val record = MatchRecord(
            id = System.currentTimeMillis().toString(),
            timestamp = System.currentTimeMillis(),
            modeName = modeName,
            isWin = isWin,
            linesCompleted = linesCompleted,
            targetLines = targetLines,
            playerCount = playerCount,
            durationSeconds = durationSeconds
        )

        saveMatchRecord(record)

        val updatedStats = PlayerCareerStats(
            gamesPlayed = newPlayed,
            gamesWon = newWon,
            currentStreak = newStreak,
            bestStreak = newBest,
            totalLinesCompleted = newTotalLines
        )

        return Pair(updatedStats, record)
    }

    fun loadMatchHistory(): List<MatchRecord> {
        val jsonStr = prefs.getString("match_history_json", "[]") ?: "[]"
        val list = mutableListOf<MatchRecord>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    MatchRecord(
                        id = obj.optString("id", i.toString()),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        modeName = obj.optString("modeName", "Classic 5x5"),
                        isWin = obj.optBoolean("isWin", false),
                        linesCompleted = obj.optInt("linesCompleted", 0),
                        targetLines = obj.optInt("targetLines", 5),
                        playerCount = obj.optInt("playerCount", 2),
                        durationSeconds = obj.optInt("durationSeconds", 60)
                    )
                )
            }
        } catch (_: Exception) {}
        return list.reversed() // newest first
    }

    private fun saveMatchRecord(record: MatchRecord) {
        val existing = loadMatchHistory().reversed().toMutableList()
        existing.add(record)
        // Keep last 50 matches
        val trimmed = if (existing.size > 50) existing.takeLast(50) else existing

        val arr = JSONArray()
        for (m in trimmed) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("timestamp", m.timestamp)
            obj.put("modeName", m.modeName)
            obj.put("isWin", m.isWin)
            obj.put("linesCompleted", m.linesCompleted)
            obj.put("targetLines", m.targetLines)
            obj.put("playerCount", m.playerCount)
            obj.put("durationSeconds", m.durationSeconds)
            arr.put(obj)
        }
        prefs.edit().putString("match_history_json", arr.toString()).apply()
    }

    fun resetStats() {
        prefs.edit()
            .putInt("games_played", 0)
            .putInt("games_won", 0)
            .putInt("current_streak", 0)
            .putInt("best_streak", 0)
            .putInt("total_lines", 0)
            .putString("match_history_json", "[]")
            .apply()
    }
}
