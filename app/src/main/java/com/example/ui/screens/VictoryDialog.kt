package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ParticleOverlay
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
fun VictoryDialog(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onDismiss: () -> Unit
) {
    val isWinner = uiState.isLocalPlayerWinner
    val isTie = uiState.isTieGame
    val isSolo = uiState.isSoloPractice
    val winnerNamesStr = if (uiState.winnerNames.isNotEmpty()) {
        uiState.winnerNames.joinToString(", ")
    } else {
        "Winner"
    }

    val isHost = uiState.isHost
    val isHostLeft = uiState.isHostLeft
    val otherPlayers = uiState.players.filterNot { it.isHost }
    val allOthersReady = otherPlayers.isEmpty() || otherPlayers.all { it.isReady }
    val isLocalReady = uiState.isLocalPlayerReady
    var showLeaveRoomConfirmDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            // Confetti Explosion
            ParticleOverlay(
                isExploding = true,
                particleCount = if (isWinner) 150 else 75
            )

            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = VibrantSurface,
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (isWinner) VibrantLimeBorder else VibrantBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 480.dp)
                        .padding(16.dp)
                        .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = VibrantLimeBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Trophy Icon Badge
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(if (isWinner) VibrantLime else VibrantCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = if (isWinner) VibrantLimeDark else VibrantTextSecondary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        // Headline Title
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when {
                                    isTie && isWinner -> "CO-WINNERS TIE!"
                                    isTie -> "SIMULTANEOUS TIE!"
                                    isWinner -> "BINGO VICTORY!"
                                    else -> "ROUND COMPLETED"
                                },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = if (isWinner) VibrantPrimary else VibrantTextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isWinner) {
                                    if (isTie) "You and other players hit Bingo at the exact same turn!"
                                    else "You completed ${uiState.selectedGameMode.linesToWin} lines first!"
                                } else {
                                    "$winnerNamesStr claimed victory this round!"
                                },
                                fontSize = 13.sp,
                                color = VibrantTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Player Line Scores Breakdown
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VibrantCard, RoundedCornerShape(20.dp))
                                .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "FINAL STANDINGS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantTextMuted,
                                    letterSpacing = 1.sp
                                )

                                uiState.players.sortedByDescending { it.completedLinesCount }.forEach { player ->
                                    val isThisPlayerWinner = uiState.winnerIds.contains(player.id)
                                    val isYou = player.id == uiState.localPlayer.id
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (isThisPlayerWinner) "👑" else "•",
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = player.name,
                                                color = if (isThisPlayerWinner) VibrantPrimary else VibrantTextPrimary,
                                                fontWeight = if (isThisPlayerWinner) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                            if (isYou) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(VibrantPrimary, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "YOU",
                                                        fontSize = 9.sp,
                                                        color = VibrantTextOnPrimary,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${player.completedLinesCount} Lines",
                                            color = if (isThisPlayerWinner) VibrantPrimary else VibrantTextMuted,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Actions Section
                        if (isSolo) {
                            // Practice Match: ONLY Home and Play Again
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.requestRematch() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("rematch_button"),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VibrantPrimary,
                                        contentColor = VibrantTextOnPrimary
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Replay,
                                            contentDescription = null,
                                            tint = VibrantTextOnPrimary
                                        )
                                        Text(
                                            text = "Play Again",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VibrantTextOnPrimary
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.returnToHome() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("return_home_button"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VibrantSurface,
                                        contentColor = VibrantTextSecondary
                                    ),
                                    border = BorderStroke(1.dp, VibrantBorder)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = null,
                                            tint = VibrantTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Home",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = VibrantTextSecondary
                                        )
                                    }
                                }
                            }
                        } else {
                            // Multiplayer Room
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (isHost) {
                                    val canPlayAgain = allOthersReady
                                    Button(
                                        onClick = { viewModel.requestRematch() },
                                        enabled = canPlayAgain,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("rematch_button"),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = VibrantPrimary,
                                            contentColor = VibrantTextOnPrimary,
                                            disabledContainerColor = VibrantCard,
                                            disabledContentColor = VibrantTextMuted
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (canPlayAgain) Icons.Default.Replay else Icons.Default.HourglassEmpty,
                                                contentDescription = null,
                                                tint = if (canPlayAgain) VibrantTextOnPrimary else VibrantTextMuted
                                            )
                                            Text(
                                                text = if (canPlayAgain) "Play Again" else "Waiting for others",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (canPlayAgain) VibrantTextOnPrimary else VibrantTextMuted
                                            )
                                        }
                                    }
                                } else {
                                    // Non-host players
                                    if (isHostLeft) {
                                        Button(
                                            onClick = {},
                                            enabled = false,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp)
                                                .testTag("rematch_button"),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                disabledContainerColor = VibrantCard,
                                                disabledContentColor = Color(0xFFE57373)
                                            )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = Color(0xFFE57373)
                                                )
                                                Text(
                                                    text = "Host Left",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE57373)
                                                )
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.toggleReady() },
                                            enabled = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp)
                                                .testTag("rematch_button"),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isLocalReady) VibrantLimeDark else VibrantLime,
                                                contentColor = if (isLocalReady) Color.White else VibrantLimeDark
                                            )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = if (isLocalReady) Color.White else VibrantLimeDark
                                                )
                                                Text(
                                                    text = if (isLocalReady) "Ready ✓" else "Ready",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isLocalReady) Color.White else VibrantLimeDark
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.returnToRoom() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("return_room_button"),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = VibrantCard,
                                            contentColor = VibrantTextPrimary
                                        ),
                                        border = BorderStroke(1.dp, VibrantBorder)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MeetingRoom,
                                                contentDescription = null,
                                                tint = VibrantPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Room",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VibrantTextPrimary
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showLeaveRoomConfirmDialog = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("return_home_button"),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = VibrantSurface,
                                            contentColor = VibrantTextSecondary
                                        ),
                                        border = BorderStroke(1.dp, VibrantBorder)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = null,
                                                tint = VibrantTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Home",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = VibrantTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Leave Multiplayer Room Confirmation Dialog
            if (showLeaveRoomConfirmDialog) {
                Dialog(
                    onDismissRequest = { showLeaveRoomConfirmDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = VibrantSurface,
                        border = BorderStroke(1.dp, VibrantBorder),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .widthIn(max = 400.dp)
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
                                text = "Leave Room?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Are you sure you want to leave the room and return to the main menu?",
                                fontSize = 13.sp,
                                color = VibrantTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showLeaveRoomConfirmDialog = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VibrantCard,
                                        contentColor = VibrantTextPrimary
                                    ),
                                    border = BorderStroke(1.dp, VibrantBorder)
                                ) {
                                    Text("Cancel", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                Button(
                                    onClick = {
                                        showLeaveRoomConfirmDialog = false
                                        viewModel.returnToHome()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("confirm_leave_room_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD32F2F),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Leave", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
