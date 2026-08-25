package com.example.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*

data class PlayerSetupItem(
    val id: Int,
    var name: String,
    val color: LudoColor,
    var type: PlayerType,
    var difficulty: AiDifficulty
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupScreen(
    onStartGame: (List<Player>) -> Unit,
    onBack: () -> Unit
) {
    var playerCount by remember { mutableIntStateOf(4) }

    // Pre-populated setup slots for 4 colors: Red, Green, Yellow, Blue
    var slot0Name by remember { mutableStateOf("Player 1") }
    var slot0Type by remember { mutableStateOf(PlayerType.HUMAN) }
    var slot0Diff by remember { mutableStateOf(AiDifficulty.MEDIUM) }

    var slot1Name by remember { mutableStateOf("Bot Green") }
    var slot1Type by remember { mutableStateOf(PlayerType.AI) }
    var slot1Diff by remember { mutableStateOf(AiDifficulty.MEDIUM) }

    var slot2Name by remember { mutableStateOf("Bot Yellow") }
    var slot2Type by remember { mutableStateOf(PlayerType.AI) }
    var slot2Diff by remember { mutableStateOf(AiDifficulty.HARD) }

    var slot3Name by remember { mutableStateOf("Bot Blue") }
    var slot3Type by remember { mutableStateOf(PlayerType.AI) }
    var slot3Diff by remember { mutableStateOf(AiDifficulty.EASY) }

    val activeColors = when (playerCount) {
        2 -> listOf(LudoColor.RED, LudoColor.YELLOW)
        3 -> listOf(LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW)
        else -> listOf(LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW, LudoColor.BLUE)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Setup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("setup_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            val players = mutableListOf<Player>()
                            activeColors.forEachIndexed { index, color ->
                                val (name, type, diff) = when (color) {
                                    LudoColor.RED -> Triple(slot0Name, slot0Type, slot0Diff)
                                    LudoColor.GREEN -> Triple(slot1Name, slot1Type, slot1Diff)
                                    LudoColor.YELLOW -> Triple(slot2Name, slot2Type, slot2Diff)
                                    LudoColor.BLUE -> Triple(slot3Name, slot3Type, slot3Diff)
                                }
                                players.add(
                                    Player(
                                        id = index,
                                        name = if (name.isBlank()) color.title else name.trim(),
                                        color = color,
                                        type = type,
                                        difficulty = diff
                                    )
                                )
                            }
                            onStartGame(players)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_game_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Match ($playerCount Players)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Player Count Selector
            Text(
                text = "Number of Players",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(2, 3, 4).forEach { count ->
                    val isSelected = playerCount == count
                    FilterChip(
                        selected = isSelected,
                        onClick = { playerCount = count },
                        label = {
                            Text(
                                text = "$count Players",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("player_count_$count"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Player Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Slot Configuration Cards
            activeColors.forEachIndexed { index, color ->
                when (color) {
                    LudoColor.RED -> PlayerConfigCard(
                        color = color,
                        slotNumber = index + 1,
                        name = slot0Name,
                        type = slot0Type,
                        difficulty = slot0Diff,
                        onNameChange = { slot0Name = it },
                        onTypeChange = { slot0Type = it },
                        onDifficultyChange = { slot0Diff = it }
                    )
                    LudoColor.GREEN -> PlayerConfigCard(
                        color = color,
                        slotNumber = index + 1,
                        name = slot1Name,
                        type = slot1Type,
                        difficulty = slot1Diff,
                        onNameChange = { slot1Name = it },
                        onTypeChange = { slot1Type = it },
                        onDifficultyChange = { slot1Diff = it }
                    )
                    LudoColor.YELLOW -> PlayerConfigCard(
                        color = color,
                        slotNumber = index + 1,
                        name = slot2Name,
                        type = slot2Type,
                        difficulty = slot2Diff,
                        onNameChange = { slot2Name = it },
                        onTypeChange = { slot2Type = it },
                        onDifficultyChange = { slot2Diff = it }
                    )
                    LudoColor.BLUE -> PlayerConfigCard(
                        color = color,
                        slotNumber = index + 1,
                        name = slot3Name,
                        type = slot3Type,
                        difficulty = slot3Diff,
                        onNameChange = { slot3Name = it },
                        onTypeChange = { slot3Type = it },
                        onDifficultyChange = { slot3Diff = it }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PlayerConfigCard(
    color: LudoColor,
    slotNumber: Int,
    name: String,
    type: PlayerType,
    difficulty: AiDifficulty,
    onNameChange: (String) -> Unit,
    onTypeChange: (PlayerType) -> Unit,
    onDifficultyChange: (AiDifficulty) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, color.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Color Circle + Name Field
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$slotNumber",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("${color.title} Player Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player Type Selector: Human vs AI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Human Button
                FilterChip(
                    selected = type == PlayerType.HUMAN,
                    onClick = { onTypeChange(PlayerType.HUMAN) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Human") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                // AI Button
                FilterChip(
                    selected = type == PlayerType.AI,
                    onClick = { onTypeChange(PlayerType.AI) },
                    leadingIcon = { Icon(Icons.Default.SmartToy, contentDescription = null) },
                    label = { Text("AI Bot") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // If AI, show Difficulty Segmented Options
            if (type == PlayerType.AI) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AiDifficulty.values().forEach { d ->
                        val isSel = difficulty == d
                        FilterChip(
                            selected = isSel,
                            onClick = { onDifficultyChange(d) },
                            label = { Text(d.label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}
