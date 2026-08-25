package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val speedX: Float,
    val speedY: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color
)

@Composable
fun ConfettiCanvas(
    modifier: Modifier = Modifier,
    particleCount: Int = 75
) {
    val colors = listOf(
        Color(0xFFE11D48),
        Color(0xFF2563EB),
        Color(0xFFF59E0B),
        Color(0xFF10B981),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899)
    )

    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                initialX = Random.nextFloat(),
                initialY = -0.1f - Random.nextFloat() * 0.5f,
                speedX = (Random.nextFloat() - 0.5f) * 0.4f,
                speedY = 0.5f + Random.nextFloat() * 0.8f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                size = 12f + Random.nextFloat() * 16f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        particles.forEach { p ->
            val px = (p.initialX + p.speedX * t) * size.width
            val py = (p.initialY + p.speedY * t) * size.height
            val rot = p.rotationSpeed * t

            rotate(degrees = rot, pivot = Offset(px, py)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(px - p.size / 2f, py - p.size / 4f),
                    size = Size(p.size, p.size / 2f)
                )
            }
        }
    }
}
