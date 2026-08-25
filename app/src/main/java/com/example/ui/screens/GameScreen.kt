package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GamePhase
import com.example.model.PlayerType
import com.example.ui.board.LudoBoard
import com.example.ui.components.DiceView
import com.example.ui.components.PlayerCard
import com.example.ui.dialogs.GameRulesDialog
import com.example.ui.dialogs.TurnHistoryDialog
import com.example.ui.dialogs.WinnerDialog
import com.example.viewmodel.LudoUiState
import com.example.viewmodel.LudoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: LudoViewModel,
    uiState: LudoUiState
) {
    var showRulesDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showRestartConfirm by remember { mutableStateOf(false) }

    val currentPlayer = uiState.players.getOrNull(uiState.currentTurnIndex)
    val isHumanTurn = currentPlayer?.type == PlayerType.HUMAN

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentPlayer != null) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(currentPlayer.color.primaryColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (currentPlayer != null) "${currentPlayer.name}'s Turn" else "Ludo Classic",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.backToMenu() },
                        modifier = Modifier.testTag("game_menu_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
                    }
                },
                actions = {
                    // Turn History Button
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }

                    // Sound Toggle
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier.testTag("game_sound_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Sound"
                        )
                    }

                    // Speed Toggle
                    IconButton(
                        onClick = { viewModel.toggleFastMode() },
                        modifier = Modifier.testTag("game_speed_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isFastMode) Icons.Default.Speed else Icons.Default.SlowMotionVideo,
                            contentDescription = "Speed",
                            tint = if (uiState.isFastMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Restart Button
                    IconButton(
                        onClick = { showRestartConfirm = true },
                        modifier = Modifier.testTag("restart_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart")
                    }

                    // Rules Button
                    IconButton(
                        onClick = { showRulesDialog = true },
                        modifier = Modifier.testTag("game_rules_icon")
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Rules")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideLayout = maxWidth > 680.dp

            if (isWideLayout) {
                // Tablet / Landscape Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Board
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        LudoBoard(
                            players = uiState.players,
                            currentPlayerIndex = uiState.currentTurnIndex,
                            legalTokens = uiState.legalTokens,
                            animatingToken = uiState.animatingToken,
                            onTokenClicked = { viewModel.onTokenSelected(it) },
                            modifier = Modifier
                                .fillMaxHeight(0.95f)
                                .aspectRatio(1f)
                        )
                    }

                    // Right Column: Controls & Player Cards
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Event Banner
                        EventBanner(bannerEvent = uiState.bannerEvent)

                        // Status message
                        StatusCard(statusMessage = uiState.statusMessage)

                        // Dice Station
                        DiceStation(
                            uiState = uiState,
                            currentPlayer = currentPlayer,
                            isHumanTurn = isHumanTurn,
                            onRoll = { viewModel.rollDice() }
                        )

                        // Players List
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.players.forEachIndexed { idx, p ->
                                PlayerCard(
                                    player = p,
                                    isCurrentTurn = idx == uiState.currentTurnIndex,
                                    isRolling = uiState.isRolling,
                                    isDiceRolled = uiState.isDiceRolled,
                                    diceValue = uiState.diceValue,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            } else {
                // Portrait Phone Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Top Section: Event Banner & Status Message
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EventBanner(bannerEvent = uiState.bannerEvent)
                        StatusCard(statusMessage = uiState.statusMessage)
                    }

                    // 2. Center Section: Ludo Board
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LudoBoard(
                            players = uiState.players,
                            currentPlayerIndex = uiState.currentTurnIndex,
                            legalTokens = uiState.legalTokens,
                            animatingToken = uiState.animatingToken,
                            onTokenClicked = { viewModel.onTokenSelected(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }

                    // 3. Bottom Section: Player Cards Grid & Dice Rolling Station
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dice & Action Row
                        DiceStation(
                            uiState = uiState,
                            currentPlayer = currentPlayer,
                            isHumanTurn = isHumanTurn,
                            onRoll = { viewModel.rollDice() }
                        )

                        // Player Status Grid (2 columns)
                        val chunked = uiState.players.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            chunked.forEach { rowPlayers ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowPlayers.forEach { p ->
                                        val idx = uiState.players.indexOf(p)
                                        PlayerCard(
                                            player = p,
                                            isCurrentTurn = idx == uiState.currentTurnIndex,
                                            isRolling = uiState.isRolling,
                                            isDiceRolled = uiState.isDiceRolled,
                                            diceValue = uiState.diceValue,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowPlayers.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Winner Dialog Overlay
    if (uiState.gamePhase == GamePhase.GAME_OVER) {
        WinnerDialog(
            winners = uiState.winnersList,
            allPlayers = uiState.players,
            onPlayAgain = { viewModel.restartCurrentGame() },
            onMainMenu = { viewModel.backToMenu() }
        )
    }

    // Rules Dialog
    if (showRulesDialog) {
        GameRulesDialog(onDismiss = { showRulesDialog = false })
    }

    // Turn History Dialog
    if (showHistoryDialog) {
        TurnHistoryDialog(
            turnHistory = uiState.turnHistory,
            onDismiss = { showHistoryDialog = false }
        )
    }

    // Restart Confirmation Dialog
    if (showRestartConfirm) {
        AlertDialog(
            onDismissRequest = { showRestartConfirm = false },
            title = { Text("Restart Match?") },
            text = { Text("Are you sure you want to restart the current game? Current board positions will be reset.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartConfirm = false
                        viewModel.restartCurrentGame()
                    }
                ) {
                    Text("Restart", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EventBanner(bannerEvent: String?) {
    AnimatedVisibility(
        visible = bannerEvent != null,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        if (bannerEvent != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFBBF24),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .testTag("event_banner")
            ) {
                Text(
                    text = bannerEvent,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF78350F),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusCard(statusMessage: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DiceStation(
    uiState: LudoUiState,
    currentPlayer: com.example.model.Player?,
    isHumanTurn: Boolean,
    onRoll: () -> Unit
) {
    if (currentPlayer == null) return

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                currentPlayer.color.primaryColor.copy(alpha = 0.6f),
                RoundedCornerShape(18.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Interactive 3D Dice
            DiceView(
                diceValue = uiState.diceValue,
                isRolling = uiState.isRolling,
                isDiceRolled = uiState.isDiceRolled,
                playerColor = currentPlayer.color,
                isHumanTurn = isHumanTurn,
                onRollClick = onRoll
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Action / Guidance Area
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                if (isHumanTurn) {
                    if (!uiState.isDiceRolled && !uiState.isRolling) {
                        Button(
                            onClick = onRoll,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = currentPlayer.color.primaryColor),
                            modifier = Modifier.testTag("action_roll_button")
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TAP TO ROLL", fontWeight = FontWeight.Bold)
                        }
                    } else if (uiState.isRolling) {
                        Text(
                            text = "Rolling...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = currentPlayer.color.primaryColor
                        )
                    } else {
                        Text(
                            text = if (uiState.legalTokens.isNotEmpty()) "SELECT A TOKEN" else "No moves",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (uiState.legalTokens.isNotEmpty()) currentPlayer.color.primaryColor else Color.Gray
                        )
                        Text(
                            text = "Rolled a ${uiState.diceValue}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Bot Turn indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = currentPlayer.color.primaryColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isRolling) "Bot Rolling..." else "Bot Thinking...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = currentPlayer.color.darkColor
                        )
                    }
                }
            }
        }
    }
}
