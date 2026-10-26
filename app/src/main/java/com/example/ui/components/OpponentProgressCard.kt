package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.PlayerState
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.VibrantBorder
import com.example.ui.theme.VibrantCard
import com.example.ui.theme.VibrantLime
import com.example.ui.theme.VibrantLimeBorder
import com.example.ui.theme.VibrantLimeDark
import com.example.ui.theme.VibrantPrimary
import com.example.ui.theme.VibrantSurface
import com.example.ui.theme.VibrantTextMuted
import com.example.ui.theme.VibrantTextPrimary
import com.example.viewmodel.ReactionPopup

@Composable
fun OpponentProgressCard(
    player: PlayerState,
    gameMode: GameMode,
    isCurrentTurn: Boolean,
    isLocal: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = (player.completedLinesCount.toFloat() / gameMode.linesToWin).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .background(
                if (isCurrentTurn) VibrantSurface else VibrantCard.copy(alpha = 0.6f),
                RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isCurrentTurn) 1.5.dp else 1.dp,
                color = if (isCurrentTurn) VibrantPrimary else VibrantBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayerAvatar(
                player = player,
                size = 30.dp
            )

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isLocal) "${player.name} (You)" else player.name,
                        color = if (isCurrentTurn) VibrantPrimary else VibrantTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (player.isHost) {
                        Text(
                            text = "HOST",
                            color = VibrantPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .size(width = 44.dp, height = 5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = VibrantPrimary,
                        trackColor = VibrantCard,
                    )
                    Text(
                        text = "${player.completedLinesCount}/${gameMode.linesToWin}",
                        color = VibrantTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ReactionOverlay(
    reactions: List<ReactionPopup>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.End
    ) {
        reactions.takeLast(3).forEach { reaction ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                Row(
                    modifier = Modifier
                        .background(VibrantSurface, RoundedCornerShape(20.dp))
                        .border(1.dp, VibrantLimeBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = reaction.emoji, fontSize = 20.sp)
                    Text(
                        text = reaction.playerName,
                        color = VibrantTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
