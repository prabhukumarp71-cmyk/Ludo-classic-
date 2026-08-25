package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.LudoAi
import com.example.engine.LudoEngine
import com.example.engine.MoveResult
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ludo Classic", appName)
  }

  @Test
  fun testTokenBaseExitOnSix() {
    val player = Player(id = 0, name = "Red", color = LudoColor.RED)
    val legalMovesOn5 = LudoEngine.getLegalMoves(player, 5)
    assertTrue("Cannot move out of base without rolling a 6", legalMovesOn5.isEmpty())

    val legalMovesOn6 = LudoEngine.getLegalMoves(player, 6)
    assertEquals(4, legalMovesOn6.size)
  }

  @Test
  fun testTokenMovementAndCapture() {
    val redPlayer = Player(
      id = 0,
      name = "Red",
      color = LudoColor.RED,
      tokens = listOf(Token(id = 0, color = LudoColor.RED, stepCount = 0))
    )
    val yellowPlayer = Player(
      id = 1,
      name = "Yellow",
      color = LudoColor.YELLOW,
      // Yellow start offset is 26. Track tile 2 is step 28 for Yellow: (28 - 26 + 52) % 52 = 28
      tokens = listOf(Token(id = 0, color = LudoColor.YELLOW, stepCount = 28))
    )
    val players = listOf(redPlayer, yellowPlayer)

    // Red rolls 2 and moves from step 0 to step 2 (track index 2, not a safe square)
    val result = LudoEngine.executeMove(players, 0, 0, 2)
    assertTrue(result is MoveResult.Success)
    val success = result as MoveResult.Success
    assertEquals(1, success.capturedTokens.size)
    assertEquals(-1, success.updatedPlayers[1].tokens[0].stepCount) // Captured Yellow returned to base
  }

  @Test
  fun testAiSelectsCapture() {
    val redPlayer = Player(
      id = 0,
      name = "Red",
      color = LudoColor.RED,
      tokens = listOf(
        Token(id = 0, color = LudoColor.RED, stepCount = 0),
        Token(id = 1, color = LudoColor.RED, stepCount = 10)
      )
    )
    val yellowPlayer = Player(
      id = 1,
      name = "Yellow",
      color = LudoColor.YELLOW,
      tokens = listOf(Token(id = 0, color = LudoColor.YELLOW, stepCount = 28)) // Track tile 2
    )
    // Red rolls 2. Token 0 can capture Yellow at track tile 2 (0 + 2 = 2). Token 1 moves 10 -> 12.
    val move = LudoAi.selectTokenMove(redPlayer, listOf(redPlayer, yellowPlayer), 2, AiDifficulty.MEDIUM)
    assertEquals(0, move)
  }
}
