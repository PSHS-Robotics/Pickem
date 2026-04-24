/*
 * This model groups the main deterministic calculation outputs in one place for verification.
 * It keeps the final prediction together with the league averages and team ratings used to create it.
 */
package com.example.pickem.data.model

data class DeterministicPredictionResult(
    val leagueAverages: LeagueAverages,
    val awayTeamRatings: TeamRatings,
    val homeTeamRatings: TeamRatings,
    val modelPrediction: ModelPrediction
)
