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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SentimentDissatisfied
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MatchRecord
import com.example.ui.theme.VibrantBorder
import com.example.ui.theme.VibrantCard
import com.example.ui.theme.VibrantCardLight
import com.example.ui.theme.VibrantLime
import com.example.ui.theme.VibrantLimeDark
import com.example.ui.theme.VibrantPrimary
import com.example.ui.theme.VibrantSurface
import com.example.ui.theme.VibrantTextMuted
import com.example.ui.theme.VibrantTextPrimary
import com.example.ui.theme.VibrantTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MatchHistoryDialog(
    history: List<MatchRecord>,
    onDismiss: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

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
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = VibrantLimeDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Match History",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextPrimary
                            )
                            Text(
                                text = "${history.size} recent rounds played",
                                fontSize = 11.sp,
                                color = VibrantTextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_history_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VibrantTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(VibrantCard, RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No Match Records Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = VibrantTextPrimary
                            )
                            Text(
                                text = "Complete a match in LAN Multiplayer or Solo Practice to see your match logs here!",
                                fontSize = 12.sp,
                                color = VibrantTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { record ->
                            val isWin = record.isWin
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VibrantCardLight)
                                    .border(
                                        width = 1.dp,
                                        color = if (isWin) Color(0xFFB9D363) else VibrantBorder,
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
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isWin) Color(0xFFD1ED7A) else Color(0xFFFFEBEE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isWin) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                                                contentDescription = null,
                                                tint = if (isWin) Color(0xFF3D4F00) else Color(0xFFD32F2F),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = if (isWin) "VICTORY" else "DEFEAT",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 13.sp,
                                                    color = if (isWin) Color(0xFF3D4F00) else Color(0xFFC62828)
                                                )
                                                Text(
                                                    text = "• ${record.modeName}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = VibrantTextPrimary
                                                )
                                            }
                                            Text(
                                                text = dateFormatter.format(Date(record.timestamp)),
                                                fontSize = 10.sp,
                                                color = VibrantTextMuted
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${record.linesCompleted}/${record.targetLines} Lines",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = VibrantPrimary
                                        )
                                        Text(
                                            text = "${record.durationSeconds}s • ${record.playerCount} Players",
                                            fontSize = 10.sp,
                                            color = VibrantTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
