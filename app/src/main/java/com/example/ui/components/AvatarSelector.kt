package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.VibrantBackground
import com.example.ui.theme.VibrantBorder
import com.example.ui.theme.VibrantCard
import com.example.ui.theme.VibrantCardLight
import com.example.ui.theme.VibrantLime
import com.example.ui.theme.VibrantLimeBorder
import com.example.ui.theme.VibrantLimeDark
import com.example.ui.theme.VibrantPrimary
import com.example.ui.theme.VibrantTextMuted
import com.example.ui.theme.VibrantTextOnPrimary
import com.example.ui.theme.VibrantTextPrimary
import java.io.File

val AvatarEmojis = listOf("🦁", "🐼", "🦊", "🚀", "⚡", "👑", "🎯", "🐯", "🐺", "🐲")

@Composable
fun AvatarSelector(
    selectedAvatarIndex: Int,
    customAvatarPath: String?,
    customAvatarUri: android.net.Uri? = null,
    onAvatarSelected: (Int) -> Unit,
    onCustomPhotoSelected: (android.net.Uri) -> Unit,
    onChooseCustomPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onCustomPhotoSelected(uri)
        }
    }

    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onCustomPhotoSelected(uri)
        }
    }

    val launchPicker = {
        try {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (_: Exception) {
            getContentLauncher.launch("image/*")
        }
    }

    val hasFilePhoto = !customAvatarPath.isNullOrBlank() && File(customAvatarPath).exists()
    val isCustomPhotoActive = customAvatarUri != null || hasFilePhoto
    val isCustomSelected = isCustomPhotoActive && selectedAvatarIndex == -1

    Column(
        modifier = modifier
            .background(VibrantCard, RoundedCornerShape(20.dp))
            .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option 1: Custom Local Photo option
            item {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isCustomSelected) VibrantLime else VibrantBackground)
                        .border(
                            width = if (isCustomSelected) 2.5.dp else 1.dp,
                            color = if (isCustomSelected) VibrantPrimary else VibrantBorder,
                            shape = CircleShape
                        )
                        .clickable {
                            if (isCustomPhotoActive) {
                                onChooseCustomPhoto()
                            } else {
                                launchPicker()
                            }
                        }
                        .testTag("custom_photo_avatar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCustomPhotoActive) {
                        val model: Any = customAvatarUri ?: File(customAvatarPath!!)
                        AsyncImage(
                            model = model,
                            contentDescription = "Custom Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        if (isCustomSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(VibrantPrimary)
                                    .border(1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = VibrantTextOnPrimary,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    } else {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = "Add Custom Photo",
                            tint = VibrantPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Option 2: Pre-defined Avatar Emojis
            itemsIndexed(AvatarEmojis) { index, emoji ->
                val isSelected = !isCustomSelected && index == selectedAvatarIndex
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) VibrantLime else VibrantBackground
                        )
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) VibrantPrimary else VibrantBorder,
                            shape = CircleShape
                        )
                        .clickable { onAvatarSelected(index) }
                        .testTag("avatar_option_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = if (isSelected) 24.sp else 20.sp
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(VibrantPrimary)
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = VibrantTextOnPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Helper button to change / replace local photo if already selected
        if (isCustomPhotoActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VibrantCardLight)
                    .clickable { launchPicker() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = VibrantPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Upload or replace local photo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VibrantPrimary
                )
            }
        }
    }
}
