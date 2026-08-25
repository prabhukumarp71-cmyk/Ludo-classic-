package com.example.model

import androidx.compose.ui.graphics.Color

enum class LudoColor(
    val title: String,
    val primaryColor: Color,
    val darkColor: Color,
    val lightColor: Color,
    val trackStartOffset: Int, // offset in 52-tile board loop
    val quadrantCol: Int, // 0 for left, 1 for right
    val quadrantRow: Int // 0 for top, 1 for bottom
) {
    RED(
        title = "Red",
        primaryColor = Color(0xFFE11D48),
        darkColor = Color(0xFF9F1239),
        lightColor = Color(0xFFFFE4E6),
        trackStartOffset = 0,
        quadrantCol = 0,
        quadrantRow = 1
    ),
    BLUE(
        title = "Blue",
        primaryColor = Color(0xFF2563EB),
        darkColor = Color(0xFF1E40AF),
        lightColor = Color(0xFFDBEAFE),
        trackStartOffset = 13,
        quadrantCol = 1,
        quadrantRow = 1
    ),
    YELLOW(
        title = "Yellow",
        primaryColor = Color(0xFFF59E0B),
        darkColor = Color(0xFFB45309),
        lightColor = Color(0xFFFEF3C7),
        trackStartOffset = 26,
        quadrantCol = 1,
        quadrantRow = 0
    ),
    GREEN(
        title = "Green",
        primaryColor = Color(0xFF10B981),
        darkColor = Color(0xFF047857),
        lightColor = Color(0xFFD1FAE5),
        trackStartOffset = 39,
        quadrantCol = 0,
        quadrantRow = 0
    )
}

enum class PlayerType {
    HUMAN,
    AI
}

enum class AiDifficulty(val label: String, val description: String) {
    EASY("Easy", "Makes casual, mostly random moves"),
    MEDIUM("Medium", "Captures opponents and advances toward home"),
    HARD("Hard", "Tactical: calculates danger, escapes, and optimal paths")
}

data class Token(
    val id: Int, // 0 to 3
    val color: LudoColor,
    // stepCount: -1 means in base.
    // 0..50 means on the 52-tile track loop (where 0 is this color's starting tile).
    // 51..55 means in this color's 5-tile home run column.
    // 56 means safely in Center Home!
    val stepCount: Int = -1
) {
    val isInBase: Boolean get() = stepCount == -1
    val isOnTrack: Boolean get() = stepCount in 0..50
    val isInHomeRun: Boolean get() = stepCount in 51..55
    val isHome: Boolean get() = stepCount == 56

    /**
     * Absolute 0..51 index on the shared 52-tile perimeter loop, or -1 if not on shared track.
     */
    val trackTileIndex: Int
        get() = if (isOnTrack) {
            (color.trackStartOffset + stepCount) % 52
        } else {
            -1
        }
}

data class Player(
    val id: Int,
    val name: String,
    val color: LudoColor,
    val type: PlayerType = PlayerType.HUMAN,
    val difficulty: AiDifficulty = AiDifficulty.MEDIUM,
    val tokens: List<Token> = listOf(
        Token(0, color),
        Token(1, color),
        Token(2, color),
        Token(3, color)
    ),
    val rank: Int = 0, // 0 if not finished, 1 for 1st place, etc.
    val capturesMade: Int = 0,
    val totalMoves: Int = 0,
    val sixesRolled: Int = 0
) {
    val isWinner: Boolean get() = tokens.all { it.isHome }
    val tokensHomeCount: Int get() = tokens.count { it.isHome }
    val tokensInBaseCount: Int get() = tokens.count { it.isInBase }
    val tokensActiveCount: Int get() = tokens.count { it.isOnTrack || it.isInHomeRun }
}

enum class GamePhase {
    MENU,
    SETUP,
    PLAYING,
    PAUSED,
    GAME_OVER
}

data class TurnRecord(
    val id: Long = System.currentTimeMillis(),
    val playerColor: LudoColor,
    val playerName: String,
    val diceRoll: Int,
    val message: String,
    val isCapture: Boolean = false,
    val isHomeEntry: Boolean = false
)

data class GridPos(val x: Float, val y: Float)

object BoardConstants {
    // 52-tile main track coordinates on standard 15x15 Ludo board (x: 0..14, y: 0..14)
    // Index 0 is Red's start square at (1, 8)
    val TRACK_COORDINATES: List<Pair<Int, Int>> = listOf(
        Pair(1, 8),   // 0: Red Start (Safe)
        Pair(2, 8),   // 1
        Pair(3, 8),   // 2
        Pair(4, 8),   // 3
        Pair(5, 8),   // 4
        Pair(6, 9),   // 5
        Pair(6, 10),  // 6
        Pair(6, 11),  // 7
        Pair(6, 12),  // 8: Star (Safe)
        Pair(6, 13),  // 9
        Pair(6, 14),  // 10
        Pair(7, 14),  // 11
        Pair(8, 14),  // 12
        Pair(8, 13),  // 13: Blue Start (Safe)
        Pair(8, 12),  // 14
        Pair(8, 11),  // 15
        Pair(8, 10),  // 16
        Pair(8, 9),   // 17
        Pair(9, 8),   // 18
        Pair(10, 8),  // 19
        Pair(11, 8),  // 20
        Pair(12, 8),  // 21: Star (Safe)
        Pair(13, 8),  // 22
        Pair(14, 8),  // 23
        Pair(14, 7),  // 24
        Pair(14, 6),  // 25
        Pair(13, 6),  // 26: Yellow Start (Safe)
        Pair(12, 6),  // 27
        Pair(11, 6),  // 28
        Pair(10, 6),  // 29
        Pair(9, 6),   // 30
        Pair(8, 5),   // 31
        Pair(8, 4),   // 32
        Pair(8, 3),   // 33
        Pair(8, 2),   // 34: Star (Safe)
        Pair(8, 1),   // 35
        Pair(8, 0),   // 36
        Pair(7, 0),   // 37
        Pair(6, 0),   // 38
        Pair(6, 1),   // 39: Green Start (Safe)
        Pair(6, 2),   // 40
        Pair(6, 3),   // 41
        Pair(6, 4),   // 42
        Pair(6, 5),   // 43
        Pair(5, 6),   // 44
        Pair(4, 6),   // 45
        Pair(3, 6),   // 46
        Pair(2, 6),   // 47: Star (Safe)
        Pair(1, 6),   // 48
        Pair(0, 6),   // 49
        Pair(0, 7),   // 50
        Pair(0, 8)    // 51
    )

    // Star / Safe squares on the 52-tile track
    val SAFE_TRACK_INDICES = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // Base slot offsets for 4 tokens inside the 6x6 base yards
    fun getBaseSlotPos(color: LudoColor, tokenId: Int): GridPos {
        val (baseCol, baseRow) = when (color) {
            LudoColor.GREEN -> Pair(0f, 0f)
            LudoColor.YELLOW -> Pair(9f, 0f)
            LudoColor.RED -> Pair(0f, 9f)
            LudoColor.BLUE -> Pair(9f, 9f)
        }
        val (slotX, slotY) = when (tokenId) {
            0 -> Pair(1.6f, 1.6f)
            1 -> Pair(3.8f, 1.6f)
            2 -> Pair(1.6f, 3.8f)
            else -> Pair(3.8f, 3.8f)
        }
        return GridPos(baseCol + slotX, baseRow + slotY)
    }

    // Home Run path coordinates for steps 51..55 and 56 (Center)
    fun getHomeRunPos(color: LudoColor, step: Int): GridPos {
        if (step == 56) {
            return when (color) {
                LudoColor.RED -> GridPos(6.6f, 7.0f)
                LudoColor.BLUE -> GridPos(7.0f, 7.4f)
                LudoColor.YELLOW -> GridPos(7.4f, 7.0f)
                LudoColor.GREEN -> GridPos(7.0f, 6.6f)
            }
        }
        val subStep = step - 51 // 0..4
        return when (color) {
            LudoColor.RED -> GridPos((1 + subStep).toFloat(), 7f)
            LudoColor.BLUE -> GridPos(7f, (13 - subStep).toFloat())
            LudoColor.YELLOW -> GridPos((13 - subStep).toFloat(), 7f)
            LudoColor.GREEN -> GridPos(7f, (1 + subStep).toFloat())
        }
    }

    fun getTokenGridPos(token: Token): GridPos {
        if (token.isInBase) {
            return getBaseSlotPos(token.color, token.id)
        }
        if (token.stepCount in 51..56) {
            return getHomeRunPos(token.color, token.stepCount)
        }
        val trackIdx = token.trackTileIndex
        val (gx, gy) = TRACK_COORDINATES[trackIdx]
        return GridPos(gx.toFloat(), gy.toFloat())
    }
}
