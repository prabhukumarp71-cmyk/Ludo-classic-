package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LudoColor

@Composable
fun MenuScreen(
    hasSavedGame: Boolean,
    isSoundMuted: Boolean,
    isFastMode: Boolean,
    onResumeGame: () -> Unit,
    onQuickVsAi: () -> Unit,
    onCustomSetup: () -> Unit,
    onOpenRules: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleFastMode: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Brand Header & Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // 4-Colored Emblem Box
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .size(100.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(LudoColor.GREEN.primaryColor))
                                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(LudoColor.RED.primaryColor))
                            }
                            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(LudoColor.YELLOW.primaryColor))
                                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(LudoColor.BLUE.primaryColor))
                            }
                        }
                        // Center White Dice Pip
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .align(Alignment.Center)
                                .shadow(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎲",
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "LUDO CLASSIC",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Classic Board Strategy • Play Local & vs AI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Middle Section: Game Modes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Resume Game Button (if saved)
                if (hasSavedGame) {
                    MenuActionButton(
                        title = "Resume Match",
                        subtitle = "Continue your saved board session",
                        icon = Icons.Default.PlayArrow,
                        accentColor = Color(0xFF10B981),
                        testTag = "resume_game_button",
                        onClick = onResumeGame
                    )
                }

                // Quick vs Bot
                MenuActionButton(
                    title = "Quick vs Bot",
                    subtitle = "Instant 1 vs 1 against smart AI",
                    icon = Icons.Default.SmartToy,
                    accentColor = Color(0xFFE11D48),
                    testTag = "quick_vs_ai_button",
                    onClick = onQuickVsAi
                )

                // Pass & Play / Custom Game
                MenuActionButton(
                    title = "Play Game",
                    subtitle = "2, 3, or 4 Players • Human & AI bots",
                    icon = Icons.Default.Groups,
                    accentColor = Color(0xFF2563EB),
                    testTag = "custom_setup_button",
                    onClick = onCustomSetup
                )

                // Rules & Strategy Guide
                MenuActionButton(
                    title = "Rules & How to Play",
                    subtitle = "Learn safe squares, dice rolls & captures",
                    icon = Icons.Default.MenuBook,
                    accentColor = Color(0xFFF59E0B),
                    testTag = "menu_rules_button",
                    onClick = onOpenRules
                )
            }

            // Bottom Section: Toggles (Sound & Speed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound Toggle
                FilledTonalButton(
                    onClick = onToggleSound,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("toggle_sound_button")
                ) {
                    Icon(
                        imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Sound Toggle"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSoundMuted) "Muted" else "Sound On")
                }

                // Speed Toggle
                FilledTonalButton(
                    onClick = onToggleFastMode,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("toggle_speed_button")
                ) {
                    Icon(
                        imageVector = if (isFastMode) Icons.Default.Speed else Icons.Default.SlowMotionVideo,
                        contentDescription = "Speed Toggle"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFastMode) "Fast Anim" else "Normal")
                }
            }
        }
    }
}

@Composable
private fun MenuActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .testTag(testTag),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
