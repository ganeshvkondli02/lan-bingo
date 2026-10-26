package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VpnKey
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
import com.example.ui.theme.VibrantLime
import com.example.ui.theme.VibrantLimeDark
import com.example.ui.theme.VibrantPrimary
import com.example.ui.theme.VibrantSurface
import com.example.ui.theme.VibrantTextMuted
import com.example.ui.theme.VibrantTextOnPrimary
import com.example.ui.theme.VibrantTextPrimary
import com.example.ui.theme.VibrantTextSecondary

@Composable
fun MultiplayerRoadmapDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = VibrantSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VibrantLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = VibrantLimeDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Online Multiplayer",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantTextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE5A000), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "COMING SOON",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Text(
                                text = "Future Development Roadmap",
                                fontSize = 11.sp,
                                color = VibrantTextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_multiplayer_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VibrantTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(VibrantCard)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "🌐 Global Internet Matchmaking",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = VibrantTextPrimary
                        )
                        Text(
                            text = "While you can currently enjoy seamless zero-latency Peer-to-Peer matches over local Wi-Fi & Hotspot, our upcoming update will introduce worldwide cloud play!",
                            fontSize = 12.sp,
                            color = VibrantTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Planned Features
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoadmapFeatureItem(
                        title = "6-Digit Global Room Codes",
                        description = "Invite friends from anywhere in the world across mobile networks.",
                        icon = Icons.Default.VpnKey
                    )
                    RoadmapFeatureItem(
                        title = "Cross-Platform Matchmaking",
                        description = "Instant 1v1 and 4-player casual & ranked matchmaking lobbies.",
                        icon = Icons.Default.Language
                    )
                    RoadmapFeatureItem(
                        title = "Global Leaderboards & Seasons",
                        description = "Climb the weekly leaderboard with your win streak & achievements.",
                        icon = Icons.Default.Leaderboard
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
                ) {
                    Text(
                        text = "Got It!",
                        fontWeight = FontWeight.Bold,
                        color = VibrantTextOnPrimary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RoadmapFeatureItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(VibrantCardLight)
            .border(1.dp, VibrantBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(VibrantLime),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = VibrantLimeDark, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VibrantTextPrimary)
                Text(text = description, fontSize = 11.sp, color = VibrantTextMuted, lineHeight = 14.sp)
            }
        }
    }
}
