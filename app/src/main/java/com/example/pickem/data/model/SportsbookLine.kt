/*
 * This model represents the fake sportsbook numbers shown for a game.
 * Examples include moneyline, spread, and over/under values.
 */
package com.example.pickem.data.model

data class SportsbookLine(
    val overUnder: Double,
    val homeMoneyline: Int,
    val awayMoneyline: Int,
    val homeSpread: Double
)
