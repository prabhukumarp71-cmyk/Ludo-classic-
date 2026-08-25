package com.example.engine

import com.example.model.*
import kotlin.random.Random

object LudoAi {

    /**
     * Decides which token to move based on the AI difficulty level.
     */
    fun selectTokenMove(
        player: Player,
        allPlayers: List<Player>,
        diceRoll: Int,
        difficulty: AiDifficulty
    ): Int? {
        val legalMoves = LudoEngine.getLegalMoves(player, diceRoll)
        if (legalMoves.isEmpty()) return null
        if (legalMoves.size == 1) return legalMoves.first()

        return when (difficulty) {
            AiDifficulty.EASY -> chooseEasyMove(legalMoves)
            AiDifficulty.MEDIUM -> chooseMediumMove(player, allPlayers, legalMoves, diceRoll)
            AiDifficulty.HARD -> chooseHardMove(player, allPlayers, legalMoves, diceRoll)
        }
    }

    private fun chooseEasyMove(legalMoves: List<Int>): Int {
        return legalMoves[Random.nextInt(legalMoves.size)]
    }

    private fun chooseMediumMove(
        player: Player,
        allPlayers: List<Player>,
        legalMoves: List<Int>,
        diceRoll: Int
    ): Int {
        val tokens = player.tokens

        // 1. Can we capture any opponent?
        for (tokenId in legalMoves) {
            val t = tokens[tokenId]
            val nextStep = if (t.isInBase) 0 else t.stepCount + diceRoll
            if (nextStep in 0..50) {
                val targetTrackIdx = (player.color.trackStartOffset + nextStep) % 52
                if (!BoardConstants.SAFE_TRACK_INDICES.contains(targetTrackIdx)) {
                    val oppFound = allPlayers.any { opp ->
                        opp.id != player.id && !opp.isWinner && opp.tokens.any {
                            it.isOnTrack && it.trackTileIndex == targetTrackIdx
                        }
                    }
                    if (oppFound) return tokenId
                }
            }
        }

        // 2. Can we enter Home (step 56)?
        for (tokenId in legalMoves) {
            val t = tokens[tokenId]
            if (!t.isInBase && t.stepCount + diceRoll == 56) {
                return tokenId
            }
        }

        // 3. If rolled 6, bring out a new token from base
        if (diceRoll == 6) {
            val baseTokenId = legalMoves.find { tokens[it].isInBase }
            if (baseTokenId != null) return baseTokenId
        }

        // 4. Otherwise move token furthest along the board (closest to home)
        return legalMoves.maxByOrNull { tokens[it].stepCount } ?: legalMoves.first()
    }

    private fun chooseHardMove(
        player: Player,
        allPlayers: List<Player>,
        legalMoves: List<Int>,
        diceRoll: Int
    ): Int {
        val opponentTokens = allPlayers
            .filter { it.id != player.id && !it.isWinner }
            .flatMap { it.tokens }
            .filter { it.isOnTrack }

        var bestScore = Double.NEGATIVE_INFINITY
        var bestTokenId = legalMoves.first()

        for (tokenId in legalMoves) {
            val token = player.tokens[tokenId]
            var score = 0.0

            val currentStep = token.stepCount
            val targetStep = if (token.isInBase) 0 else currentStep + diceRoll

            // 1. Entering Home
            if (targetStep == 56) {
                score += 1200.0
            }

            // 2. Entering safe Home Run path (steps 51..55)
            if (currentStep <= 50 && targetStep in 51..55) {
                score += 500.0
            }

            // 3. Capturing Opponent
            if (targetStep in 0..50) {
                val targetTrackIdx = (player.color.trackStartOffset + targetStep) % 52
                val isSafe = BoardConstants.SAFE_TRACK_INDICES.contains(targetTrackIdx)

                if (!isSafe) {
                    val capturedOpponents = opponentTokens.count { it.trackTileIndex == targetTrackIdx }
                    if (capturedOpponents > 0) {
                        score += 900.0 * capturedOpponents
                    }
                } else {
                    // Landing on a safe square / star
                    score += 350.0
                }

                // 4. Vulnerability check: Is target tile dangerous? (Are opponents 1..6 tiles behind target?)
                val threatenedAtTarget = opponentTokens.any { opp ->
                    val dist = (targetTrackIdx - opp.trackTileIndex + 52) % 52
                    dist in 1..6
                }
                if (threatenedAtTarget && !isSafe) {
                    score -= 320.0
                }
            }

            // 5. Escaping danger from current square
            if (token.isOnTrack) {
                val currentTrackIdx = token.trackTileIndex
                val isCurrentlySafe = BoardConstants.SAFE_TRACK_INDICES.contains(currentTrackIdx)
                if (!isCurrentlySafe) {
                    val isCurrentlyThreatened = opponentTokens.any { opp ->
                        val dist = (currentTrackIdx - opp.trackTileIndex + 52) % 52
                        dist in 1..6
                    }
                    if (isCurrentlyThreatened) {
                        score += 480.0 // Big priority to escape impending capture!
                    }
                }
            }

            // 6. Bringing token out of base on a 6
            if (token.isInBase && diceRoll == 6) {
                score += 550.0
            }

            // 7. General forward progress bonus
            score += targetStep * 4.0

            // Add tiny random tie-breaker to keep behavior organic
            score += Random.nextDouble(0.0, 5.0)

            if (score > bestScore) {
                bestScore = score
                bestTokenId = tokenId
            }
        }

        return bestTokenId
    }
}
