package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.PlayerState
import com.example.ui.theme.VibrantBackground
import com.example.ui.theme.VibrantBorder
import com.example.ui.theme.VibrantLime
import com.example.ui.theme.VibrantLimeBorder
import com.example.ui.theme.VibrantPrimary
import java.io.File

@Composable
fun PlayerAvatar(
    avatarIndex: Int,
    customAvatarPath: String? = null,
    customAvatarUri: android.net.Uri? = null,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    backgroundColor: Color = VibrantBackground,
    borderColor: Color = VibrantBorder,
    borderWidth: Dp = 1.5.dp,
    showBorder: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier

    val borderModifier = if (showBorder) {
        Modifier.border(borderWidth, borderColor, CircleShape)
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(borderModifier)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        val hasFilePhoto = !customAvatarPath.isNullOrBlank() && File(customAvatarPath).exists()
        val hasCustomPhoto = customAvatarUri != null || hasFilePhoto

        if (hasCustomPhoto && avatarIndex == -1) {
            val model: Any = customAvatarUri ?: File(customAvatarPath!!)
            AsyncImage(
                model = model,
                contentDescription = "Profile Avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val emoji = AvatarEmojis.getOrElse(avatarIndex) { "👤" }
            val fontSize = (size.value * 0.52f).sp
            Text(
                text = emoji,
                fontSize = fontSize,
                lineHeight = fontSize
            )
        }
    }
}

@Composable
fun PlayerAvatar(
    player: PlayerState,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    backgroundColor: Color = VibrantBackground,
    borderColor: Color = VibrantBorder,
    borderWidth: Dp = 1.5.dp,
    showBorder: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    PlayerAvatar(
        avatarIndex = player.avatarIndex,
        customAvatarPath = player.customAvatarPath,
        size = size,
        modifier = modifier,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        showBorder = showBorder,
        onClick = onClick
    )
}
