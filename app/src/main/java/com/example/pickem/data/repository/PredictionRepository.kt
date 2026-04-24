/*
 * This repository coordinates deterministic prediction work for one game.
 * It loads team stats, finds the matching teams, and sends the matchup to the prediction engine.
 */
package com.example.pickem.data.repository

import android.content.Context
import com.example.pickem.data.local.asset.TeamStatsAssetDataSource
import com.example.pickem.data.model.DeterministicPredictionResult
import com.example.pickem.data.model.Game
import com.example.pickem.engine.PredictionEngine

class PredictionRepository(
    private val teamStatsAssetDataSource: TeamStatsAssetDataSource = TeamStatsAssetDataSource(),
    private val predictionEngine: PredictionEngine = PredictionEngine()
) {

    /**
     * Loads the away and home team stats for a game and runs the full prediction engine.
     */
    fun predictGame(context: Context, game: Game): DeterministicPredictionResult {
        val allTeamStats = teamStatsAssetDataSource.loadTeamStats(context)
        val awayTeamStats = allTeamStats.firstOrNull { it.teamName == game.awayTeam }
            ?: error("Missing team stats for away team: ${game.awayTeam}")
        val homeTeamStats = allTeamStats.firstOrNull { it.teamName == game.homeTeam }
            ?: error("Missing team stats for home team: ${game.homeTeam}")

        return predictionEngine.predictMatchup(
            allTeamStats = allTeamStats,
            awayTeamStats = awayTeamStats,
            homeTeamStats = homeTeamStats,
            sportsbookLine = game.sportsbookLine
        )
    }
}
