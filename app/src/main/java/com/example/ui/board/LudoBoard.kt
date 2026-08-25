package com.example.ui.board

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.viewmodel.AnimatingTokenState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LudoBoard(
    players: List<Player>,
    currentPlayerIndex: Int,
    legalTokens: List<Int>,
    animatingToken: AnimatingTokenState?,
    onTokenClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPlayer = players.getOrNull(currentPlayerIndex)
    val isHumanTurn = currentPlayer?.type == PlayerType.HUMAN

    // Pulsing animation for movable tokens
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(16.dp, shape = MaterialTheme.shapes.extraLarge)
            .clip(MaterialTheme.shapes.extraLarge)
            .testTag("ludo_board_container")
    ) {
        val boardSize = maxWidth
        val cellSize = boardSize / 15f

        // 1. Draw static Board background, pathways, bases, and stars
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLudoBoard(size)
        }

        // 2. Draw active tokens on board
        // Group tokens by their grid location to handle stacking
        val allTokensWithPos = remember(players, animatingToken) {
            val list = mutableListOf<Triple<Player, Token, GridPos>>()
            players.forEach { p ->
                p.tokens.forEach { t ->
                    // If this token is currently animating, use its animated position
                    val isCurrentAnimating = animatingToken != null &&
                            animatingToken.playerColor == p.color &&
                            animatingToken.tokenId == t.id

                    val pos = if (isCurrentAnimating && animatingToken.intermediatePositions.isNotEmpty()) {
                        animatingToken.intermediatePositions.getOrElse(animatingToken.currentAnimIndex) {
                            BoardConstants.getTokenGridPos(t)
                        }
                    } else {
                        BoardConstants.getTokenGridPos(t)
                    }
                    list.add(Triple(p, t, pos))
                }
            }
            list
        }

        // Render token elements
        allTokensWithPos.forEach { (player, token, pos) ->
            val isCurrentPlayerToken = currentPlayer?.id == player.id
            val isLegal = isCurrentPlayerToken && legalTokens.contains(token.id) && animatingToken == null

            // Calculate exact pixel offset for this token
            val xOffset = cellSize * pos.x
            val yOffset = cellSize * pos.y

            Box(
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
                    .size(cellSize)
                    .padding(1.5.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLegal) {
                    // Pulsing selection aura
                    Box(
                        modifier = Modifier
                            .fillMaxSize(pulseScale)
                            .alpha(pulseAlpha)
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            player.color.primaryColor.copy(alpha = 0.8f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            }
                    )
                }

                // Token Body
                TokenView(
                    color = player.color,
                    tokenId = token.id,
                    isLegal = isLegal,
                    isAnimating = animatingToken?.tokenId == token.id && animatingToken.playerColor == player.color,
                    onClick = {
                        if (isLegal && isHumanTurn) {
                            onTokenClicked(token.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TokenView(
    color: LudoColor,
    tokenId: Int,
    isLegal: Boolean,
    isAnimating: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxSize(0.85f)
            .shadow(
                elevation = if (isAnimating) 10.dp else if (isLegal) 6.dp else 3.dp,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = isLegal,
                onClick = onClick
            )
            .testTag("token_${color.name.lowercase()}_$tokenId")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer ring
            drawCircle(
                color = Color.White,
                radius = r,
                center = center
            )

            // Outer stroke
            drawCircle(
                color = color.darkColor,
                radius = r,
                center = center,
                style = Stroke(width = r * 0.15f)
            )

            // Gradient fill
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.lightColor,
                        color.primaryColor,
                        color.darkColor
                    ),
                    center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                    radius = r * 1.1f
                ),
                radius = r * 0.82f,
                center = center
            )

            // Center crown / dot
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = r * 0.32f,
                center = center
            )
            drawCircle(
                color = color.darkColor,
                radius = r * 0.20f,
                center = center
            )
        }
    }
}

private fun DrawScope.drawLudoBoard(boardSize: Size) {
    val cellW = boardSize.width / 15f
    val cellH = boardSize.height / 15f

    // Board Base Background
    drawRect(color = Color(0xFFF8FAFC), size = boardSize)

    // Draw Base Quadrants (6x6 cells each)
    // 1. Green Base (Top-Left: 0..5, 0..5)
    drawBaseYard(
        topLeft = Offset(0f, 0f),
        size = Size(cellW * 6f, cellH * 6f),
        color = LudoColor.GREEN,
        cellW = cellW,
        cellH = cellH
    )

    // 2. Yellow Base (Top-Right: 9..14, 0..5)
    drawBaseYard(
        topLeft = Offset(cellW * 9f, 0f),
        size = Size(cellW * 6f, cellH * 6f),
        color = LudoColor.YELLOW,
        cellW = cellW,
        cellH = cellH
    )

    // 3. Red Base (Bottom-Left: 0..5, 9..14)
    drawBaseYard(
        topLeft = Offset(0f, cellH * 9f),
        size = Size(cellW * 6f, cellH * 6f),
        color = LudoColor.RED,
        cellW = cellW,
        cellH = cellH
    )

    // 4. Blue Base (Bottom-Right: 9..14, 9..14)
    drawBaseYard(
        topLeft = Offset(cellW * 9f, cellH * 9f),
        size = Size(cellW * 6f, cellH * 6f),
        color = LudoColor.BLUE,
        cellW = cellW,
        cellH = cellH
    )

    // Draw Pathways & Grid Lines
    drawGridPaths(cellW, cellH)

    // Draw Center Finish Zone (3x3 cells: 6..8, 6..8)
    drawCenterFinishZone(cellW, cellH)

    // Draw Safe Star squares
    drawSafeStars(cellW, cellH)
}

private fun DrawScope.drawBaseYard(
    topLeft: Offset,
    size: Size,
    color: LudoColor,
    cellW: Float,
    cellH: Float
) {
    // Quadrant border & filled background
    drawRect(
        color = color.primaryColor,
        topLeft = topLeft,
        size = size
    )

    // Inner White Yard Area (4x4 cells inside 6x6)
    val innerMarginW = cellW * 0.9f
    val innerMarginH = cellH * 0.9f
    val innerTopLeft = Offset(topLeft.x + innerMarginW, topLeft.y + innerMarginH)
    val innerSize = Size(size.width - innerMarginW * 2f, size.height - innerMarginH * 2f)

    drawRoundRect(
        color = Color(0xFFFFFFFF),
        topLeft = innerTopLeft,
        size = innerSize,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )

    // Inner Yard Border
    drawRoundRect(
        color = color.darkColor.copy(alpha = 0.35f),
        topLeft = innerTopLeft,
        size = innerSize,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
        style = Stroke(width = 3f)
    )

    // 4 Token circles inside Yard
    val slotOffsets = listOf(
        Pair(1.6f, 1.6f),
        Pair(3.8f, 1.6f),
        Pair(1.6f, 3.8f),
        Pair(3.8f, 3.8f)
    )

    for ((ox, oy) in slotOffsets) {
        val slotCenter = Offset(topLeft.x + ox * cellW + cellW * 0.5f, topLeft.y + oy * cellH + cellH * 0.5f)
        val radius = cellW * 0.44f

        drawCircle(
            color = color.lightColor,
            radius = radius,
            center = slotCenter
        )
        drawCircle(
            color = color.primaryColor,
            radius = radius,
            center = slotCenter,
            style = Stroke(width = 3f)
        )
        drawCircle(
            color = color.primaryColor.copy(alpha = 0.5f),
            radius = radius * 0.4f,
            center = slotCenter
        )
    }
}

private fun DrawScope.drawGridPaths(cellW: Float, cellH: Float) {
    val gridBorderColor = Color(0xFF94A3B8)
    val strokeWidth = 1.5f

    // Draw all individual path cells (Top Arm, Bottom Arm, Left Arm, Right Arm)
    for (x in 0..14) {
        for (y in 0..14) {
            val isBaseYard = (x < 6 && y < 6) || (x > 8 && y < 6) || (x < 6 && y > 8) || (x > 8 && y > 8)
            val isCenter = x in 6..8 && y in 6..8
            if (isBaseYard || isCenter) continue

            val cellTopLeft = Offset(x * cellW, y * cellH)
            val cellSize = Size(cellW, cellH)

            // Check colored tiles: Home Run columns & Starting squares
            val fillColor = when {
                // Red Start: (1, 8)
                x == 1 && y == 8 -> LudoColor.RED.primaryColor
                // Red Home Run: Row 7, columns 1..5
                y == 7 && x in 1..5 -> LudoColor.RED.primaryColor
                // Blue Start: (8, 13)
                x == 8 && y == 13 -> LudoColor.BLUE.primaryColor
                // Blue Home Run: Column 7, rows 9..13
                x == 7 && y in 9..13 -> LudoColor.BLUE.primaryColor
                // Yellow Start: (13, 6)
                x == 13 && y == 6 -> LudoColor.YELLOW.primaryColor
                // Yellow Home Run: Row 7, columns 9..13
                y == 7 && x in 9..13 -> LudoColor.YELLOW.primaryColor
                // Green Start: (6, 1)
                x == 6 && y == 1 -> LudoColor.GREEN.primaryColor
                // Green Home Run: Column 7, rows 1..5
                x == 7 && y in 1..5 -> LudoColor.GREEN.primaryColor
                else -> Color.White
            }

            drawRect(color = fillColor, topLeft = cellTopLeft, size = cellSize)

            // Cell border
            drawRect(
                color = gridBorderColor,
                topLeft = cellTopLeft,
                size = cellSize,
                style = Stroke(width = strokeWidth)
            )

            // Add directional start arrows on start squares
            if ((x == 1 && y == 8) || (x == 8 && y == 13) || (x == 13 && y == 6) || (x == 6 && y == 1)) {
                drawStartArrow(cellTopLeft, cellSize, x, y)
            }
        }
    }
}

private fun DrawScope.drawStartArrow(topLeft: Offset, size: Size, x: Int, y: Int) {
    val path = Path()
    val cx = topLeft.x + size.width / 2f
    val cy = topLeft.y + size.height / 2f
    val r = size.width * 0.28f

    when {
        // Red start (1,8) points right
        x == 1 && y == 8 -> {
            path.moveTo(cx - r, cy - r)
            path.lineTo(cx + r, cy)
            path.lineTo(cx - r, cy + r)
        }
        // Blue start (8,13) points up
        x == 8 && y == 13 -> {
            path.moveTo(cx - r, cy + r)
            path.lineTo(cx, cy - r)
            path.lineTo(cx + r, cy + r)
        }
        // Yellow start (13,6) points left
        x == 13 && y == 6 -> {
            path.moveTo(cx + r, cy - r)
            path.lineTo(cx - r, cy)
            path.lineTo(cx + r, cy + r)
        }
        // Green start (6,1) points down
        x == 6 && y == 1 -> {
            path.moveTo(cx - r, cy - r)
            path.lineTo(cx, cy + r)
            path.lineTo(cx + r, cy - r)
        }
    }
    path.close()
    drawPath(path, color = Color.White, style = Fill)
}

private fun DrawScope.drawCenterFinishZone(cellW: Float, cellH: Float) {
    val cx = cellW * 7.5f
    val cy = cellH * 7.5f

    // Center 3x3 box coordinates: (6..8, 6..8)
    val left = cellW * 6f
    val right = cellW * 9f
    val top = cellH * 6f
    val bottom = cellH * 9f

    // 4 Triangles meeting at center (cx, cy)
    // 1. Green Triangle (Top)
    val greenPath = Path().apply {
        moveTo(left, top)
        lineTo(right, top)
        lineTo(cx, cy)
        close()
    }
    drawPath(greenPath, color = LudoColor.GREEN.primaryColor)

    // 2. Yellow Triangle (Right)
    val yellowPath = Path().apply {
        moveTo(right, top)
        lineTo(right, bottom)
        lineTo(cx, cy)
        close()
    }
    drawPath(yellowPath, color = LudoColor.YELLOW.primaryColor)

    // 3. Blue Triangle (Bottom)
    val bluePath = Path().apply {
        moveTo(right, bottom)
        lineTo(left, bottom)
        lineTo(cx, cy)
        close()
    }
    drawPath(bluePath, color = LudoColor.BLUE.primaryColor)

    // 4. Red Triangle (Left)
    val redPath = Path().apply {
        moveTo(left, bottom)
        lineTo(left, top)
        lineTo(cx, cy)
        close()
    }
    drawPath(redPath, color = LudoColor.RED.primaryColor)

    // Center Gold Crown Circle
    drawCircle(
        color = Color(0xFFFBBF24),
        radius = cellW * 0.7f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color(0xFFD97706),
        radius = cellW * 0.7f,
        center = Offset(cx, cy),
        style = Stroke(width = 3f)
    )
    drawCircle(
        color = Color(0xFFFFFFFF),
        radius = cellW * 0.4f,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawSafeStars(cellW: Float, cellH: Float) {
    val starCoords = listOf(
        Pair(6, 12), // Star Bottom-Left
        Pair(12, 8), // Star Bottom-Right
        Pair(8, 2),  // Star Top-Right
        Pair(2, 6)   // Star Top-Left
    )

    for ((gx, gy) in starCoords) {
        val cx = gx * cellW + cellW / 2f
        val cy = gy * cellH + cellH / 2f
        val r = cellW * 0.38f
        drawStarShape(Offset(cx, cy), r, Color(0xFF475569))
    }
}

private fun DrawScope.drawStarShape(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.42f
    val points = 5
    var angle = -Math.PI / 2.0
    val step = Math.PI / points

    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val x = (center.x + cos(angle) * r).toFloat()
        val y = (center.y + sin(angle) * r).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += step
    }
    path.close()
    drawPath(path, color = color, style = Fill)
}
