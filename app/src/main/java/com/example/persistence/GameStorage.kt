package com.example.persistence

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

class GameStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ludo_game_prefs", Context.MODE_PRIVATE)

    fun saveSoundMuted(isMuted: Boolean) {
        prefs.edit().putBoolean("sound_muted", isMuted).apply()
    }

    fun isSoundMuted(): Boolean {
        return prefs.getBoolean("sound_muted", false)
    }

    fun saveFastMode(isFast: Boolean) {
        prefs.edit().putBoolean("fast_mode", isFast).apply()
    }

    fun isFastMode(): Boolean {
        return prefs.getBoolean("fast_mode", false)
    }

    fun saveActiveGame(players: List<Player>, currentTurnIndex: Int, diceValue: Int, isDiceRolled: Boolean) {
        try {
            val root = JSONObject()
            root.put("currentTurnIndex", currentTurnIndex)
            root.put("diceValue", diceValue)
            root.put("isDiceRolled", isDiceRolled)

            val playersArr = JSONArray()
            for (p in players) {
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("name", p.name)
                pObj.put("color", p.color.name)
                pObj.put("type", p.type.name)
                pObj.put("difficulty", p.difficulty.name)
                pObj.put("rank", p.rank)
                pObj.put("capturesMade", p.capturesMade)
                pObj.put("totalMoves", p.totalMoves)
                pObj.put("sixesRolled", p.sixesRolled)

                val tokensArr = JSONArray()
                for (t in p.tokens) {
                    val tObj = JSONObject()
                    tObj.put("id", t.id)
                    tObj.put("stepCount", t.stepCount)
                    tokensArr.put(tObj)
                }
                pObj.put("tokens", tokensArr)
                playersArr.put(pObj)
            }
            root.put("players", playersArr)

            prefs.edit().putString("saved_game_state", root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearSavedGame() {
        prefs.edit().remove("saved_game_state").apply()
    }

    fun hasSavedGame(): Boolean {
        return prefs.contains("saved_game_state")
    }

    fun loadSavedGame(): SavedGameData? {
        val jsonStr = prefs.getString("saved_game_state", null) ?: return null
        return try {
            val root = JSONObject(jsonStr)
            val currentTurnIndex = root.getInt("currentTurnIndex")
            val diceValue = root.getInt("diceValue")
            val isDiceRolled = root.getBoolean("isDiceRolled")

            val playersArr = root.getJSONArray("players")
            val playersList = mutableListOf<Player>()

            for (i in 0 until playersArr.length()) {
                val pObj = playersArr.getJSONObject(i)
                val id = pObj.getInt("id")
                val name = pObj.getString("name")
                val color = LudoColor.valueOf(pObj.getString("color"))
                val type = PlayerType.valueOf(pObj.getString("type"))
                val difficulty = AiDifficulty.valueOf(pObj.getString("difficulty"))
                val rank = pObj.getInt("rank")
                val capturesMade = pObj.optInt("capturesMade", 0)
                val totalMoves = pObj.optInt("totalMoves", 0)
                val sixesRolled = pObj.optInt("sixesRolled", 0)

                val tokensArr = pObj.getJSONArray("tokens")
                val tokensList = mutableListOf<Token>()
                for (j in 0 until tokensArr.length()) {
                    val tObj = tokensArr.getJSONObject(j)
                    val tId = tObj.getInt("id")
                    val stepCount = tObj.getInt("stepCount")
                    tokensList.add(Token(id = tId, color = color, stepCount = stepCount))
                }

                playersList.add(
                    Player(
                        id = id,
                        name = name,
                        color = color,
                        type = type,
                        difficulty = difficulty,
                        tokens = tokensList,
                        rank = rank,
                        capturesMade = capturesMade,
                        totalMoves = totalMoves,
                        sixesRolled = sixesRolled
                    )
                )
            }

            SavedGameData(
                players = playersList,
                currentTurnIndex = currentTurnIndex,
                diceValue = diceValue,
                isDiceRolled = isDiceRolled
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class SavedGameData(
    val players: List<Player>,
    val currentTurnIndex: Int,
    val diceValue: Int,
    val isDiceRolled: Boolean
)
