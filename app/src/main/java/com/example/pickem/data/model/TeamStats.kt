/*
 * This model represents the stat inputs for one team.
 * Those stats are the numbers the app will later use to generate its own line projections.
 */
package com.example.pickem.data.model

data class TeamStats(
    val teamName: String,
    val teamTwoPointMakesPerGame: Double,
    val teamThreePointMakesPerGame: Double,
    val teamFreeThrowsMadePerGame: Double,
    val teamTwoPointMakesAllowedPerGame: Double,
    val teamThreePointMakesAllowedPerGame: Double,
    val teamFreeThrowsMadeAllowedPerGame: Double
)
