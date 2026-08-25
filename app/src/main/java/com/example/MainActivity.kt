package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.model.GamePhase
import com.example.ui.dialogs.GameRulesDialog
import com.example.ui.screens.GameScreen
import com.example.ui.screens.GameSetupScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LudoViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val viewModel: LudoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    var showRulesFromMenu by remember { mutableStateOf(false) }

                    AnimatedContent(
                        targetState = uiState.gamePhase,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "gamePhaseTransition"
                    ) { phase ->
                        when (phase) {
                            GamePhase.MENU -> {
                                MenuScreen(
                                    hasSavedGame = uiState.hasSavedGame,
                                    isSoundMuted = uiState.isSoundMuted,
                                    isFastMode = uiState.isFastMode,
                                    onResumeGame = { viewModel.resumeSavedGame() },
                                    onQuickVsAi = { viewModel.startQuickVsAiGame() },
                                    onCustomSetup = { viewModel.openSetup() },
                                    onOpenRules = { showRulesFromMenu = true },
                                    onToggleSound = { viewModel.toggleSound() },
                                    onToggleFastMode = { viewModel.toggleFastMode() }
                                )
                            }
                            GamePhase.SETUP -> {
                                GameSetupScreen(
                                    onStartGame = { players -> viewModel.startCustomGame(players) },
                                    onBack = { viewModel.backToMenu() }
                                )
                            }
                            GamePhase.PLAYING, GamePhase.PAUSED, GamePhase.GAME_OVER -> {
                                GameScreen(
                                    viewModel = viewModel,
                                    uiState = uiState
                                )
                            }
                        }
                    }

                    if (showRulesFromMenu) {
                        GameRulesDialog(onDismiss = { showRulesFromMenu = false })
                    }
                }
            }
        }
    }
}
