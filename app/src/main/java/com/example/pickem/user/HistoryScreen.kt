package com.example.pickem.user

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun HistoryScreen(
    user: User?,
    onBackClick: () -> Unit
) {
    BackHandler {
        onBackClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        UserHeader()

        Button(onClick = { onBackClick() }) {
            Text("Home")
        }

        Text("Bet History", style = MaterialTheme.typography.headlineMedium)

        user?.history?.forEach { bet ->
            Card(modifier = Modifier.padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("${bet.awayTeam} at ${bet.homeTeam}")
                    Text("Bet Type: ${bet.betType}")
                    Text("Pick: ${bet.selectedSide}")
                    bet.line?.let { Text("Line: $it") }
                    Text("Odds: ${formatOdds(bet.odds)}")
                    Text("Amount Bet: ${formatMoney(bet.amountBet)}")
                    Text("Potential Profit: ${formatMoney(bet.potentialProfit)}")
                    Text("Result: ${if (bet.win) "Won" else "Lost"}")
                    Text("Amount Won: ${formatMoney(bet.amountWon)}")
                }
            }
        }
    }
}

private fun formatOdds(odds: Int): String {
    return if (odds > 0) "+$odds" else odds.toString()
}

private fun formatMoney(amount: Double): String {
    return "$${"%.2f".format(amount)}"
}
