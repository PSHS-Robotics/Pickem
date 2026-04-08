/*
* Screen with current game displayed
* */
package com.example.pickem.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pickem.data.model.Game
import com.example.pickem.data.repository.GameRepository

/**
 * Shows the seeded games from assets along with their fake sportsbook lines.
 */
@Composable
fun GamesScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val games = remember(context) { GameRepository().getGames(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Games")

        games.forEach { game ->
            GameCard(game = game)
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Home")
        }
    }
}

/**
 * Shows one game's teams and basic sportsbook line information.
 */
@Composable
fun GameCard(game: Game) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "${game.awayTeam} at ${game.homeTeam}")
            Text(text = "Over/Under: ${game.sportsbookLine.overUnder}")
            Text(text = "Home Moneyline: ${game.sportsbookLine.homeMoneyline}")
            Text(text = "Away Moneyline: ${game.sportsbookLine.awayMoneyline}")
            Text(text = "Home Spread: ${game.sportsbookLine.homeSpread}")
        }
    }
}
