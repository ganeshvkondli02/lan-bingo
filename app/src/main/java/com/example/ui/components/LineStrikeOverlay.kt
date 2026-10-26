package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.example.model.CompletedLine
import com.example.model.LineType
import com.example.ui.theme.VibrantLime
import com.example.ui.theme.VibrantLimeBorder
import com.example.ui.theme.VibrantPrimary

@Composable
fun LineStrikeOverlay(
    modifier: Modifier = Modifier,
    gridSize: Int,
    completedLines: List<CompletedLine>
) {
    val progress = remember { Animatable(1f) }
    var previouslyDrawnCount by remember { mutableIntStateOf(0) }
    val limeColor = VibrantLime
    val primaryColor = VibrantPrimary
    val limeBorderColor = VibrantLimeBorder

    LaunchedEffect(completedLines.size) {
        if (completedLines.size > previouslyDrawnCount) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
            previouslyDrawnCount = completedLines.size
        } else if (completedLines.isEmpty()) {
            previouslyDrawnCount = 0
            progress.snapTo(1f)
        }
    }

    if (completedLines.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val cellWidth = width / gridSize
        val cellHeight = height / gridSize

        for ((index, line) in completedLines.withIndex()) {
            val (startOffset, endOffset) = when (line.lineType) {
                LineType.HORIZONTAL -> {
                    val centerY = (line.index + 0.5f) * cellHeight
                    Offset(0.08f * cellWidth, centerY) to Offset(width - 0.08f * cellWidth, centerY)
                }
                LineType.VERTICAL -> {
                    val centerX = (line.index + 0.5f) * cellWidth
                    Offset(centerX, 0.08f * cellHeight) to Offset(centerX, height - 0.08f * cellHeight)
                }
                LineType.DIAGONAL_MAIN -> {
                    Offset(0.08f * cellWidth, 0.08f * cellHeight) to
                        Offset(width - 0.08f * cellWidth, height - 0.08f * cellHeight)
                }
                LineType.DIAGONAL_ANTI -> {
                    Offset(width - 0.08f * cellWidth, 0.08f * cellHeight) to
                        Offset(0.08f * cellWidth, height - 0.08f * cellHeight)
                }
            }

            // Animate only newly completed lines, previously completed lines are permanently fully drawn (1f)
            val currentProgress = if (index >= previouslyDrawnCount) progress.value else 1f
            val currentEnd = Offset(
                x = startOffset.x + (endOffset.x - startOffset.x) * currentProgress,
                y = startOffset.y + (endOffset.y - startOffset.y) * currentProgress
            )

            val glowBrush = Brush.linearGradient(
                colors = listOf(limeColor, primaryColor, limeBorderColor),
                start = startOffset,
                end = currentEnd
            )

            // Outer vibrant glow line
            drawLine(
                brush = glowBrush,
                start = startOffset,
                end = currentEnd,
                strokeWidth = if (gridSize == 5) 12f else 7f,
                cap = StrokeCap.Round,
                alpha = 0.6f
            )

            // Core crisp line
            drawLine(
                color = Color.White,
                start = startOffset,
                end = currentEnd,
                strokeWidth = if (gridSize == 5) 5f else 3f,
                cap = StrokeCap.Round,
                alpha = 0.95f
            )

            // Endpoint node
            drawCircle(
                color = limeColor,
                radius = if (gridSize == 5) 7f else 4f,
                center = currentEnd
            )
        }
    }
}
