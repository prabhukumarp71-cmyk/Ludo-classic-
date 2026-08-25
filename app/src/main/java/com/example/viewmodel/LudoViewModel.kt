package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.engine.LudoAi
import com.example.engine.LudoEngine
import com.example.engine.MoveResult
import com.example.model.*
import com.example.persistence.GameStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class AnimatingTokenState(
    val playerColor: LudoColor,
    val tokenId: Int,
    val currentStep: Int,
    val targetStep: Int,
    val intermediatePositions: List<GridPos> = emptyList(),
    val currentAnimIndex: Int = 0
)

data class LudoUiState(
    val gamePhase: GamePhase = GamePhase.MENU,
    val players: List<Player> = emptyList(),
    val currentTurnIndex: Int = 0,
    val diceValue: Int = 6,
    val isRolling: Boolean = false,
    val isDiceRolled: Boolean = false,
    val consecutiveSixes: Int = 0,
    val legalTokens: List<Int> = emptyList(),
    val statusMessage: String = "Welcome to Ludo Classic!",
    val turnHistory: List<TurnRecord> = emptyList(),
    val winnersList: List<Player> = emptyList(),
    val isSoundMuted: Boolean = false,
    val isFastMode: Boolean = false,
    val hasSavedGame: Boolean = false,
    val animatingToken: AnimatingTokenState? = null,
    val bannerEvent: String? = null
)

class LudoViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = GameStorage(application)
    private val _uiState = MutableStateFlow(LudoUiState())
    val uiState: StateFlow<LudoUiState> = _uiState.asStateFlow()

    private var aiJob: Job? = null
    private var animationJob: Job? = null

    init {
        val muted = storage.isSoundMuted()
        val fast = storage.isFastMode()
        val hasSaved = storage.hasSavedGame()
        SoundManager.isMuted = muted

        _uiState.value = _uiState.value.copy(
            isSoundMuted = muted,
            isFastMode = fast,
            hasSavedGame = hasSaved
        )
    }

    fun toggleSound() {
        val newMuted = !_uiState.value.isSoundMuted
        SoundManager.isMuted = newMuted
        storage.saveSoundMuted(newMuted)
        _uiState.value = _uiState.value.copy(isSoundMuted = newMuted)
    }

    fun toggleFastMode() {
        val newFast = !_uiState.value.isFastMode
        storage.saveFastMode(newFast)
        _uiState.value = _uiState.value.copy(isFastMode = newFast)
    }

    fun openSetup() {
        SoundManager.playButton()
        _uiState.value = _uiState.value.copy(gamePhase = GamePhase.SETUP)
    }

    fun backToMenu() {
        SoundManager.playButton()
        aiJob?.cancel()
        animationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            gamePhase = GamePhase.MENU,
            hasSavedGame = storage.hasSavedGame()
        )
    }

    fun pauseGame() {
        SoundManager.playButton()
        aiJob?.cancel()
        _uiState.value = _uiState.value.copy(gamePhase = GamePhase.PAUSED)
    }

    fun resumeGame() {
        SoundManager.playButton()
        _uiState.value = _uiState.value.copy(gamePhase = GamePhase.PLAYING)
        checkCurrentTurn()
    }

    fun startQuickVsAiGame() {
        SoundManager.playButton()
        val players = listOf(
            Player(id = 0, name = "You", color = LudoColor.RED, type = PlayerType.HUMAN),
            Player(id = 1, name = "Bot Yellow", color = LudoColor.YELLOW, type = PlayerType.AI, difficulty = AiDifficulty.MEDIUM)
        )
        initGameWithPlayers(players)
    }

    fun startCustomGame(players: List<Player>) {
        SoundManager.playButton()
        initGameWithPlayers(players)
    }

    fun resumeSavedGame() {
        SoundManager.playButton()
        val saved = storage.loadSavedGame() ?: return
        _uiState.value = _uiState.value.copy(
            gamePhase = GamePhase.PLAYING,
            players = saved.players,
            currentTurnIndex = saved.currentTurnIndex,
            diceValue = saved.diceValue,
            isDiceRolled = saved.isDiceRolled,
            isRolling = false,
            legalTokens = if (saved.isDiceRolled) {
                LudoEngine.getLegalMoves(saved.players[saved.currentTurnIndex], saved.diceValue)
            } else {
                emptyList()
            },
            statusMessage = "Game Resumed! ${saved.players[saved.currentTurnIndex].name}'s Turn",
            animatingToken = null
        )
        checkCurrentTurn()
    }

    private fun initGameWithPlayers(players: List<Player>) {
        aiJob?.cancel()
        animationJob?.cancel()
        storage.clearSavedGame()

        val initialPlayer = players.first()
        _uiState.value = _uiState.value.copy(
            gamePhase = GamePhase.PLAYING,
            players = players,
            currentTurnIndex = 0,
            diceValue = 6,
            isRolling = false,
            isDiceRolled = false,
            consecutiveSixes = 0,
            legalTokens = emptyList(),
            statusMessage = "${initialPlayer.name}'s turn to roll!",
            turnHistory = listOf(
                TurnRecord(
                    playerColor = initialPlayer.color,
                    playerName = initialPlayer.name,
                    diceRoll = 0,
                    message = "Game Started! May the best player win!"
                )
            ),
            winnersList = emptyList(),
            animatingToken = null,
            bannerEvent = null
        )

        saveCurrentState()
        checkCurrentTurn()
    }

    fun rollDice() {
        val state = _uiState.value
        if (state.gamePhase != GamePhase.PLAYING) return
        if (state.isRolling || state.isDiceRolled || state.animatingToken != null) return

        val currentPlayer = state.players[state.currentTurnIndex]
        performDiceRoll(currentPlayer)
    }

    private fun performDiceRoll(player: Player) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRolling = true,
                statusMessage = "${player.name} is rolling the dice..."
            )
            SoundManager.playDiceRoll()

            val fast = _uiState.value.isFastMode
            val rollIterations = if (fast) 4 else 8
            val delayMs = if (fast) 45L else 70L

            for (i in 0 until rollIterations) {
                _uiState.value = _uiState.value.copy(diceValue = Random.nextInt(1, 7))
                delay(delayMs)
            }

            val finalRoll = Random.nextInt(1, 7)
            if (finalRoll == 6) {
                SoundManager.playSixRolled()
            }

            val newConsecutive = if (finalRoll == 6) _uiState.value.consecutiveSixes + 1 else 0

            // 3-Consecutive-Sixes penalty rule
            if (newConsecutive >= 3) {
                _uiState.value = _uiState.value.copy(
                    diceValue = finalRoll,
                    isRolling = false,
                    isDiceRolled = true,
                    consecutiveSixes = 0,
                    legalTokens = emptyList(),
                    statusMessage = "${player.name} rolled three 6s in a row! Turn forfeited!",
                    bannerEvent = "Three 6s! Turn Lost"
                )
                delay(1200)
                passTurnToNextPlayer()
                return@launch
            }

            val legalMoves = LudoEngine.getLegalMoves(player, finalRoll)

            _uiState.value = _uiState.value.copy(
                diceValue = finalRoll,
                isRolling = false,
                isDiceRolled = true,
                consecutiveSixes = newConsecutive,
                legalTokens = legalMoves,
                statusMessage = if (legalMoves.isNotEmpty()) {
                    "${player.name} rolled a $finalRoll! Select a token to move."
                } else {
                    "${player.name} rolled a $finalRoll. No legal moves!"
                },
                bannerEvent = if (finalRoll == 6) "Rolled 6! Extra Turn!" else null
            )

            // Auto-pass if no legal moves
            if (legalMoves.isEmpty()) {
                val delayTime = if (fast) 600L else 1100L
                delay(delayTime)
                passTurnToNextPlayer()
            } else {
                // If single legal move and human or AI, could auto-select or let user tap
                if (player.type == PlayerType.AI) {
                    val thinkDelay = if (fast) 300L else 600L
                    delay(thinkDelay)
                    val selectedTokenId = LudoAi.selectTokenMove(player, _uiState.value.players, finalRoll, player.difficulty)
                    if (selectedTokenId != null) {
                        onTokenSelected(selectedTokenId)
                    }
                }
            }
        }
    }

    fun onTokenSelected(tokenId: Int) {
        val state = _uiState.value
        if (state.gamePhase != GamePhase.PLAYING) return
        if (!state.isDiceRolled || state.isRolling || state.animatingToken != null) return

        val currentPlayer = state.players[state.currentTurnIndex]
        if (!state.legalTokens.contains(tokenId)) return

        SoundManager.playButton()
        executePlayerMove(currentPlayer, tokenId, state.diceValue)
    }

    private fun executePlayerMove(player: Player, tokenId: Int, diceRoll: Int) {
        val state = _uiState.value
        val token = player.tokens[tokenId]

        // Calculate step-by-step positions for animation
        val fromStep = token.stepCount
        val targetStep = if (token.isInBase) 0 else fromStep + diceRoll

        val intermediatePos = mutableListOf<GridPos>()
        if (fromStep == -1) {
            intermediatePos.add(BoardConstants.getBaseSlotPos(player.color, tokenId))
            intermediatePos.add(BoardConstants.getTokenGridPos(token.copy(stepCount = 0)))
        } else {
            for (s in fromStep..targetStep) {
                intermediatePos.add(BoardConstants.getTokenGridPos(token.copy(stepCount = s)))
            }
        }

        animationJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                animatingToken = AnimatingTokenState(
                    playerColor = player.color,
                    tokenId = tokenId,
                    currentStep = fromStep,
                    targetStep = targetStep,
                    intermediatePositions = intermediatePos,
                    currentAnimIndex = 0
                ),
                legalTokens = emptyList()
            )

            val stepDelay = if (_uiState.value.isFastMode) 60L else 110L
            for (idx in intermediatePos.indices) {
                _uiState.value = _uiState.value.copy(
                    animatingToken = _uiState.value.animatingToken?.copy(currentAnimIndex = idx)
                )
                if (idx > 0) {
                    SoundManager.playStep()
                }
                delay(stepDelay)
            }

            // Execute engine logic
            val result = LudoEngine.executeMove(_uiState.value.players, _uiState.value.currentTurnIndex, tokenId, diceRoll)

            if (result is MoveResult.Success) {
                if (result.capturedTokens.isNotEmpty()) {
                    SoundManager.playCapture()
                } else if (result.isHomeEntry) {
                    SoundManager.playHomeEntry()
                }

                val newTurnRecord = TurnRecord(
                    playerColor = player.color,
                    playerName = player.name,
                    diceRoll = diceRoll,
                    message = result.message,
                    isCapture = result.capturedTokens.isNotEmpty(),
                    isHomeEntry = result.isHomeEntry
                )

                // Check winner status
                var updatedPlayers = result.updatedPlayers
                var newWinners = _uiState.value.winnersList.toMutableList()
                val movedPlayer = updatedPlayers[_uiState.value.currentTurnIndex]

                if (movedPlayer.isWinner && !newWinners.any { it.id == movedPlayer.id }) {
                    val rank = newWinners.size + 1
                    updatedPlayers = updatedPlayers.map {
                        if (it.id == movedPlayer.id) it.copy(rank = rank) else it
                    }
                    newWinners.add(movedPlayer.copy(rank = rank))
                    SoundManager.playWin()
                }

                // Check if game is completely finished
                val nonWinnersCount = updatedPlayers.count { !it.isWinner }
                val isGameOver = (updatedPlayers.size > 1 && nonWinnersCount <= 1) || (updatedPlayers.size == 1 && movedPlayer.isWinner)

                _uiState.value = _uiState.value.copy(
                    players = updatedPlayers,
                    animatingToken = null,
                    isDiceRolled = false,
                    statusMessage = result.message,
                    turnHistory = listOf(newTurnRecord) + _uiState.value.turnHistory.take(25),
                    winnersList = newWinners,
                    bannerEvent = when {
                        result.capturedTokens.isNotEmpty() -> "Token Captured! ⚔️"
                        result.isHomeEntry -> "Token Reached Home! 🏠"
                        result.grantsExtraTurn -> "Bonus Turn!"
                        else -> null
                    }
                )

                saveCurrentState()

                if (isGameOver) {
                    _uiState.value = _uiState.value.copy(
                        gamePhase = GamePhase.GAME_OVER,
                        statusMessage = "${newWinners.firstOrNull()?.name ?: "Player"} won the game!"
                    )
                    storage.clearSavedGame()
                    return@launch
                }

                delay(if (_uiState.value.isFastMode) 250L else 450L)

                if (result.grantsExtraTurn && !movedPlayer.isWinner) {
                    _uiState.value = _uiState.value.copy(
                        statusMessage = "${player.name} earned an extra roll!"
                    )
                    checkCurrentTurn()
                } else {
                    passTurnToNextPlayer()
                }
            } else {
                _uiState.value = _uiState.value.copy(animatingToken = null, isDiceRolled = false)
                passTurnToNextPlayer()
            }
        }
    }

    private fun passTurnToNextPlayer() {
        val state = _uiState.value
        val nextIndex = LudoEngine.getNextPlayerIndex(state.players, state.currentTurnIndex)
        val nextPlayer = state.players[nextIndex]

        _uiState.value = _uiState.value.copy(
            currentTurnIndex = nextIndex,
            isDiceRolled = false,
            isRolling = false,
            consecutiveSixes = 0,
            legalTokens = emptyList(),
            statusMessage = "${nextPlayer.name}'s turn to roll!",
            bannerEvent = null
        )

        saveCurrentState()
        checkCurrentTurn()
    }

    private fun checkCurrentTurn() {
        val state = _uiState.value
        if (state.gamePhase != GamePhase.PLAYING) return

        val currentPlayer = state.players.getOrNull(state.currentTurnIndex) ?: return
        if (currentPlayer.isWinner) {
            passTurnToNextPlayer()
            return
        }

        aiJob?.cancel()
        if (currentPlayer.type == PlayerType.AI && !state.isRolling && !state.isDiceRolled && state.animatingToken == null) {
            aiJob = viewModelScope.launch {
                val delayTime = if (state.isFastMode) 350L else 750L
                delay(delayTime)
                if (_uiState.value.gamePhase == GamePhase.PLAYING) {
                    performDiceRoll(currentPlayer)
                }
            }
        }
    }

    private fun saveCurrentState() {
        val state = _uiState.value
        if (state.gamePhase == GamePhase.PLAYING && state.players.isNotEmpty()) {
            storage.saveActiveGame(
                players = state.players,
                currentTurnIndex = state.currentTurnIndex,
                diceValue = state.diceValue,
                isDiceRolled = state.isDiceRolled
            )
        }
    }

    fun restartCurrentGame() {
        SoundManager.playButton()
        val currentPlayers = _uiState.value.players.map {
            it.copy(
                tokens = listOf(
                    Token(0, it.color),
                    Token(1, it.color),
                    Token(2, it.color),
                    Token(3, it.color)
                ),
                rank = 0,
                capturesMade = 0,
                totalMoves = 0,
                sixesRolled = 0
            )
        }
        initGameWithPlayers(currentPlayers)
    }

    fun dismissBanner() {
        _uiState.value = _uiState.value.copy(bannerEvent = null)
    }
}
