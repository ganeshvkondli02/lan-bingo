package com.example.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Strongly typed Network Packet protocol for Local Sockets.
 */
sealed class NetworkPacket(val type: String) {

    data class Handshake(
        val playerId: String,
        val playerName: String,
        val avatarIndex: Int,
        val isHost: Boolean,
        val password: String = ""
    ) : NetworkPacket("HANDSHAKE")

    data class ReadyToggle(
        val playerId: String,
        val isReady: Boolean
    ) : NetworkPacket("READY_TOGGLE")

    data class KickPlayer(
        val playerId: String,
        val reason: String = "You were removed from the room by the host."
    ) : NetworkPacket("KICK_PLAYER")

    data class RoomDisbanded(
        val reason: String = "The host has closed the room."
    ) : NetworkPacket("ROOM_DISBANDED")

    data class PlayerForfeited(
        val playerId: String,
        val playerName: String
    ) : NetworkPacket("PLAYER_FORFEITED")

    data class PlayerOffline(
        val playerId: String,
        val isOffline: Boolean
    ) : NetworkPacket("PLAYER_OFFLINE")

    data class LobbyUpdate(
        val players: List<PlayerState>,
        val gameMode: GameMode,
        val currentTurnPlayerId: String? = null
    ) : NetworkPacket("LOBBY_UPDATE")

    data class GameStart(
        val seed: Long,
        val mode: GameMode,
        val turnOrder: List<String>,
        val currentTurnPlayerId: String
    ) : NetworkPacket("GAME_START")

    data class NumberMarked(
        val playerId: String,
        val number: Int,
        val timestamp: Long,
        val nextTurnPlayerId: String
    ) : NetworkPacket("NUMBER_MARKED")

    data class LineCompleted(
        val playerId: String,
        val lineType: LineType,
        val lineIndex: Int,
        val totalLinesCount: Int
    ) : NetworkPacket("LINE_COMPLETED")

    data class GameOver(
        val winnerIds: List<String>,
        val winnerNames: List<String>,
        val finalScores: Map<String, Int>
    ) : NetworkPacket("GAME_OVER")

    data class Heartbeat(
        val timestamp: Long
    ) : NetworkPacket("HEARTBEAT")

    data class Ping(
        val timestamp: Long
    ) : NetworkPacket("PING")

    data class Pong(
        val timestamp: Long
    ) : NetworkPacket("PONG")

    data class ChatReaction(
        val playerId: String,
        val playerName: String,
        val emoji: String,
        val timestamp: Long
    ) : NetworkPacket("CHAT_REACTION")

    data class RematchRequest(
        val playerId: String
    ) : NetworkPacket("REMATCH_REQUEST")

    data class RematchStart(
        val seed: Long,
        val mode: GameMode,
        val turnOrder: List<String>,
        val currentTurnPlayerId: String
    ) : NetworkPacket("REMATCH_START")

    fun toJsonString(): String {
        val json = JSONObject()
        json.put("type", type)
        when (this) {
            is Handshake -> {
                json.put("playerId", playerId)
                json.put("playerName", playerName)
                json.put("avatarIndex", avatarIndex)
                json.put("isHost", isHost)
                json.put("password", password)
            }
            is ReadyToggle -> {
                json.put("playerId", playerId)
                json.put("isReady", isReady)
            }
            is KickPlayer -> {
                json.put("playerId", playerId)
                json.put("reason", reason)
            }
            is RoomDisbanded -> {
                json.put("reason", reason)
            }
            is PlayerForfeited -> {
                json.put("playerId", playerId)
                json.put("playerName", playerName)
            }
            is PlayerOffline -> {
                json.put("playerId", playerId)
                json.put("isOffline", isOffline)
            }
            is LobbyUpdate -> {
                val array = JSONArray()
                for (p in players) {
                    val pObj = JSONObject()
                    pObj.put("id", p.id)
                    pObj.put("name", p.name)
                    pObj.put("isHost", p.isHost)
                    pObj.put("avatarIndex", p.avatarIndex)
                    pObj.put("lines", p.completedLinesCount)
                    pObj.put("isReady", p.isReady)
                    pObj.put("isWinner", p.isWinner)
                    pObj.put("pingMs", p.pingMs)
                    pObj.put("isAi", p.isAi)
                    pObj.put("isOffline", p.isOffline)
                    pObj.put("isEliminated", p.isEliminated)
                    pObj.put("consecutiveMissedTurns", p.consecutiveMissedTurns)
                    array.put(pObj)
                }
                json.put("players", array)
                json.put("gameMode", gameMode.name)
                json.put("currentTurnPlayerId", currentTurnPlayerId ?: "")
            }
            is GameStart -> {
                json.put("seed", seed)
                json.put("mode", mode.name)
                val orderArr = JSONArray()
                turnOrder.forEach { orderArr.put(it) }
                json.put("turnOrder", orderArr)
                json.put("currentTurnPlayerId", currentTurnPlayerId)
            }
            is NumberMarked -> {
                json.put("playerId", playerId)
                json.put("number", number)
                json.put("timestamp", timestamp)
                json.put("nextTurnPlayerId", nextTurnPlayerId)
            }
            is LineCompleted -> {
                json.put("playerId", playerId)
                json.put("lineType", lineType.name)
                json.put("lineIndex", lineIndex)
                json.put("totalLinesCount", totalLinesCount)
            }
            is GameOver -> {
                val winIds = JSONArray()
                winnerIds.forEach { winIds.put(it) }
                json.put("winnerIds", winIds)

                val winNames = JSONArray()
                winnerNames.forEach { winNames.put(it) }
                json.put("winnerNames", winNames)

                val scoresObj = JSONObject()
                finalScores.forEach { (k, v) -> scoresObj.put(k, v) }
                json.put("finalScores", scoresObj)
            }
            is Heartbeat -> {
                json.put("timestamp", timestamp)
            }
            is Ping -> {
                json.put("timestamp", timestamp)
            }
            is Pong -> {
                json.put("timestamp", timestamp)
            }
            is ChatReaction -> {
                json.put("playerId", playerId)
                json.put("playerName", playerName)
                json.put("emoji", emoji)
                json.put("timestamp", timestamp)
            }
            is RematchRequest -> {
                json.put("playerId", playerId)
            }
            is RematchStart -> {
                json.put("seed", seed)
                json.put("mode", mode.name)
                val orderArr = JSONArray()
                turnOrder.forEach { orderArr.put(it) }
                json.put("turnOrder", orderArr)
                json.put("currentTurnPlayerId", currentTurnPlayerId)
            }
        }
        return json.toString()
    }

    companion object {
        fun fromJsonString(str: String): NetworkPacket? {
            return try {
                val json = JSONObject(str)
                when (json.optString("type")) {
                    "HANDSHAKE" -> Handshake(
                        playerId = json.getString("playerId"),
                        playerName = json.getString("playerName"),
                        avatarIndex = json.optInt("avatarIndex", 0),
                        isHost = json.optBoolean("isHost", false),
                        password = json.optString("password", "")
                    )
                    "READY_TOGGLE" -> ReadyToggle(
                        playerId = json.getString("playerId"),
                        isReady = json.getBoolean("isReady")
                    )
                    "KICK_PLAYER" -> KickPlayer(
                        playerId = json.getString("playerId"),
                        reason = json.optString("reason", "You were removed from the room by the host.")
                    )
                    "ROOM_DISBANDED" -> RoomDisbanded(
                        reason = json.optString("reason", "The host has closed the room.")
                    )
                    "PLAYER_FORFEITED" -> PlayerForfeited(
                        playerId = json.getString("playerId"),
                        playerName = json.optString("playerName", "Player")
                    )
                    "PLAYER_OFFLINE" -> PlayerOffline(
                        playerId = json.getString("playerId"),
                        isOffline = json.getBoolean("isOffline")
                    )
                    "LOBBY_UPDATE" -> {
                        val array = json.getJSONArray("players")
                        val players = mutableListOf<PlayerState>()
                        for (i in 0 until array.length()) {
                            val pObj = array.getJSONObject(i)
                            players.add(
                                PlayerState(
                                    id = pObj.getString("id"),
                                    name = pObj.getString("name"),
                                    isHost = pObj.optBoolean("isHost", false),
                                    avatarIndex = pObj.optInt("avatarIndex", 0),
                                    completedLinesCount = pObj.optInt("lines", 0),
                                    isReady = pObj.optBoolean("isReady", false),
                                    isWinner = pObj.optBoolean("isWinner", false),
                                    pingMs = pObj.optLong("pingMs", 0L),
                                    isAi = pObj.optBoolean("isAi", false),
                                    isOffline = pObj.optBoolean("isOffline", false),
                                    isEliminated = pObj.optBoolean("isEliminated", false),
                                    consecutiveMissedTurns = pObj.optInt("consecutiveMissedTurns", 0)
                                )
                            )
                        }
                        val modeStr = json.optString("gameMode", GameMode.CLASSIC_5X5.name)
                        val mode = try { GameMode.valueOf(modeStr) } catch (_: Exception) { GameMode.CLASSIC_5X5 }
                        val turnId = json.optString("currentTurnPlayerId").takeIf { it.isNotEmpty() }
                        LobbyUpdate(players, mode, turnId)
                    }
                    "GAME_START" -> {
                        val seed = json.getLong("seed")
                        val modeStr = json.optString("mode", GameMode.CLASSIC_5X5.name)
                        val mode = try { GameMode.valueOf(modeStr) } catch (_: Exception) { GameMode.CLASSIC_5X5 }
                        val turnArr = json.getJSONArray("turnOrder")
                        val turnOrder = mutableListOf<String>()
                        for (i in 0 until turnArr.length()) {
                            turnOrder.add(turnArr.getString(i))
                        }
                        val currentTurn = json.getString("currentTurnPlayerId")
                        GameStart(seed, mode, turnOrder, currentTurn)
                    }
                    "NUMBER_MARKED" -> {
                        NumberMarked(
                            playerId = json.getString("playerId"),
                            number = json.getInt("number"),
                            timestamp = json.getLong("timestamp"),
                            nextTurnPlayerId = json.optString("nextTurnPlayerId", "")
                        )
                    }
                    "LINE_COMPLETED" -> {
                        val typeStr = json.getString("lineType")
                        val lineType = try { LineType.valueOf(typeStr) } catch (_: Exception) { LineType.HORIZONTAL }
                        LineCompleted(
                            playerId = json.getString("playerId"),
                            lineType = lineType,
                            lineIndex = json.getInt("lineIndex"),
                            totalLinesCount = json.getInt("totalLinesCount")
                        )
                    }
                    "GAME_OVER" -> {
                        val winIdsArr = json.getJSONArray("winnerIds")
                        val winnerIds = mutableListOf<String>()
                        for (i in 0 until winIdsArr.length()) {
                            winnerIds.add(winIdsArr.getString(i))
                        }

                        val winNamesArr = json.optJSONArray("winnerNames") ?: JSONArray()
                        val winnerNames = mutableListOf<String>()
                        for (i in 0 until winNamesArr.length()) {
                            winnerNames.add(winNamesArr.getString(i))
                        }

                        val scoresObj = json.getJSONObject("finalScores")
                        val finalScores = mutableMapOf<String, Int>()
                        val keys = scoresObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            finalScores[k] = scoresObj.getInt(k)
                        }

                        GameOver(winnerIds, winnerNames, finalScores)
                    }
                    "HEARTBEAT" -> Heartbeat(json.optLong("timestamp", System.currentTimeMillis()))
                    "PING" -> Ping(json.optLong("timestamp", System.currentTimeMillis()))
                    "PONG" -> Pong(json.optLong("timestamp", System.currentTimeMillis()))
                    "CHAT_REACTION" -> ChatReaction(
                        playerId = json.getString("playerId"),
                        playerName = json.getString("playerName"),
                        emoji = json.getString("emoji"),
                        timestamp = json.getLong("timestamp")
                    )
                    "REMATCH_REQUEST" -> RematchRequest(
                        playerId = json.getString("playerId")
                    )
                    "REMATCH_START" -> {
                        val seed = json.getLong("seed")
                        val modeStr = json.optString("mode", GameMode.CLASSIC_5X5.name)
                        val mode = try { GameMode.valueOf(modeStr) } catch (_: Exception) { GameMode.CLASSIC_5X5 }
                        val turnArr = json.getJSONArray("turnOrder")
                        val turnOrder = mutableListOf<String>()
                        for (i in 0 until turnArr.length()) {
                            turnOrder.add(turnArr.getString(i))
                        }
                        val currentTurn = json.getString("currentTurnPlayerId")
                        RematchStart(seed, mode, turnOrder, currentTurn)
                    }
                    else -> null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
