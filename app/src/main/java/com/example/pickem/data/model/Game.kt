/*
 * This model represents one scheduled game shown to the user.
 * It will later connect the matchup details to the sportsbook lines and analysis flow.
 */
package com.example.pickem.data.model

data class Game(
    val id: String,
    val awayTeam: String,
    val homeTeam: String,
    val sportsbookLine: SportsbookLine
)
