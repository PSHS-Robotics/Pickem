/*
 * This file will be responsible for loading the pre-made game list from assets.
 * Those games are read-only seed data that ship with the app and do not change at runtime.
 */
package com.example.pickem.data.local.asset

import android.content.Context
import com.example.pickem.data.model.Game
import com.example.pickem.data.model.SportsbookLine
import org.json.JSONObject

class GameAssetDataSource {

    /**
     * Reads the seeded games file from assets and turns it into a list of Game objects.
     */
    fun loadGames(context: Context): List<Game> {
        val jsonText = context.assets.open("games.json").bufferedReader().use { it.readText() }
        val gamesArray = JSONObject(jsonText).getJSONArray("games")
        val games = mutableListOf<Game>()

        for (index in 0 until gamesArray.length()) {
            val gameObject = gamesArray.getJSONObject(index)
            val lineObject = gameObject.getJSONObject("sportsbookLine")

            games.add(
                Game(
                    id = gameObject.getString("id"),
                    awayTeam = gameObject.getString("awayTeam"),
                    homeTeam = gameObject.getString("homeTeam"),
                    sportsbookLine = SportsbookLine(
                        overUnder = lineObject.getDouble("overUnder"),
                        homeMoneyline = lineObject.getInt("homeMoneyline"),
                        awayMoneyline = lineObject.getInt("awayMoneyline"),
                        homeSpread = lineObject.getDouble("homeSpread")
                    )
                )
            )
        }

        return games
    }
}
