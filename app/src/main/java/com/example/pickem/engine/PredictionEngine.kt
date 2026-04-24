/*
 * This engine performs the deterministic matchup math and the Phase 2B simulation layer for one game.
 * It returns expected scoring values plus betting probabilities and model odds built from 2500 simulations.
 */
package com.example.pickem.engine

import com.example.pickem.data.model.DeterministicPredictionResult
import com.example.pickem.data.model.LeagueAverages
import com.example.pickem.data.model.ModelPrediction
import com.example.pickem.data.model.SportsbookLine
import com.example.pickem.data.model.TeamRatings
import com.example.pickem.data.model.TeamStats
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class PredictionEngine {

    /**
     * Runs the deterministic engine and simulation layer for one away team and one home team.
     */
    fun predictMatchup(
        allTeamStats: List<TeamStats>,
        awayTeamStats: TeamStats,
        homeTeamStats: TeamStats,
        sportsbookLine: SportsbookLine
    ): DeterministicPredictionResult {
        val leagueAverages = calculateLeagueAverages(allTeamStats)
        val awayTeamRatings = calculateTeamRatings(awayTeamStats, leagueAverages)
        val homeTeamRatings = calculateTeamRatings(homeTeamStats, leagueAverages)
        val homeCourtAdvantagePercent = 0.025
        val awayTeamHomeCourtMultiplier = 1 / sqrt(1 + homeCourtAdvantagePercent)
        val homeTeamHomeCourtMultiplier = sqrt(1 + homeCourtAdvantagePercent)

        val awayTeamExpectedTwoPointMakes =
            awayTeamRatings.teamTwoPointAttackRating *
                homeTeamRatings.teamTwoPointDefenseRating *
                leagueAverages.leagueAverageTwoPointMakesPerGame *
                awayTeamHomeCourtMultiplier

        val awayTeamExpectedThreePointMakes =
            awayTeamRatings.teamThreePointAttackRating *
                homeTeamRatings.teamThreePointDefenseRating *
                leagueAverages.leagueAverageThreePointMakesPerGame *
                awayTeamHomeCourtMultiplier

        val awayTeamExpectedFreeThrowsMade =
            awayTeamRatings.teamFreeThrowAttackRating *
                homeTeamRatings.teamFreeThrowDefenseRating *
                leagueAverages.leagueAverageFreeThrowsMadePerGame *
                awayTeamHomeCourtMultiplier

        val homeTeamExpectedTwoPointMakes =
            homeTeamRatings.teamTwoPointAttackRating *
                awayTeamRatings.teamTwoPointDefenseRating *
                leagueAverages.leagueAverageTwoPointMakesPerGame *
                homeTeamHomeCourtMultiplier

        val homeTeamExpectedThreePointMakes =
            homeTeamRatings.teamThreePointAttackRating *
                awayTeamRatings.teamThreePointDefenseRating *
                leagueAverages.leagueAverageThreePointMakesPerGame *
                homeTeamHomeCourtMultiplier

        val homeTeamExpectedFreeThrowsMade =
            homeTeamRatings.teamFreeThrowAttackRating *
                awayTeamRatings.teamFreeThrowDefenseRating *
                leagueAverages.leagueAverageFreeThrowsMadePerGame *
                homeTeamHomeCourtMultiplier

        val simulationSummary = runSimulations(
            awayTeamExpectedTwoPointMakes = awayTeamExpectedTwoPointMakes,
            awayTeamExpectedThreePointMakes = awayTeamExpectedThreePointMakes,
            awayTeamExpectedFreeThrowsMade = awayTeamExpectedFreeThrowsMade,
            homeTeamExpectedTwoPointMakes = homeTeamExpectedTwoPointMakes,
            homeTeamExpectedThreePointMakes = homeTeamExpectedThreePointMakes,
            homeTeamExpectedFreeThrowsMade = homeTeamExpectedFreeThrowsMade,
            sportsbookLine = sportsbookLine
        )

        val modelPrediction = ModelPrediction(
            awayTeamName = awayTeamStats.teamName,
            homeTeamName = homeTeamStats.teamName,
            awayTeamExpectedTwoPointMakes = awayTeamExpectedTwoPointMakes,
            awayTeamExpectedThreePointMakes = awayTeamExpectedThreePointMakes,
            awayTeamExpectedFreeThrowsMade = awayTeamExpectedFreeThrowsMade,
            homeTeamExpectedTwoPointMakes = homeTeamExpectedTwoPointMakes,
            homeTeamExpectedThreePointMakes = homeTeamExpectedThreePointMakes,
            homeTeamExpectedFreeThrowsMade = homeTeamExpectedFreeThrowsMade,
            // Expected points are the sum of each shot type's point contribution.
            awayTeamExpectedPoints =
                (awayTeamExpectedTwoPointMakes * 2) +
                    (awayTeamExpectedThreePointMakes * 3) +
                    awayTeamExpectedFreeThrowsMade,
            homeTeamExpectedPoints =
                (homeTeamExpectedTwoPointMakes * 2) +
                    (homeTeamExpectedThreePointMakes * 3) +
                    homeTeamExpectedFreeThrowsMade,
            awayTeamHomeCourtMultiplier = awayTeamHomeCourtMultiplier,
            homeTeamHomeCourtMultiplier = homeTeamHomeCourtMultiplier,
            awayTeamWinProbability = simulationSummary.awayTeamWinProbability,
            homeTeamWinProbability = simulationSummary.homeTeamWinProbability,
            awayTeamModelMoneylineOdds = probabilityToAmericanOdds(simulationSummary.awayTeamWinProbability),
            homeTeamModelMoneylineOdds = probabilityToAmericanOdds(simulationSummary.homeTeamWinProbability),
            awayTeamSpreadCoverProbability = simulationSummary.awayTeamSpreadCoverProbability,
            homeTeamSpreadCoverProbability = simulationSummary.homeTeamSpreadCoverProbability,
            awayTeamModelSpreadOdds = probabilityToAmericanOdds(simulationSummary.awayTeamSpreadCoverProbability),
            homeTeamModelSpreadOdds = probabilityToAmericanOdds(simulationSummary.homeTeamSpreadCoverProbability),
            overProbability = simulationSummary.overProbability,
            underProbability = simulationSummary.underProbability,
            modelOverOdds = probabilityToAmericanOdds(simulationSummary.overProbability),
            modelUnderOdds = probabilityToAmericanOdds(simulationSummary.underProbability),
            averageSimulatedAwayTeamScore = simulationSummary.averageSimulatedAwayTeamScore,
            averageSimulatedHomeTeamScore = simulationSummary.averageSimulatedHomeTeamScore,
            sportsbookHomeSpread = sportsbookLine.homeSpread,
            sportsbookOverUnderLine = sportsbookLine.overUnder
        )

        return DeterministicPredictionResult(
            leagueAverages = leagueAverages,
            awayTeamRatings = awayTeamRatings,
            homeTeamRatings = homeTeamRatings,
            modelPrediction = modelPrediction
        )
    }

    /**
     * Calculates the six league-average baseline values from every team in the asset.
     */
    fun calculateLeagueAverages(allTeamStats: List<TeamStats>): LeagueAverages {
        return LeagueAverages(
            leagueAverageTwoPointMakesPerGame = allTeamStats.map { it.teamTwoPointMakesPerGame }.average(),
            leagueAverageThreePointMakesPerGame = allTeamStats.map { it.teamThreePointMakesPerGame }.average(),
            leagueAverageFreeThrowsMadePerGame = allTeamStats.map { it.teamFreeThrowsMadePerGame }.average(),
            leagueAverageTwoPointMakesAllowedPerGame = allTeamStats.map { it.teamTwoPointMakesAllowedPerGame }.average(),
            leagueAverageThreePointMakesAllowedPerGame = allTeamStats.map { it.teamThreePointMakesAllowedPerGame }.average(),
            leagueAverageFreeThrowsMadeAllowedPerGame = allTeamStats.map { it.teamFreeThrowsMadeAllowedPerGame }.average()
        )
    }

    /**
     * Converts one team's raw stats into normalized attack and defense ratings.
     */
    fun calculateTeamRatings(teamStats: TeamStats, leagueAverages: LeagueAverages): TeamRatings {
        return TeamRatings(
            teamName = teamStats.teamName,
            teamTwoPointAttackRating =
                teamStats.teamTwoPointMakesPerGame / leagueAverages.leagueAverageTwoPointMakesPerGame,
            teamThreePointAttackRating =
                teamStats.teamThreePointMakesPerGame / leagueAverages.leagueAverageThreePointMakesPerGame,
            teamFreeThrowAttackRating =
                teamStats.teamFreeThrowsMadePerGame / leagueAverages.leagueAverageFreeThrowsMadePerGame,
            teamTwoPointDefenseRating =
                teamStats.teamTwoPointMakesAllowedPerGame / leagueAverages.leagueAverageTwoPointMakesAllowedPerGame,
            teamThreePointDefenseRating =
                teamStats.teamThreePointMakesAllowedPerGame / leagueAverages.leagueAverageThreePointMakesAllowedPerGame,
            teamFreeThrowDefenseRating =
                teamStats.teamFreeThrowsMadeAllowedPerGame / leagueAverages.leagueAverageFreeThrowsMadeAllowedPerGame
        )
    }

    /**
     * Runs 2500 simulated games using the deterministic expected values as the center of each random draw.
     */
    private fun runSimulations(
        awayTeamExpectedTwoPointMakes: Double,
        awayTeamExpectedThreePointMakes: Double,
        awayTeamExpectedFreeThrowsMade: Double,
        homeTeamExpectedTwoPointMakes: Double,
        homeTeamExpectedThreePointMakes: Double,
        homeTeamExpectedFreeThrowsMade: Double,
        sportsbookLine: SportsbookLine
    ): SimulationSummary {
        val numberOfSimulations = 2500
        val overtimeLengthMultiplier = 5.0 / 48.0
        val maximumNumberOfOvertimePeriods = 3

        var awayWins = 0
        var homeWins = 0
        var awaySpreadCovers = 0
        var homeSpreadCovers = 0
        var overs = 0
        var unders = 0
        var totalAwayScore = 0.0
        var totalHomeScore = 0.0

        repeat(numberOfSimulations) {
            var awayFinalScore = calculateScoreFromExpectedMakes(
                expectedTwoPointMakes = awayTeamExpectedTwoPointMakes,
                expectedThreePointMakes = awayTeamExpectedThreePointMakes,
                expectedFreeThrowsMade = awayTeamExpectedFreeThrowsMade
            )
            var homeFinalScore = calculateScoreFromExpectedMakes(
                expectedTwoPointMakes = homeTeamExpectedTwoPointMakes,
                expectedThreePointMakes = homeTeamExpectedThreePointMakes,
                expectedFreeThrowsMade = homeTeamExpectedFreeThrowsMade
            )

            var overtimePeriodsPlayed = 0
            while (awayFinalScore == homeFinalScore && overtimePeriodsPlayed < maximumNumberOfOvertimePeriods) {
                awayFinalScore += calculateScoreFromExpectedMakes(
                    expectedTwoPointMakes = awayTeamExpectedTwoPointMakes * overtimeLengthMultiplier,
                    expectedThreePointMakes = awayTeamExpectedThreePointMakes * overtimeLengthMultiplier,
                    expectedFreeThrowsMade = awayTeamExpectedFreeThrowsMade * overtimeLengthMultiplier
                )
                homeFinalScore += calculateScoreFromExpectedMakes(
                    expectedTwoPointMakes = homeTeamExpectedTwoPointMakes * overtimeLengthMultiplier,
                    expectedThreePointMakes = homeTeamExpectedThreePointMakes * overtimeLengthMultiplier,
                    expectedFreeThrowsMade = homeTeamExpectedFreeThrowsMade * overtimeLengthMultiplier
                )
                overtimePeriodsPlayed += 1
            }

            if (awayFinalScore > homeFinalScore) {
                awayWins += 1
            } else if (homeFinalScore > awayFinalScore) {
                homeWins += 1
            }

            if (homeTeamCoveredSpread(homeFinalScore, awayFinalScore, sportsbookLine.homeSpread)) {
                homeSpreadCovers += 1
            } else if (awayTeamCoveredSpread(homeFinalScore, awayFinalScore, sportsbookLine.homeSpread)) {
                awaySpreadCovers += 1
            }

            val simulatedGameTotalPoints = awayFinalScore + homeFinalScore
            if (simulatedGameTotalPoints > sportsbookLine.overUnder) {
                overs += 1
            } else if (simulatedGameTotalPoints < sportsbookLine.overUnder) {
                unders += 1
            }

            totalAwayScore += awayFinalScore
            totalHomeScore += homeFinalScore
        }

        return SimulationSummary(
            awayTeamWinProbability = awayWins.toDouble() / numberOfSimulations,
            homeTeamWinProbability = homeWins.toDouble() / numberOfSimulations,
            awayTeamSpreadCoverProbability = awaySpreadCovers.toDouble() / numberOfSimulations,
            homeTeamSpreadCoverProbability = homeSpreadCovers.toDouble() / numberOfSimulations,
            overProbability = overs.toDouble() / numberOfSimulations,
            underProbability = unders.toDouble() / numberOfSimulations,
            averageSimulatedAwayTeamScore = totalAwayScore / numberOfSimulations,
            averageSimulatedHomeTeamScore = totalHomeScore / numberOfSimulations
        )
    }

    /**
     * Simulates one team's score by drawing random made shots around the expected values and converting them to points.
     */
    private fun calculateScoreFromExpectedMakes(
        expectedTwoPointMakes: Double,
        expectedThreePointMakes: Double,
        expectedFreeThrowsMade: Double
    ): Int {
        val simulatedTwoPointMakes = samplePoisson(expectedTwoPointMakes)
        val simulatedThreePointMakes = samplePoisson(expectedThreePointMakes)
        val simulatedFreeThrowsMade = samplePoisson(expectedFreeThrowsMade)

        return (simulatedTwoPointMakes * 2) +
            (simulatedThreePointMakes * 3) +
            simulatedFreeThrowsMade
    }

    /**
     * Simulates an integer count centered on an expected value using a simple Poisson draw.
     */
    private fun samplePoisson(expectedValue: Double): Int {
        if (expectedValue <= 0.0) {
            return 0
        }

        val limit = kotlin.math.exp(-expectedValue)
        var product = 1.0
        var count = 0

        do {
            count += 1
            product *= Random.nextDouble()
        } while (product > limit)

        return count - 1
    }

    /**
     * Checks whether the home team covered the listed home spread.
     */
    private fun homeTeamCoveredSpread(
        homeFinalScore: Int,
        awayFinalScore: Int,
        homeSpread: Double
    ): Boolean {
        val adjustedHomeScore = homeFinalScore + homeSpread
        return adjustedHomeScore > awayFinalScore
    }

    /**
     * Checks whether the away team covered the listed home spread.
     */
    private fun awayTeamCoveredSpread(
        homeFinalScore: Int,
        awayFinalScore: Int,
        homeSpread: Double
    ): Boolean {
        val adjustedHomeScore = homeFinalScore + homeSpread
        return awayFinalScore > adjustedHomeScore
    }

    /**
     * Converts a probability into American odds while guarding against impossible edge-case values.
     */
    fun probabilityToAmericanOdds(probability: Double): Int {
        val safeProbability = probability.coerceIn(0.0001, 0.9999)

        return if (safeProbability > 0.50) {
            (-100.0 / ((1.0 / safeProbability) - 1.0)).roundToInt()
        } else {
            (((1.0 / safeProbability) - 1.0) * 100.0).roundToInt()
        }
    }

    private data class SimulationSummary(
        val awayTeamWinProbability: Double,
        val homeTeamWinProbability: Double,
        val awayTeamSpreadCoverProbability: Double,
        val homeTeamSpreadCoverProbability: Double,
        val overProbability: Double,
        val underProbability: Double,
        val averageSimulatedAwayTeamScore: Double,
        val averageSimulatedHomeTeamScore: Double
    )
}
