package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.VibrantBorder
import com.example.ui.theme.VibrantCard
import com.example.ui.theme.VibrantCardLight
import com.example.ui.theme.VibrantPrimary
import com.example.ui.theme.VibrantSurface
import com.example.ui.theme.VibrantTextMuted
import com.example.ui.theme.VibrantTextOnPrimary
import com.example.ui.theme.VibrantTextPrimary
import com.example.ui.theme.VibrantTextSecondary

@Composable
fun HowToPlayDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VibrantSurface,
            border = BorderStroke(1.dp, VibrantBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 480.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(VibrantCard)
                                .border(1.dp, VibrantPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = VibrantPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "How To Play Bingo LAN",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_rules_button")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = VibrantTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Rules List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RuleCard(
                        emoji = "⚡",
                        title = "1. LAN / Wi-Fi Multiplayer",
                        description = "Connect to the same Wi-Fi network or Mobile Hotspot. One player hosts a room, and all other devices automatically discover it with zero internet or server required!"
                    )

                    RuleCard(
                        emoji = "⊞",
                        title = "2. Game Modes",
                        description = "• 5×5 Classic Bingo: Numbers 1–25. Need 5 completed lines to unlock B-I-N-G-O and win!\n• 10×10 Extra Bingo: Numbers 1–100. Need 10 completed lines to win!"
                    )

                    RuleCard(
                        emoji = "🗹",
                        title = "3. Real-Time Number Marking",
                        description = "When ANY player taps a number on their board, that number is instantly broadcast to all players! Every board containing that number automatically marks it."
                    )

                    RuleCard(
                        emoji = "🎗",
                        title = "4. Winning the Match",
                        description = "Lines can be completed horizontally, vertically, or diagonally. Every line completed unlocks a letter in B-I-N-G-O! The first player to complete all lines wins the match!"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Got It Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("got_it_rules_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantPrimary,
                        contentColor = VibrantTextOnPrimary
                    )
                ) {
                    Text(
                        text = "Got It, Let's Play!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleCard(
    emoji: String,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VibrantCard)
            .border(1.dp, VibrantBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = emoji,
                    fontSize = 16.sp
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VibrantTextPrimary
                )
            }
            Text(
                text = description,
                fontSize = 12.sp,
                color = VibrantTextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}
