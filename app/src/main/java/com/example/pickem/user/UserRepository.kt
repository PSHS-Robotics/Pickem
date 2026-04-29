package com.example.pickem.user

import kotlin.math.abs

object UserRepository {

    var currentUser: User? = null

    fun addBet(
        gameId: String,
        amountBet: Double,
        win: Boolean,
        odds: Double,

    ) {
        val user = currentUser ?: return

        var amountWon = 0.0
        if (odds >= 0){
            amountWon = amountBet * odds
        } else {
            amountWon = amountBet * 1 - abs(odds)
        }


        val bet = BetHistory(
            gameID = gameId,
            amountBet = amountBet,
            win = win,
            amountWon = amountWon
        )

        user.history.add(bet)

        if (win) {
            user.balance += amountWon
        } else {
            user.balance -= amountBet
        }
    }
}