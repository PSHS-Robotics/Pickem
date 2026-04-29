package com.example.pickem.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun HistoryScreen(
    user: User?,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Button(onClick = onBackClick) {
            Text("Back")
        }

        Text("Bet History", style = MaterialTheme.typography.headlineMedium)

        user?.history?.forEach { bet ->
            Card(modifier = Modifier.padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Game: ${bet.gameID}")
                    Text("Bet: ${bet.amountBet}")
                    Text("Won: ${bet.win}")
                    Text("Payout: ${bet.amountWon}")
                }
            }
        }
    }
}