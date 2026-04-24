/*
 * This repository gives the rest of the app a simple way to request seeded games.
 */
package com.example.pickem.data.repository

import android.content.Context
import com.example.pickem.data.local.asset.GameAssetDataSource
import com.example.pickem.data.local.asset.TeamStatsAssetDataSource
import com.example.pickem.data.model.Game

class GameRepository(
    private val gameAssetDataSource: GameAssetDataSource = GameAssetDataSource(),
    private val teamStatsAssetDataSource: TeamStatsAssetDataSource = TeamStatsAssetDataSource()
) {

    /**
     * Returns the list of pre-made games stored in the app assets.
     */
    fun getGames(context: Context): List<Game> {
        return gameAssetDataSource.loadGames(context)
    }

    /**
     * Checks whether every team used in the games list exists in the team stats asset.
     */
    fun findMissingTeamStats(context: Context): List<String> {
        val games = getGames(context)
        val knownTeamNames = teamStatsAssetDataSource.loadTeamStats(context)
            .map { it.teamName }
            .toSet()

        return games
            .flatMap { listOf(it.awayTeam, it.homeTeam) }
            .distinct()
            .filter { it !in knownTeamNames }
    }
}
