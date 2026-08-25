package com.example.engine

import com.example.model.*

sealed class MoveResult {
    data class Success(
        val updatedPlayers: List<Player>,
        val movedToken: Token,
        val capturedTokens: List<Token>,
        val isHomeEntry: Boolean,
        val grantsExtraTurn: Boolean,
        val message: String
    ) : MoveResult()

    data class Invalid(val reason: String) : MoveResult()
}

object LudoEngine {

    /**
     * Checks if a token can make a legal move given the dice roll.
     */
    fun canTokenMove(token: Token, diceRoll: Int): Boolean {
        if (token.isHome) return false
        if (token.isInBase) {
            return diceRoll == 6
        }
        val targetStep = token.stepCount + diceRoll
        return targetStep <= 56
    }

    /**
     * Returns the list of token IDs for the given player that can legally move.
     */
    fun getLegalMoves(player: Player, diceRoll: Int): List<Int> {
        if (player.isWinner) return emptyList()
        return player.tokens
            .filter { canTokenMove(it, diceRoll) }
            .map { it.id }
    }

    /**
     * Executes the move for a token and returns the result including captured tokens and extra turn determination.
     */
    fun executeMove(
        players: List<Player>,
        playerIndex: Int,
        tokenId: Int,
        diceRoll: Int
    ): MoveResult {
        val player = players.getOrNull(playerIndex)
            ?: return MoveResult.Invalid("Player index out of bounds")
        val token = player.tokens.getOrNull(tokenId)
            ?: return MoveResult.Invalid("Token ID out of bounds")

        if (!canTokenMove(token, diceRoll)) {
            return MoveResult.Invalid("Illegal move for token ${tokenId + 1}")
        }

        val nextStep = if (token.isInBase) 0 else token.stepCount + diceRoll
        val updatedToken = token.copy(stepCount = nextStep)

        var capturedTokens = mutableListOf<Token>()
        var isCapture = false
        var isHomeEntry = updatedToken.isHome

        // Check for captures on shared track (steps 0..50)
        if (updatedToken.isOnTrack) {
            val targetTrackIdx = updatedToken.trackTileIndex
            val isSafeSquare = BoardConstants.SAFE_TRACK_INDICES.contains(targetTrackIdx)

            if (!isSafeSquare) {
                // Look for opponents on the same track index
                players.forEachIndexed { pIdx, oppPlayer ->
                    if (pIdx != playerIndex && !oppPlayer.isWinner) {
                        oppPlayer.tokens.forEach { oppToken ->
                            if (oppToken.isOnTrack && oppToken.trackTileIndex == targetTrackIdx) {
                                capturedTokens.add(oppToken)
                            }
                        }
                    }
                }
                if (capturedTokens.isNotEmpty()) {
                    isCapture = true
                }
            }
        }

        val capturedSet = capturedTokens.map { it.color to it.id }.toSet()

        // Build updated players list
        val updatedPlayers = players.mapIndexed { pIdx, p ->
            if (pIdx == playerIndex) {
                val newTokens = p.tokens.map { if (it.id == tokenId) updatedToken else it }
                p.copy(
                    tokens = newTokens,
                    capturesMade = p.capturesMade + capturedTokens.size,
                    totalMoves = p.totalMoves + 1,
                    sixesRolled = if (diceRoll == 6) p.sixesRolled + 1 else p.sixesRolled
                )
            } else {
                // Reset any captured tokens to base
                val newTokens = p.tokens.map {
                    if (capturedSet.contains(it.color to it.id)) {
                        it.copy(stepCount = -1)
                    } else {
                        it
                    }
                }
                p.copy(tokens = newTokens)
            }
        }

        val grantsExtraTurn = (diceRoll == 6) || isCapture || isHomeEntry

        val message = when {
            token.isInBase -> "${player.name} brought a token onto the board!"
            isHomeEntry -> "${player.name}'s token reached Home! 🎉"
            isCapture -> "${player.name} captured ${capturedTokens.first().color.title}!"
            diceRoll == 6 -> "${player.name} rolled a 6 and gets another turn!"
            else -> "${player.name} moved token ${tokenId + 1} by $diceRoll steps."
        }

        return MoveResult.Success(
            updatedPlayers = updatedPlayers,
            movedToken = updatedToken,
            capturedTokens = capturedTokens,
            isHomeEntry = isHomeEntry,
            grantsExtraTurn = grantsExtraTurn,
            message = message
        )
    }

    /**
     * Gets the index of the next active player who hasn't already won all their tokens.
     */
    fun getNextPlayerIndex(players: List<Player>, currentIndex: Int): Int {
        val total = players.size
        for (i in 1..total) {
            val nextIdx = (currentIndex + i) % total
            if (!players[nextIdx].isWinner) {
                return nextIdx
            }
        }
        return currentIndex
    }
}
