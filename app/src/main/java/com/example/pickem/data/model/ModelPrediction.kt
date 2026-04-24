/*
 * This model holds the deterministic and simulation-based outputs for one matchup.
 * It gives later phases one place to read expected scoring, betting probabilities, and model odds.
 */
package com.example.pickem.data.model

data class ModelPrediction(
    val awayTeamName: String,
    val homeTeamName: String,
    val awayTeamExpectedTwoPointMakes: Double,
    val awayTeamExpectedThreePointMakes: Double,
    val awayTeamExpectedFreeThrowsMade: Double,
    val homeTeamExpectedTwoPointMakes: Double,
    val homeTeamExpectedThreePointMakes: Double,
    val homeTeamExpectedFreeThrowsMade: Double,
    val awayTeamExpectedPoints: Double,
    val homeTeamExpectedPoints: Double,
    val awayTeamHomeCourtMultiplier: Double,
    val homeTeamHomeCourtMultiplier: Double,
    val awayTeamWinProbability: Double,
    val homeTeamWinProbability: Double,
    val awayTeamModelMoneylineOdds: Int,
    val homeTeamModelMoneylineOdds: Int,
    val awayTeamSpreadCoverProbability: Double,
    val homeTeamSpreadCoverProbability: Double,
    val awayTeamModelSpreadOdds: Int,
    val homeTeamModelSpreadOdds: Int,
    val overProbability: Double,
    val underProbability: Double,
    val modelOverOdds: Int,
    val modelUnderOdds: Int,
    val averageSimulatedAwayTeamScore: Double,
    val averageSimulatedHomeTeamScore: Double,
    val sportsbookHomeSpread: Double,
    val sportsbookOverUnderLine: Double
)
