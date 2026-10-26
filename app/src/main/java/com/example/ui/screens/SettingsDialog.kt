package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.AvatarSelector
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.AppTheme
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
fun SettingsDialog(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onDismiss: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var draftPlayerName by remember(uiState.localPlayer.name) { mutableStateOf(uiState.localPlayer.name) }
    var draftAvatarIndex by remember(uiState.localPlayer.avatarIndex) { mutableIntStateOf(uiState.localPlayer.avatarIndex) }
    var draftCustomAvatarPath by remember(uiState.localPlayer.customAvatarPath) { mutableStateOf(uiState.localPlayer.customAvatarPath) }
    var draftSelectedCustomPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val handleCancelOrDismiss = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onDismiss()
    }

    val handleDone = {
        focusManager.clearFocus()
        keyboardController?.hide()

        val trimmedName = draftPlayerName.trim().ifEmpty { "Player" }

        if (draftSelectedCustomPhotoUri != null) {
            viewModel.updatePlayerCustomPhoto(draftSelectedCustomPhotoUri!!)
            viewModel.updatePlayerProfile(trimmedName, -1)
        } else if (draftAvatarIndex >= 0) {
            viewModel.updatePlayerProfile(trimmedName, draftAvatarIndex)
        } else if (draftAvatarIndex == -1 && draftCustomAvatarPath != null) {
            viewModel.selectCustomPhotoAvatar()
            viewModel.updatePlayerProfile(trimmedName, -1)
        } else {
            viewModel.updatePlayerProfile(trimmedName, uiState.localPlayer.avatarIndex)
        }

        onDismiss()
    }

    Dialog(
        onDismissRequest = handleCancelOrDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = VibrantSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp)
                .fillMaxHeight(0.88f)
                .imePadding()
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Fixed Header
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
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = VibrantLimeDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Settings & Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary
                        )
                    }

                    IconButton(
                        onClick = handleCancelOrDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VibrantTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // SECTION 1: Player Profile & Identity
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VibrantCard, RoundedCornerShape(20.dp))
                            .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "PLAYER IDENTITY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextMuted,
                                letterSpacing = 1.sp
                            )
                        }

                        // Current Draft Profile Preview
                        val isCustomActive = (draftSelectedCustomPhotoUri != null || !draftCustomAvatarPath.isNullOrBlank()) && draftAvatarIndex == -1
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VibrantCardLight, RoundedCornerShape(14.dp))
                                .border(1.dp, VibrantBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            PlayerAvatar(
                                avatarIndex = draftAvatarIndex,
                                customAvatarPath = draftCustomAvatarPath,
                                customAvatarUri = draftSelectedCustomPhotoUri,
                                size = 42.dp
                            )
                            Column {
                                Text(
                                    text = draftPlayerName.ifBlank { "Player" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantTextPrimary
                                )
                                Text(
                                    text = "ID: #${uiState.localPlayer.id.uppercase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VibrantPrimary
                                )
                            }
                        }

                        OutlinedTextField(
                            value = draftPlayerName,
                            onValueChange = {
                                if (it.length <= 16) {
                                    draftPlayerName = it
                                }
                            },
                            label = { Text("Your Nickname") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_player_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = VibrantCardLight,
                                unfocusedContainerColor = VibrantCardLight,
                                focusedBorderColor = VibrantPrimary,
                                unfocusedBorderColor = VibrantBorder,
                                focusedLabelColor = VibrantPrimary,
                                unfocusedLabelColor = VibrantTextMuted,
                                focusedTextColor = VibrantTextPrimary,
                                unfocusedTextColor = VibrantTextPrimary,
                                cursorColor = VibrantPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Text(
                            text = "Choose Avatar or Upload Local Photo:",
                            fontSize = 11.sp,
                            color = VibrantTextMuted,
                            fontWeight = FontWeight.Medium
                        )

                        AvatarSelector(
                            selectedAvatarIndex = draftAvatarIndex,
                            customAvatarPath = draftCustomAvatarPath,
                            customAvatarUri = draftSelectedCustomPhotoUri,
                            onAvatarSelected = { index ->
                                draftAvatarIndex = index
                                draftCustomAvatarPath = null
                                draftSelectedCustomPhotoUri = null
                            },
                            onCustomPhotoSelected = { uri ->
                                draftSelectedCustomPhotoUri = uri
                                draftAvatarIndex = -1
                            },
                            onChooseCustomPhoto = {
                                draftAvatarIndex = -1
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 2: App Themes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VibrantCard, RoundedCornerShape(20.dp))
                        .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "APP THEMES & STYLES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextMuted,
                                letterSpacing = 1.sp
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(AppTheme.values()) { theme ->
                                val isSelected = uiState.selectedTheme == theme
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) theme.accentLime else VibrantCardLight)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) theme.primaryColor else VibrantBorder,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.updateTheme(theme) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(theme.primaryColor)
                                                .border(1.dp, Color.White, CircleShape)
                                        )
                                        Text(
                                            text = theme.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) theme.accentDarkText else VibrantTextPrimary
                                        )
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = theme.accentDarkText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 3: Audio & Haptics Toggles
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VibrantCard, RoundedCornerShape(20.dp))
                        .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "AUDIO & FEEDBACK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextMuted,
                                letterSpacing = 1.sp
                            )
                        }

                        // Sound switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(20.dp))
                                Text(text = "Game Sound Effects", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VibrantTextPrimary)
                            }
                            Switch(
                                checked = uiState.isSoundEnabled,
                                onCheckedChange = { viewModel.setSoundEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VibrantTextOnPrimary,
                                    checkedTrackColor = VibrantPrimary,
                                    uncheckedThumbColor = VibrantBorder,
                                    uncheckedTrackColor = VibrantCardLight
                                )
                            )
                        }

                        // Haptics switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(20.dp))
                                Text(text = "Haptic Vibration", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VibrantTextPrimary)
                            }
                            Switch(
                                checked = uiState.isHapticsEnabled,
                                onCheckedChange = { viewModel.setHapticsEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VibrantTextOnPrimary,
                                    checkedTrackColor = VibrantPrimary,
                                    uncheckedThumbColor = VibrantBorder,
                                    uncheckedTrackColor = VibrantCardLight
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reset Stats Button
                TextButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset Career Stats & History",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fixed Done Button
            Button(
                onClick = handleDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("done_settings_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
            ) {
                Text(
                    text = "Done",
                    fontWeight = FontWeight.Bold,
                    color = VibrantTextOnPrimary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Stats & Match History?", fontWeight = FontWeight.Bold) },
            text = { Text("This will clear all your games played, wins, streaks, and match logs permanently.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetCareerStats()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
