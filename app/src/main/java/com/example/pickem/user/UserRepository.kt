package com.example.pickem.user

object UserRepository {

    var currentUser: User? = null

    fun addBet(
        gameId: String,
        amountBet: Double,
        win: Boolean,
        amountWon: Double
    ) {
        val user = currentUser ?: return

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