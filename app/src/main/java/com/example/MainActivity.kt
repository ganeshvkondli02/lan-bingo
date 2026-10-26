package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.theme.LocalTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VibrantBorder
import com.example.ui.theme.VibrantPrimary
import com.example.ui.theme.VibrantSurface
import com.example.ui.theme.VibrantTextPrimary
import com.example.ui.theme.VibrantTextSecondary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme(appTheme = uiState.selectedTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LocalTheme.current.background
                ) {
                    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                        BingoApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BingoApp(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = uiState.currentScreen,
            animationSpec = tween(300),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                AppScreen.HOME -> {
                    HomeScreen(viewModel = viewModel, uiState = uiState)
                }
                AppScreen.HOST_LOBBY, AppScreen.CLIENT_LOBBY -> {
                    LobbyScreen(viewModel = viewModel, uiState = uiState)
                }
                AppScreen.GAME_PLAY -> {
                    GameScreen(viewModel = viewModel, uiState = uiState)
                }
            }
        }

        // Global Overlay Dialog for Disband / Room alerts
        if (uiState.roomAlertMessage != null) {
            val alertMsg = uiState.roomAlertMessage!!
            Dialog(
                onDismissRequest = { viewModel.dismissRoomAlert() },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = VibrantSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBorder),
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .widthIn(max = 420.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = VibrantPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Room Notification",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = alertMsg,
                            fontSize = 14.sp,
                            color = VibrantTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.dismissRoomAlert() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
