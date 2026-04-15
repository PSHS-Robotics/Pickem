/*
 * This model holds one team's normalized attack and defense ratings for the deterministic engine.
 * These ratings are derived from raw stats and league averages only while the app is running.
 */
package com.example.pickem.data.model

data class TeamRatings(
    val teamName: String,
    val teamTwoPointAttackRating: Double,
    val teamThreePointAttackRating: Double,
    val teamFreeThrowAttackRating: Double,
    val teamTwoPointDefenseRating: Double,
    val teamThreePointDefenseRating: Double,
    val teamFreeThrowDefenseRating: Double
)
