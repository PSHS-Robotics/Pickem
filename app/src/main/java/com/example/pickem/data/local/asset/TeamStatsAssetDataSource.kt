/*
 * This file will load the local team stats used by the prediction model.
 * The stats come from a bundled asset file, so they act as fixed input data for the app.
 */
package com.example.pickem.data.local.asset

import android.content.Context
import com.example.pickem.data.model.TeamStats
import org.json.JSONObject

class TeamStatsAssetDataSource {

    /**
     * Reads the team stats file from assets and turns it into a list of TeamStats objects.
     */
    fun loadTeamStats(context: Context): List<TeamStats> {
        val jsonText = context.assets.open("team_stats.json").bufferedReader().use { it.readText() }
        val teamsArray = JSONObject(jsonText).getJSONArray("teams")
        val teams = mutableListOf<TeamStats>()

        for (index in 0 until teamsArray.length()) {
            val teamObject = teamsArray.getJSONObject(index)

            teams.add(
                TeamStats(
                    teamName = teamObject.getString("teamName"),
                    teamTwoPointMakesPerGame = teamObject.getDouble("teamTwoPointMakesPerGame"),
                    teamThreePointMakesPerGame = teamObject.getDouble("teamThreePointMakesPerGame"),
                    teamFreeThrowsMadePerGame = teamObject.getDouble("teamFreeThrowsMadePerGame"),
                    teamTwoPointMakesAllowedPerGame = teamObject.getDouble("teamTwoPointMakesAllowedPerGame"),
                    teamThreePointMakesAllowedPerGame = teamObject.getDouble("teamThreePointMakesAllowedPerGame"),
                    teamFreeThrowsMadeAllowedPerGame = teamObject.getDouble("teamFreeThrowsMadeAllowedPerGame")
                )
            )
        }

        return teams
    }
}
