package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val ConfettiColors = listOf(
    Color(0xFFFFD700), // Gold
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF43F5E), // Rose
    Color(0xFF10B981), // Emerald
    Color(0xFFA855F7), // Purple
    Color(0xFFFB923C), // Orange
    Color(0xFF38BDF8)  // Sky Blue
)

private enum class ParticleShape {
    RECTANGLE,
    CIRCLE,
    STAR
}

private class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color,
    val shape: ParticleShape,
    var alpha: Float = 1f,
    val decay: Float = Random.nextFloat() * 0.003f + 0.002f
)

@Composable
fun ParticleOverlay(
    modifier: Modifier = Modifier,
    isExploding: Boolean = true,
    particleCount: Int = 120
) {
    val particles = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(isExploding) {
        if (isExploding) {
            particles.clear()
            // Spawn burst from center & top
            for (i in 0 until particleCount) {
                val angle = Random.nextDouble(0.0, Math.PI * 2)
                val speed = Random.nextFloat() * 18f + 8f
                particles.add(
                    Particle(
                        x = 500f + Random.nextFloat() * 200f - 100f,
                        y = 600f + Random.nextFloat() * 200f - 100f,
                        vx = (cos(angle) * speed).toFloat(),
                        vy = (sin(angle) * speed).toFloat() - 12f,
                        rotation = Random.nextFloat() * 360f,
                        rotationSpeed = Random.nextFloat() * 12f - 6f,
                        size = Random.nextFloat() * 14f + 8f,
                        color = ConfettiColors.random(),
                        shape = ParticleShape.values().random()
                    )
                )
            }

            while (particles.isNotEmpty()) {
                withFrameMillis { _ ->
                    val iterator = particles.listIterator()
                    while (iterator.hasNext()) {
                        val p = iterator.next()
                        p.x += p.vx
                        p.y += p.vy
                        p.vy += 0.45f // Gravity
                        p.vx *= 0.985f // Drag
                        p.rotation += p.rotationSpeed
                        p.alpha -= p.decay
                        if (p.alpha <= 0f || p.y > 3000f) {
                            iterator.remove()
                        }
                    }
                }
            }
        }
    }

    if (particles.isNotEmpty()) {
        Canvas(modifier = modifier.fillMaxSize()) {
            for (p in particles) {
                val drawColor = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f))
                rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                    when (p.shape) {
                        ParticleShape.RECTANGLE -> {
                            drawRect(
                                color = drawColor,
                                topLeft = Offset(p.x - p.size / 2, p.y - p.size / 4),
                                size = Size(p.size, p.size / 2)
                            )
                        }
                        ParticleShape.CIRCLE -> {
                            drawCircle(
                                color = drawColor,
                                radius = p.size / 2,
                                center = Offset(p.x, p.y)
                            )
                        }
                        ParticleShape.STAR -> {
                            drawRect(
                                color = drawColor,
                                topLeft = Offset(p.x - p.size / 2, p.y - p.size / 2),
                                size = Size(p.size, p.size)
                            )
                        }
                    }
                }
            }
        }
    }
}
