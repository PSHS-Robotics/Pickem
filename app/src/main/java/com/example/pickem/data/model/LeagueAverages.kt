/*
 * This model holds the league-wide averages that the prediction engine uses as its baseline.
 * These values are calculated in memory from the team stats asset and are not stored permanently.
 */
package com.example.pickem.data.model

data class LeagueAverages(
    val leagueAverageTwoPointMakesPerGame: Double,
    val leagueAverageThreePointMakesPerGame: Double,
    val leagueAverageFreeThrowsMadePerGame: Double,
    val leagueAverageTwoPointMakesAllowedPerGame: Double,
    val leagueAverageThreePointMakesAllowedPerGame: Double,
    val leagueAverageFreeThrowsMadeAllowedPerGame: Double
)
