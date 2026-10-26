package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PlayerState
import com.example.ui.components.AvatarEmojis
import com.example.ui.components.LetterTracker
import com.example.ui.components.LineStrikeOverlay
import com.example.ui.components.ReactionOverlay
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
fun GameScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val mode = uiState.selectedGameMode
    val gridSize = mode.size
    val isMyTurn = uiState.isMyTurn
    var showExitGameConfirmDialog by remember { mutableStateOf(false) }

    // Android back navigation handler
    BackHandler {
        if (uiState.isGameOver) {
            if (uiState.isSoloPractice) {
                viewModel.returnToHome()
            } else {
                viewModel.returnToRoom()
            }
        } else {
            showExitGameConfirmDialog = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "turnPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibrantBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP BAR: Exit button on left, Players Connected & Latency on right (NO Bingo P2P / NW Discovery text)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (uiState.isGameOver) {
                            viewModel.returnToRoom()
                        } else {
                            showExitGameConfirmDialog = true
                        }
                    },
                    modifier = Modifier.testTag("exit_game_button")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Exit",
                        tint = VibrantTextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Connected players count badge
                    Box(
                        modifier = Modifier
                            .background(VibrantCard, RoundedCornerShape(8.dp))
                            .border(1.dp, VibrantBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = VibrantPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${uiState.players.size} Players Connected",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextPrimary
                            )
                        }
                    }

                    // Latency badge
                    Box(
                        modifier = Modifier
                            .background(VibrantCard, RoundedCornerShape(8.dp))
                            .border(1.dp, VibrantBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isSoloPractice) VibrantTextMuted else VibrantLimeDark)
                            )
                            Text(
                                text = if (uiState.isSoloPractice) "Offline" else "${uiState.pingMs}ms Latency",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextSecondary
                            )
                        }
                    }
                }
            }

            // B-I-N-G-O Letter Tracker (50% smaller boxes)
            LetterTracker(
                targetWord = mode.targetWord,
                completedLinesCount = uiState.completedLines.size
            )

            // Recent Numbers Bar + Last Pick
            if (uiState.calledNumbersHistory.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Called:",
                            color = VibrantTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.calledNumbersHistory.takeLast(8).reversed().forEachIndexed { idx, num ->
                            val isLatest = idx == 0
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isLatest) VibrantLime else VibrantCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = num.toString(),
                                    color = if (isLatest) VibrantLimeDark else VibrantTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (uiState.lastCalledNumber != null) {
                        Box(
                            modifier = Modifier
                                .background(VibrantLime.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Last: ${uiState.lastCalledNumber}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = VibrantLimeDark
                            )
                        }
                    }
                }
            }

            // MAIN INTERACTIVE BINGO GRID (5x5 or 10x10) - Non-scrollable, responsive aspect ratio
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(VibrantCard)
                    .border(1.5.dp, VibrantBorder, RoundedCornerShape(16.dp))
                    .padding(if (gridSize == 5) 6.dp else 4.dp)
            ) {
                val gridSpacing = if (gridSize == 5) 4.dp else 2.dp
                val fontSize = if (gridSize == 5) 18.sp else 9.sp
                val cellShape = if (gridSize == 5) RoundedCornerShape(8.dp) else RoundedCornerShape(4.dp)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridSize),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                    verticalArrangement = Arrangement.spacedBy(gridSpacing),
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(uiState.boardNumbers) { index, number ->
                        val isMarked = uiState.markedIndices.contains(index)
                        val isWinningCell = uiState.winningCellIndices.contains(index)

                        val cellBgColor by animateColorAsState(
                            targetValue = when {
                                isWinningCell -> VibrantLime
                                isMarked -> VibrantPrimary
                                else -> VibrantCardLight
                            },
                            label = "cellBg"
                        )

                        val cellTextColor = when {
                            isWinningCell -> VibrantLimeDark
                            isMarked -> VibrantTextOnPrimary
                            else -> VibrantTextPrimary
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .scale(if (isMarked && !isWinningCell) 0.96f else 1f)
                                .clip(cellShape)
                                .background(cellBgColor)
                                .then(
                                    if (isWinningCell) {
                                        Modifier.border(
                                            width = 1.5.dp,
                                            color = VibrantLimeBorder,
                                            shape = cellShape
                                        )
                                    } else if (!isMarked) {
                                        Modifier.border(
                                            width = 0.8.dp,
                                            color = VibrantBorder.copy(alpha = 0.5f),
                                            shape = cellShape
                                        )
                                    } else Modifier
                                )
                                .clickable(enabled = isMyTurn && !isMarked && !uiState.isGameOver) {
                                    viewModel.onCellClicked(number)
                                }
                                .testTag("bingo_cell_${number}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = number.toString(),
                                fontSize = fontSize,
                                fontWeight = if (isMarked || isWinningCell) FontWeight.Black else FontWeight.Bold,
                                color = cellTextColor
                            )
                        }
                    }
                }

                // Dynamic Animated Line Strikes Canvas Overlay
                LineStrikeOverlay(
                    gridSize = gridSize,
                    completedLines = uiState.completedLines,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ALL PLAYERS LIST BELOW BOARD (2-column layout: 2+2 in 5x5, 4+4 in 10x10)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val players = uiState.players
                players.chunked(2).forEach { rowPlayers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowPlayers.forEach { player ->
                            val isCurrentTurn = player.id == uiState.currentTurnPlayerId
                            val isLocal = player.id == uiState.localPlayer.id
                            PlayerBoardCard(
                                player = player,
                                isCurrentTurn = isCurrentTurn,
                                isLocal = isLocal,
                                timerSeconds = uiState.turnTimerSeconds,
                                linesToWin = mode.linesToWin,
                                pulseScale = if (isCurrentTurn && isMyTurn) pulseScale else 1f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowPlayers.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Quick LAN Reaction & Audio Toolbar (Hidden in solo practice)
            if (!uiState.isSoloPractice) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VibrantSurface, RoundedCornerShape(14.dp))
                        .border(1.dp, VibrantBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("🎉", "🍀", "🔥", "😱", "👑").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(VibrantCard)
                                    .clickable { viewModel.sendReaction(emoji) }
                                    .testTag("reaction_$emoji"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 14.sp)
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(VibrantCard)
                    ) {
                        Icon(
                            imageVector = if (uiState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Toggle Sound",
                            tint = if (uiState.isSoundEnabled) VibrantPrimary else VibrantTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // Floating Reaction Overlay (Multiplayer only)
        if (!uiState.isSoloPractice) {
            ReactionOverlay(
                reactions = uiState.activeReactions,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 60.dp)
            )
        }

        // Victory Dialog Celebration
        if (uiState.isGameOver) {
            VictoryDialog(
                viewModel = viewModel,
                uiState = uiState,
                onDismiss = {}
            )
        }

        // Forfeit / Exit Confirmation Dialog
        if (showExitGameConfirmDialog) {
            Dialog(
                onDismissRequest = { showExitGameConfirmDialog = false },
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
                            text = "Forfeit and Leave?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "If you leave the match now, it will count as a Loss. Other players will continue the match.",
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
                                onClick = { showExitGameConfirmDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantCard)
                            ) {
                                Text("Stay & Play", color = VibrantTextSecondary)
                            }
                            Button(
                                onClick = {
                                    showExitGameConfirmDialog = false
                                    viewModel.forfeitAndLeaveGame()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Forfeit", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerBoardCard(
    player: PlayerState,
    isCurrentTurn: Boolean,
    isLocal: Boolean,
    timerSeconds: Int,
    linesToWin: Int,
    pulseScale: Float,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(10.dp)
    val isUrgent = timerSeconds <= 3

    Box(
        modifier = modifier
            .scale(if (isCurrentTurn) pulseScale else 1f)
            .clip(cardShape)
            .background(if (isCurrentTurn) VibrantLime else VibrantSurface)
            .border(
                width = if (isCurrentTurn) 1.5.dp else 1.dp,
                color = if (isCurrentTurn) VibrantLimeBorder else VibrantBorder,
                shape = cardShape
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val emoji = AvatarEmojis.getOrElse(player.avatarIndex) { "👤" }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isCurrentTurn) VibrantPrimary else VibrantCard),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 12.sp)
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isLocal) "${player.name} (You)" else player.name,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCurrentTurn) VibrantLimeDark else VibrantTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (player.isHost) {
                        Text(
                            text = "★",
                            color = if (isCurrentTurn) VibrantLimeDark else VibrantPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Text(
                    text = "${player.completedLinesCount}/$linesToWin Lines",
                    fontSize = 9.sp,
                    color = if (isCurrentTurn) VibrantLimeDark.copy(alpha = 0.85f) else VibrantTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isCurrentTurn) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isUrgent) Color(0xFFFF5252) else VibrantLimeDark)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⏱${timerSeconds}s",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(VibrantCard, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${player.completedLinesCount}L",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = VibrantTextSecondary
                    )
                }
            }
        }
    }
}

