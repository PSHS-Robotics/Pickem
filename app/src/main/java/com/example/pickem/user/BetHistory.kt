package com.example.pickem.user

import java.util.UUID

data class BetHistory(
    val id: String = UUID.randomUUID().toString(),
    val gameID: String,
    val amountBet: Double,
    val win: Boolean,
    val amountWon: Double
)