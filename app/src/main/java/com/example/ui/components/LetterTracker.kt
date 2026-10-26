package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VibrantBorder
import com.example.ui.theme.VibrantCard
import com.example.ui.theme.VibrantLime
import com.example.ui.theme.VibrantLimeBorder
import com.example.ui.theme.VibrantLimeDark
import com.example.ui.theme.VibrantTextSecondary

@Composable
fun LetterTracker(
    targetWord: String,
    completedLinesCount: Int,
    modifier: Modifier = Modifier
) {
    val illuminatedCount = completedLinesCount.coerceAtMost(targetWord.length)
    val compact = targetWord.length > 5

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in targetWord.indices) {
            val letter = targetWord[i]
            val isUnlocked = i < illuminatedCount

            LetterPill(
                letter = letter,
                isUnlocked = isUnlocked,
                compact = compact,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LetterPill(
    letter: Char,
    isUnlocked: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isUnlocked) {
        if (isUnlocked) {
            scaleAnim.animateTo(
                targetValue = 1.15f,
                animationSpec = keyframes {
                    durationMillis = 300
                    1.0f at 0
                    1.15f at 140 using FastOutSlowInEasing
                    1.0f at 300
                }
            )
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isUnlocked) VibrantLime else VibrantCard,
        animationSpec = tween(300),
        label = "letterBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isUnlocked) VibrantLimeDark else VibrantTextSecondary,
        animationSpec = tween(300),
        label = "letterText"
    )

    val pillHeight = if (compact) 22.dp else 26.dp
    val fontSize = if (compact) 10.sp else 12.sp
    val cornerShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .height(pillHeight)
            .scale(scaleAnim.value)
            .then(
                if (isUnlocked) {
                    Modifier.shadow(2.dp, cornerShape, spotColor = VibrantLimeBorder)
                } else Modifier.alpha(0.7f)
            )
            .clip(cornerShape)
            .background(backgroundColor)
            .then(
                if (isUnlocked) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = VibrantLimeBorder,
                        shape = cornerShape
                    )
                } else {
                    Modifier.border(
                        width = 0.8.dp,
                        color = VibrantBorder,
                        shape = cornerShape
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Black
        )
    }
}
