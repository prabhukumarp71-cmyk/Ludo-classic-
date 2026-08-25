package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.model.PlayerType

@Composable
fun PlayerCard(
    player: Player,
    isCurrentTurn: Boolean,
    isRolling: Boolean,
    isDiceRolled: Boolean,
    diceValue: Int,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isCurrentTurn) player.color.primaryColor else Color.Transparent,
        label = "borderColor"
    )
    val elevation by animateDpAsState(
        targetValue = if (isCurrentTurn) 8.dp else 2.dp,
        label = "elevation"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrentTurn) {
            player.color.lightColor.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        shadowElevation = elevation,
        modifier = modifier
            .border(
                width = if (isCurrentTurn) 2.5.dp else 1.dp,
                color = if (isCurrentTurn) player.color.primaryColor else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("player_card_${player.color.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Color Avatar Circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(player.color.lightColor, player.color.primaryColor)
                        )
                    )
                    .border(2.dp, player.color.darkColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (player.type == PlayerType.HUMAN) Icons.Default.Person else Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Player Name and Token Progress
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCurrentTurn) player.color.darkColor else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (player.rank > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "👑 #${player.rank}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                // 4 mini token indicator dots
                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    player.tokens.forEach { t ->
                        val tokenColor = when {
                            t.isHome -> Color(0xFF10B981) // Green for home
                            t.isOnTrack || t.isInHomeRun -> player.color.primaryColor
                            else -> Color(0xFF94A3B8) // Base gray
                        }
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(tokenColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${player.tokensHomeCount}/4",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            // Right side: Active status / Turn badge
            if (isCurrentTurn) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = player.color.primaryColor,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = if (isRolling) "ROLLING" else if (isDiceRolled) "MOVE" else "TURN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
