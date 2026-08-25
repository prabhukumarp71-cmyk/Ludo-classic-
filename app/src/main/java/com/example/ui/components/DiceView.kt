package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LudoColor

@Composable
fun DiceView(
    diceValue: Int,
    isRolling: Boolean,
    isDiceRolled: Boolean,
    playerColor: LudoColor,
    isHumanTurn: Boolean,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp
) {
    // Rotation & wobble animation during rolling
    val infiniteTransition = rememberInfiniteTransition(label = "diceAnim")
    val rollingAngle by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rollingAngle"
    )
    val rollingScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rollingScale"
    )

    // Pulsing outline when waiting for human to roll
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val isClickable = isHumanTurn && !isRolling && !isDiceRolled

    Box(
        modifier = modifier
            .size(size + 16.dp)
            .testTag("roll_dice_container"),
        contentAlignment = Alignment.Center
    ) {
        // Glow Halo for active rollable dice
        if (isClickable) {
            Box(
                modifier = Modifier
                    .size(size + 14.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(playerColor.primaryColor.copy(alpha = 0.35f))
            )
        }

        // Dice Body
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = if (isRolling) 12.dp else 6.dp,
            modifier = Modifier
                .size(size)
                .rotate(if (isRolling) rollingAngle else 0f)
                .scale(if (isRolling) rollingScale else 1f)
                .border(
                    width = 2.5.dp,
                    color = if (isClickable) playerColor.primaryColor else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = isClickable,
                    onClick = onRollClick
                )
                .testTag("roll_dice_button")
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                val pipRadius = this.size.minDimension * 0.12f
                val pipColor = if (diceValue == 6) playerColor.primaryColor else Color(0xFF1E293B)

                val left = this.size.width * 0.22f
                val centerX = this.size.width * 0.5f
                val right = this.size.width * 0.78f

                val top = this.size.height * 0.22f
                val centerY = this.size.height * 0.5f
                val bottom = this.size.height * 0.78f

                when (diceValue) {
                    1 -> {
                        drawCircle(pipColor, pipRadius * 1.35f, Offset(centerX, centerY))
                    }
                    2 -> {
                        drawCircle(pipColor, pipRadius, Offset(left, top))
                        drawCircle(pipColor, pipRadius, Offset(right, bottom))
                    }
                    3 -> {
                        drawCircle(pipColor, pipRadius, Offset(left, top))
                        drawCircle(pipColor, pipRadius, Offset(centerX, centerY))
                        drawCircle(pipColor, pipRadius, Offset(right, bottom))
                    }
                    4 -> {
                        drawCircle(pipColor, pipRadius, Offset(left, top))
                        drawCircle(pipColor, pipRadius, Offset(right, top))
                        drawCircle(pipColor, pipRadius, Offset(left, bottom))
                        drawCircle(pipColor, pipRadius, Offset(right, bottom))
                    }
                    5 -> {
                        drawCircle(pipColor, pipRadius, Offset(left, top))
                        drawCircle(pipColor, pipRadius, Offset(right, top))
                        drawCircle(pipColor, pipRadius, Offset(centerX, centerY))
                        drawCircle(pipColor, pipRadius, Offset(left, bottom))
                        drawCircle(pipColor, pipRadius, Offset(right, bottom))
                    }
                    6 -> {
                        drawCircle(pipColor, pipRadius, Offset(left, top))
                        drawCircle(pipColor, pipRadius, Offset(right, top))
                        drawCircle(pipColor, pipRadius, Offset(left, centerY))
                        drawCircle(pipColor, pipRadius, Offset(right, centerY))
                        drawCircle(pipColor, pipRadius, Offset(left, bottom))
                        drawCircle(pipColor, pipRadius, Offset(right, bottom))
                    }
                }
            }
        }
    }
}
