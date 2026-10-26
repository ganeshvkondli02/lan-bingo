package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CellWifi
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LobbyDiscoveryInfo
import com.example.ui.components.AvatarEmojis
import com.example.ui.components.PlayerAvatar
import com.example.ui.components.QrScannerDialog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.VibrantAmber
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
fun HomeScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showJoinDialog by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }
    var roomCodeInput by remember { mutableStateOf("") }
    var passwordPromptLobby by remember { mutableStateOf<LobbyDiscoveryInfo?>(null) }
    var enteredLobbyPassword by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHowToPlayDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showContactUsDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showMultiplayerRoadmap by remember { mutableStateOf(false) }
    var showModeDifficultyDialog by remember { mutableStateOf(false) }
    var showExitAppConfirmDialog by remember { mutableStateOf(false) }
    var pendingActionType by remember { mutableStateOf<SetupActionType?>(null) }

    val handleDismissJoinDialog = {
        focusManager.clearFocus()
        keyboardController?.hide()
        showJoinDialog = false
        showQrScanner = false
        viewModel.stopSearchingLobbies()
    }

    // Android back navigation handler
    BackHandler {
        when {
            showQrScanner -> showQrScanner = false
            showHowToPlayDialog -> showHowToPlayDialog = false
            showContactUsDialog -> showContactUsDialog = false
            showStatsDialog -> showStatsDialog = false
            showHistoryDialog -> showHistoryDialog = false
            showSettingsDialog -> showSettingsDialog = false
            showMultiplayerRoadmap -> showMultiplayerRoadmap = false
            showModeDifficultyDialog -> {
                showModeDifficultyDialog = false
                pendingActionType = null
            }
            showJoinDialog -> handleDismissJoinDialog()
            passwordPromptLobby != null -> passwordPromptLobby = null
            showExitAppConfirmDialog -> showExitAppConfirmDialog = false
            else -> showExitAppConfirmDialog = true
        }
    }

    val avatarEmoji = AvatarEmojis.getOrElse(uiState.localPlayer.avatarIndex) { "👤" }

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
            // Top Bar with Player Profile (round logo + plain text name) and Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player round logo and name beside it (plain text no division)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .clickable { showSettingsDialog = true }
                        .testTag("home_player_profile_header")
                ) {
                    PlayerAvatar(
                        avatarIndex = uiState.localPlayer.avatarIndex,
                        customAvatarPath = uiState.localPlayer.customAvatarPath,
                        size = 42.dp
                    )
                    Text(
                        text = uiState.localPlayer.name,
                        color = VibrantTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sound & Settings Controls
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VibrantCard)
                            .testTag("toggle_sound_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Toggle Sound",
                            tint = if (uiState.isSoundEnabled) VibrantPrimary else VibrantTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VibrantLime)
                            .testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = VibrantLimeDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Styled BINGO Title
            Text(
                text = "BINGO",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                color = VibrantTextPrimary,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .testTag("bingo_title_heading")
            )

            // Connection Error Banner if present
            if (uiState.connectionError != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Connection Notice",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Text(
                                text = uiState.connectionError ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFFB71C1C)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissConnectionError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Connecting indicator if active
            if (uiState.isConnecting) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VibrantLime.copy(alpha = 0.25f))
                        .border(1.dp, VibrantLimeBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = VibrantPrimary
                        )
                        Text(
                            text = "Connecting to game host...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VibrantPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MAIN ACTION TABS (Themed Action Cards as before)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. CREATE LAN LOBBY (Host Game)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(VibrantCard)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(24.dp))
                        .clickable {
                            pendingActionType = SetupActionType.CREATE_HOST_LOBBY
                            showModeDifficultyDialog = true
                        }
                        .padding(18.dp)
                        .testTag("host_game_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VibrantSurface)
                                    .border(1.dp, VibrantBorder, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = VibrantPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Create LAN Lobby",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = VibrantTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 2. FIND & JOIN LAN GAME
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(VibrantCard)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(24.dp))
                        .clickable {
                            viewModel.startSearchingLobbies()
                            showJoinDialog = true
                        }
                        .padding(18.dp)
                        .testTag("join_game_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VibrantSurface)
                                    .border(1.dp, VibrantBorder, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = VibrantPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Find & Join Game",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = VibrantTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 3. SOLO PRACTICE VS AI BOT
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(VibrantCard)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(24.dp))
                        .clickable {
                            pendingActionType = SetupActionType.PRACTICE_SOLO
                            showModeDifficultyDialog = true
                        }
                        .padding(18.dp)
                        .testTag("solo_practice_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VibrantSurface)
                                    .border(1.dp, VibrantBorder, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = VibrantPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Quick Practice vs Bot",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = VibrantTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 4. ONLINE MULTIPLAYER (Hub)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(VibrantCardLight)
                        .border(1.5.dp, VibrantAmber.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .clickable { showMultiplayerRoadmap = true }
                        .padding(18.dp)
                        .testTag("online_multiplayer_tab")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VibrantAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = VibrantAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
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
                                        .background(VibrantAmber, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ROADMAP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = VibrantAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION: THREE CARDS (How To Play, Statistics, Contact Us)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: How To Play
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(VibrantCard)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(18.dp))
                        .clickable { showHowToPlayDialog = true }
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                        .testTag("how_to_play_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VibrantSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "How To Play",
                                tint = VibrantPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "How To Play",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Card 2: Statistics
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(VibrantCard)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(18.dp))
                        .clickable { showStatsDialog = true }
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                        .testTag("stats_tab_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VibrantSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Statistics",
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Statistics",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Card 3: Contact Us
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(VibrantCard)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(18.dp))
                        .clickable { showContactUsDialog = true }
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                        .testTag("contact_us_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VibrantSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Contact Us",
                                tint = Color(0xFFBA68C8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Contact Us",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SINGLE SUMMARY PILL ROW (Played, Won, Streak)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(VibrantCard)
                    .border(1.dp, VibrantBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("win_streak_indicator")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Played
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Played: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = VibrantTextMuted
                        )
                        Text(
                            text = "${uiState.careerStats.gamesPlayed}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary
                        )
                    }

                    // Won
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Won: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = VibrantTextMuted
                        )
                        Text(
                            text = "${uiState.careerStats.gamesWon}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary
                        )
                        Text(
                            text = " (${uiState.careerStats.winRatePercent}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = VibrantTextMuted
                        )
                    }

                    // Streak
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Streak: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = VibrantTextMuted
                        )
                        Text(
                            text = "${uiState.careerStats.currentStreak} 🔥",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Search & Join Dialog (Discover & Join Room)
        if (showJoinDialog) {
            Dialog(
                onDismissRequest = handleDismissJoinDialog,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = VibrantSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBorder),
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .widthIn(max = 500.dp)
                        .imePadding()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top Header
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(VibrantCard),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = VibrantPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = "Discover & Join\nRoom",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantTextPrimary,
                                    lineHeight = 22.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.refreshLobbies(3000L) },
                                    modifier = Modifier.testTag("refresh_lobbies_button")
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = if (uiState.isSearchingLobbies) VibrantPrimary else VibrantTextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = handleDismissJoinDialog
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = VibrantTextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Search / Room Code Input Row with Join and QR Scan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = roomCodeInput,
                                onValueChange = { roomCodeInput = it },
                                placeholder = {
                                    Text(
                                        "ENTER ROOM CODE (E.g. BNG-1234)",
                                        color = VibrantTextMuted,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_code_input"),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = VibrantCard,
                                    unfocusedContainerColor = VibrantCard,
                                    focusedBorderColor = VibrantPrimary,
                                    unfocusedBorderColor = VibrantBorder,
                                    focusedTextColor = VibrantTextPrimary,
                                    unfocusedTextColor = VibrantTextPrimary
                                ),
                                textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (roomCodeInput.isNotBlank()) {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            viewModel.connectByRoomCodeOrIp(roomCodeInput)
                                            showJoinDialog = false
                                        }
                                    }
                                )
                            )

                            Button(
                                onClick = {
                                    if (roomCodeInput.isNotBlank()) {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        viewModel.connectByRoomCodeOrIp(roomCodeInput)
                                        showJoinDialog = false
                                    }
                                },
                                enabled = roomCodeInput.isNotBlank(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VibrantPrimary,
                                    contentColor = VibrantTextOnPrimary,
                                    disabledContainerColor = VibrantCard,
                                    disabledContentColor = VibrantTextMuted
                                ),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("join_by_code_button")
                            ) {
                                Text("Join", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            IconButton(
                                onClick = {
                                    showQrScanner = true
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VibrantCard)
                                    .border(1.dp, VibrantBorder, RoundedCornerShape(16.dp))
                                    .testTag("scan_qr_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan QR Code",
                                    tint = VibrantPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // ACTIVE LAN ROOMS Subheader
                        Text(
                            text = "ACTIVE LAN ROOMS (${uiState.discoveredLobbies.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextSecondary,
                            letterSpacing = 1.sp
                        )

                        // Big Card for LAN Rooms (Empty or List)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(VibrantCard)
                                .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.discoveredLobbies.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    uiState.discoveredLobbies.forEach { lobby ->
                                        val onSelectLobby = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            if (lobby.hasPassword) {
                                                passwordPromptLobby = lobby
                                                enteredLobbyPassword = ""
                                            } else {
                                                viewModel.connectToLobby(lobby.hostIp, lobby.port)
                                                showJoinDialog = false
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(VibrantSurface)
                                                .border(1.dp, VibrantBorder, RoundedCornerShape(14.dp))
                                                .clickable { onSelectLobby() }
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "${lobby.hostName}'s Room",
                                                            fontWeight = FontWeight.Bold,
                                                            color = VibrantTextPrimary,
                                                            fontSize = 14.sp
                                                        )
                                                        if (lobby.roomCode.isNotBlank()) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(VibrantPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = lobby.roomCode,
                                                                    color = VibrantPrimary,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                        if (lobby.hasPassword) {
                                                            Icon(
                                                                imageVector = Icons.Default.Lock,
                                                                contentDescription = "Password Protected",
                                                                tint = VibrantAmber,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "${lobby.mode.title} • ${lobby.playerCount}/${lobby.maxPlayers} Players • ${lobby.hostIp}",
                                                        color = VibrantTextMuted,
                                                        fontSize = 11.sp
                                                    )
                                                }

                                                Button(
                                                    onClick = { onSelectLobby() },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = VibrantPrimary,
                                                        contentColor = VibrantTextOnPrimary
                                                    )
                                                ) {
                                                    Text("Join", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp, horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No rooms found",
                                        color = VibrantTextMuted,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // QR Scanner Dialog
        if (showQrScanner) {
            QrScannerDialog(
                onDismiss = { showQrScanner = false },
                onQrScanned = { scannedCode ->
                    showQrScanner = false
                    showJoinDialog = false
                    viewModel.connectByRoomCodeOrIp(scannedCode)
                }
            )
        }

        // Password Prompt Dialog when joining password-protected lobby
        if (passwordPromptLobby != null) {
            val lobby = passwordPromptLobby!!
            Dialog(
                onDismissRequest = { passwordPromptLobby = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = VibrantSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBorder),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .widthIn(max = 440.dp)
                        .imePadding()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Enter Room Password",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextPrimary
                            )
                            IconButton(onClick = { passwordPromptLobby = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = VibrantTextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "This room (${lobby.hostName}) is protected by a password.",
                            fontSize = 13.sp,
                            color = VibrantTextSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        androidx.compose.material3.OutlinedTextField(
                            value = enteredLobbyPassword,
                            onValueChange = { enteredLobbyPassword = it },
                            placeholder = { Text("Password", fontSize = 13.sp, color = VibrantTextMuted) },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { passwordPromptLobby = null },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantCard)
                            ) {
                                Text("Cancel", color = VibrantTextSecondary)
                            }
                            Button(
                                onClick = {
                                    val pw = enteredLobbyPassword.trim()
                                    passwordPromptLobby = null
                                    showJoinDialog = false
                                    viewModel.connectToLobby(lobby.hostIp, lobby.port, pw)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
                            ) {
                                Text("Join Room", color = VibrantTextOnPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showHowToPlayDialog) {
            HowToPlayDialog(
                onDismiss = { showHowToPlayDialog = false }
            )
        }

        if (showContactUsDialog) {
            ContactUsDialog(
                onDismiss = { showContactUsDialog = false }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                viewModel = viewModel,
                uiState = uiState,
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showHistoryDialog) {
            MatchHistoryDialog(
                history = uiState.matchHistory,
                onDismiss = { showHistoryDialog = false }
            )
        }

        if (showStatsDialog) {
            CareerStatsDialog(
                stats = uiState.careerStats,
                history = uiState.matchHistory,
                onDismiss = { showStatsDialog = false }
            )
        }

        if (showMultiplayerRoadmap) {
            MultiplayerRoadmapDialog(
                onDismiss = { showMultiplayerRoadmap = false }
            )
        }

        if (showModeDifficultyDialog && pendingActionType != null) {
            GameModeDifficultyDialog(
                actionType = pendingActionType!!,
                initialMode = uiState.selectedGameMode,
                initialDifficulty = uiState.selectedDifficulty,
                onConfirm = { mode, difficulty, password ->
                    val action = pendingActionType
                    showModeDifficultyDialog = false
                    pendingActionType = null
                    if (action == SetupActionType.CREATE_HOST_LOBBY) {
                        viewModel.startHostingLobby(mode, difficulty, password)
                    } else {
                        viewModel.startSoloPractice(mode, difficulty)
                    }
                },
                onDismiss = {
                    showModeDifficultyDialog = false
                    pendingActionType = null
                }
            )
        }

        // Exit App Confirmation Dialog for Back Navigation from Home
        if (showExitAppConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showExitAppConfirmDialog = false },
                title = {
                    Text("Exit Bingo?", fontWeight = FontWeight.Bold, color = VibrantTextPrimary)
                },
                text = {
                    Text("Are you sure you want to close the application?", color = VibrantTextSecondary)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitAppConfirmDialog = false
                            (context as? android.app.Activity)?.finish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Exit", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showExitAppConfirmDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantCard)
                    ) {
                        Text("Cancel", color = VibrantTextSecondary)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = VibrantSurface
            )
        }
    }
}
