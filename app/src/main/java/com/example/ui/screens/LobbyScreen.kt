package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PlayerState
import com.example.ui.components.AvatarEmojis
import com.example.ui.components.PlayerAvatar
import com.example.ui.components.QrCodeDialog
import com.example.ui.theme.VibrantBackground
import com.example.ui.theme.VibrantBorder
import com.example.ui.theme.VibrantCard
import com.example.ui.theme.VibrantCardLight
import com.example.ui.theme.VibrantLime
import com.example.ui.theme.VibrantLimeBorder
import com.example.ui.theme.VibrantLimeDark
import com.example.ui.theme.VibrantPrimary
import com.example.ui.theme.VibrantSurface
import com.example.ui.theme.VibrantTextMuted
import com.example.ui.theme.VibrantTextOnPrimary
import com.example.ui.theme.VibrantTextPrimary
import com.example.ui.theme.VibrantTextSecondary
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel

@Composable
fun LobbyScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val isHost = uiState.isHost
    val mode = uiState.selectedGameMode
    val players = uiState.players
    val otherPlayers = players.filterNot { it.isHost }
    val allOtherReady = otherPlayers.isEmpty() || otherPlayers.all { it.isReady }
    val canStart = players.size >= mode.minPlayers && allOtherReady

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var playerToKickConfirm by remember { mutableStateOf<PlayerState?>(null) }

    // Android back navigation handler
    BackHandler {
        if (uiState.isSoloPractice) {
            viewModel.returnToHome()
        } else {
            showExitConfirmDialog = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibrantBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (uiState.isSoloPractice) {
                            viewModel.returnToHome()
                        } else {
                            showExitConfirmDialog = true
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VibrantCard)
                        .testTag("leave_lobby_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Leave", tint = VibrantTextPrimary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.isSoloPractice) "PRACTICE ROOM" else if (isHost) "LOBBY (HOST)" else "LOBBY (CLIENT)",
                        color = VibrantPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = mode.title,
                        color = VibrantTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.hostRoomPassword.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(VibrantCard, RoundedCornerShape(12.dp))
                                .border(1.dp, VibrantBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(12.dp))
                                Text(
                                    text = uiState.hostRoomPassword,
                                    color = VibrantTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(if (canStart) VibrantLime else VibrantCard, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${players.size}/${mode.maxPlayers}",
                            color = if (canStart) VibrantLimeDark else VibrantTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Room Code & QR Banner for LAN Multiplayer
            if (!uiState.isSoloPractice && uiState.hostRoomCode.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(VibrantCard)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "ROOM CODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = uiState.hostRoomCode,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = VibrantTextPrimary,
                                letterSpacing = 1.5.sp
                            )
                        }

                        Button(
                            onClick = { showQrDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VibrantPrimary,
                                contentColor = com.example.ui.theme.VibrantTextOnPrimary
                            ),
                            modifier = Modifier.testTag("show_qr_code_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "Show QR",
                                    tint = com.example.ui.theme.VibrantTextOnPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "QR Code",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.VibrantTextOnPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Connected Players List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYERS (${players.size}/${mode.maxPlayers})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VibrantTextMuted,
                    letterSpacing = 1.sp
                )

                if (isHost && players.size < mode.maxPlayers) {
                    Button(
                        onClick = { viewModel.addAiBot() },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_bot_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(16.dp))
                            Text("+ Add Bot", fontSize = 12.sp, color = VibrantTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                players.forEach { player ->
                    val isYou = player.id == uiState.localPlayer.id

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isYou) VibrantSurface else VibrantCard, RoundedCornerShape(16.dp))
                            .border(
                                width = if (isYou) 1.5.dp else 1.dp,
                                color = if (isYou) VibrantPrimary else VibrantBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                PlayerAvatar(
                                    player = player,
                                    size = 36.dp
                                )

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isYou) "${player.name} (You)" else player.name,
                                            fontWeight = FontWeight.Bold,
                                            color = VibrantTextPrimary,
                                            fontSize = 14.sp
                                        )
                                        if (!uiState.isSoloPractice && player.isHost) {
                                            Box(
                                                modifier = Modifier
                                                    .background(VibrantLime, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "HOST",
                                                    color = VibrantLimeDark,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                        if (player.isAi) {
                                            Box(
                                                modifier = Modifier
                                                    .background(VibrantPrimary, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "BOT",
                                                    color = VibrantTextOnPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                    if (!uiState.isSoloPractice) {
                                        Text(
                                            text = if (player.isHost) {
                                                "Host"
                                            } else if (player.isReady) {
                                                "Ready"
                                            } else {
                                                "Not Ready"
                                            },
                                            color = if (player.isReady || player.isHost) VibrantLimeDark else VibrantTextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = if (player.isReady || player.isHost) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isHost && !isYou) {
                                    IconButton(
                                        onClick = {
                                            if (player.isAi) {
                                                viewModel.removeAiBot(player.id)
                                            } else {
                                                playerToKickConfirm = player
                                            }
                                        },
                                        modifier = Modifier.size(28.dp).testTag("remove_player_${player.id}")
                                    ) {
                                        Text("✕", color = Color(0xFFD94848), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                if (!uiState.isSoloPractice) {
                                    if (player.isHost || player.isReady) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(VibrantLime),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Ready",
                                                tint = VibrantLimeDark,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(VibrantCardLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.HourglassEmpty,
                                                contentDescription = "Not Ready",
                                                tint = VibrantTextMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Board Permutation Preview & Shuffle CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR BOARD PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VibrantTextMuted,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = { viewModel.shuffleBoard() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantCard),
                    modifier = Modifier.testTag("shuffle_board_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, tint = VibrantTextSecondary, modifier = Modifier.size(16.dp))
                        Text("Shuffle Board", fontSize = 12.sp, color = VibrantTextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini grid preview (5x5 or 10x10)
            val previewSize = if (mode.size == 5) 5 else 10
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(VibrantCard)
                    .border(1.dp, VibrantBorder, RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(previewSize),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.boardNumbers) { num ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(VibrantCardLight, RoundedCornerShape(if (previewSize == 5) 10.dp else 4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = num.toString(),
                                color = VibrantTextPrimary,
                                fontSize = if (previewSize == 5) 15.sp else 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Start Game / Ready Controls Section
            if (isHost) {
                Button(
                    onClick = { viewModel.startHostGame() },
                    enabled = canStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_game_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantPrimary,
                        disabledContainerColor = VibrantCard
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = if (canStart) VibrantTextOnPrimary else VibrantTextMuted)
                        Text(
                            text = if (players.size < mode.minPlayers) {
                                "Need at least ${mode.minPlayers} players to start"
                            } else if (!allOtherReady) {
                                "Waiting for all players to be Ready..."
                            } else {
                                "Start Match"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canStart) VibrantTextOnPrimary else VibrantTextMuted
                        )
                    }
                }
            } else {
                val isReady = uiState.isLocalPlayerReady
                Button(
                    onClick = { viewModel.toggleReady() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("toggle_ready_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReady) Color(0xFFD32F2F) else VibrantLime
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isReady) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isReady) Color.White else VibrantLimeDark
                        )
                        Text(
                            text = if (isReady) "Cancel Ready" else "I'm Ready!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isReady) Color.White else VibrantLimeDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Leave Confirmation Dialog
        if (showExitConfirmDialog) {
            Dialog(
                onDismissRequest = { showExitConfirmDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = VibrantSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBorder),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .widthIn(max = 440.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = VibrantPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isHost) "Disband Room?" else "Leave Room?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHost) {
                                "Are you sure you want to leave? This will disband the room and disconnect all players."
                            } else {
                                "Are you sure you want to leave the room?"
                            },
                            fontSize = 13.sp,
                            color = VibrantTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showExitConfirmDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantCard)
                            ) {
                                Text("Stay", color = VibrantTextSecondary)
                            }
                            Button(
                                onClick = {
                                    showExitConfirmDialog = false
                                    viewModel.returnToHome()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Leave", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Kick Player Confirmation Dialog
        if (playerToKickConfirm != null) {
            val player = playerToKickConfirm!!
            Dialog(
                onDismissRequest = { playerToKickConfirm = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = VibrantSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBorder),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .widthIn(max = 440.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Remove Player",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Are you sure you want to remove ${player.name} from the room?",
                            fontSize = 13.sp,
                            color = VibrantTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { playerToKickConfirm = null },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantCard)
                            ) {
                                Text("Cancel", color = VibrantTextSecondary)
                            }
                            Button(
                                onClick = {
                                    val id = player.id
                                    playerToKickConfirm = null
                                    viewModel.kickPlayer(id)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Remove", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Host QR Code Dialog
        if (showQrDialog) {
            QrCodeDialog(
                roomCode = uiState.hostRoomCode,
                hostIp = uiState.hostLocalIp ?: "192.168.1.1",
                port = 8888,
                onDismiss = { showQrDialog = false }
            )
        }
    }
}
