package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DifficultyLevel
import com.example.model.GameMode
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

import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

enum class SetupActionType {
    CREATE_HOST_LOBBY,
    PRACTICE_SOLO
}

@Composable
fun GameModeDifficultyDialog(
    actionType: SetupActionType,
    initialMode: GameMode,
    initialDifficulty: DifficultyLevel,
    onConfirm: (GameMode, DifficultyLevel, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(initialMode) }
    var selectedDifficulty by remember { mutableStateOf(initialDifficulty) }
    var roomPassword by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp)
                .imePadding()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, VibrantBorder, RoundedCornerShape(24.dp))
                .testTag("mode_difficulty_dialog"),
            color = VibrantSurface
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
                    Text(
                        text = if (actionType == SetupActionType.CREATE_HOST_LOBBY) "Host Game Setup" else "Solo Practice Setup",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = VibrantTextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_mode_difficulty_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = VibrantTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 1: GRID SIZE (5x5 or 10x10)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = null,
                        tint = VibrantPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "1. SELECT GRID SIZE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = VibrantPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 5x5 Option
                    val is5x5 = selectedMode == GameMode.CLASSIC_5X5
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (is5x5) VibrantLime.copy(alpha = 0.35f) else VibrantCard)
                            .border(
                                width = if (is5x5) 2.dp else 1.dp,
                                color = if (is5x5) VibrantLimeBorder else VibrantBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedMode = GameMode.CLASSIC_5X5 }
                            .padding(vertical = 16.dp, horizontal = 14.dp)
                            .testTag("mode_5x5_chip"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "5 × 5",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (is5x5) VibrantLimeDark else VibrantTextPrimary
                        )
                    }

                    // 10x10 Option
                    val is10x10 = selectedMode == GameMode.EXTRA_10X10
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (is10x10) VibrantLime.copy(alpha = 0.35f) else VibrantCard)
                            .border(
                                width = if (is10x10) 2.dp else 1.dp,
                                color = if (is10x10) VibrantLimeBorder else VibrantBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedMode = GameMode.EXTRA_10X10 }
                            .padding(vertical = 16.dp, horizontal = 14.dp)
                            .testTag("mode_10x10_chip"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "10 × 10",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (is10x10) VibrantLimeDark else VibrantTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 2: DIFFICULTY LEVELS (Low, Medium, High)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = VibrantPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "2. BOT DIFFICULTY LEVEL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = VibrantPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyLevel.values().forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        val icon = when (diff) {
                            DifficultyLevel.LOW -> Icons.Default.Shuffle
                            DifficultyLevel.MEDIUM -> Icons.Default.Speed
                            DifficultyLevel.HIGH -> Icons.Default.AutoAwesome
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) VibrantLime.copy(alpha = 0.35f) else VibrantCard)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) VibrantLimeBorder else VibrantBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedDifficulty = diff }
                                .padding(12.dp)
                                .testTag("difficulty_${diff.name.lowercase()}"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) VibrantLime else VibrantSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) VibrantLimeDark else VibrantPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = diff.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) VibrantLimeDark else VibrantTextPrimary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) VibrantLimeDark else VibrantTextMuted.copy(alpha = 0.2f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = diff.tag,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) VibrantLime else VibrantTextSecondary
                                            )
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(VibrantLimeDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = VibrantLime,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (actionType == SetupActionType.CREATE_HOST_LOBBY) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 3: ROOM PASSWORD (OPTIONAL)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = VibrantPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "3. ROOM PASSWORD (OPTIONAL)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = VibrantPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = roomPassword,
                        onValueChange = { roomPassword = it.take(20) },
                        placeholder = { Text("Leave empty for open room", fontSize = 13.sp, color = VibrantTextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VibrantPrimary,
                            unfocusedBorderColor = VibrantBorder,
                            focusedTextColor = VibrantTextPrimary,
                            unfocusedTextColor = VibrantTextPrimary,
                            focusedContainerColor = VibrantCardLight,
                            unfocusedContainerColor = VibrantCardLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("host_room_password_input")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm CTA Button
                Button(
                    onClick = {
                        onConfirm(selectedMode, selectedDifficulty, roomPassword.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_setup_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantPrimary,
                        contentColor = VibrantTextOnPrimary
                    )
                ) {
                    Text(
                        text = if (actionType == SetupActionType.CREATE_HOST_LOBBY) "Create Lobby & Add Players" else "Start Solo Match",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
