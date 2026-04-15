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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pickem.data.model.Game
import com.example.pickem.data.model.ModelPrediction
import java.util.Locale

/**
 * Shows the seeded games from the GamesViewModel along with their fake sportsbook lines.
 */
@Composable
fun GamesScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val gamesViewModel = remember { GamesViewModel() }

    /**
     * Loads the game list when this screen first appears.
     */
    LaunchedEffect(Unit) {
        gamesViewModel.loadGames(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Games")

        gamesViewModel.games.forEach { game ->
            GameCard(
                game = game,
                prediction = gamesViewModel.getPredictionForGame(game.id),
                isAnalyzing = gamesViewModel.isAnalyzingGame(game.id),
                onAnalyzeClick = { gamesViewModel.analyzeGame(context, game) }
            )
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
 * Shows one game's sportsbook information and any prediction result tied to that game.
 */
@Composable
fun GameCard(
    game: Game,
    prediction: ModelPrediction?,
    isAnalyzing: Boolean,
    onAnalyzeClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "${game.awayTeam} at ${game.homeTeam}")
            Text(text = "Sportsbook")
            Text(text = "Over/Under: ${game.sportsbookLine.overUnder}")
            Text(text = "Home Moneyline: ${game.sportsbookLine.homeMoneyline}")
            Text(text = "Away Moneyline: ${game.sportsbookLine.awayMoneyline}")
            Text(text = "Home Spread: ${game.sportsbookLine.homeSpread}")

            Button(
                onClick = onAnalyzeClick,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(if (isAnalyzing) "Analyzing..." else "Analyze")
            }

            if (prediction != null) {
                PredictionSection(prediction = prediction)
            }
        }
    }
}

/**
 * Shows the prediction output for one analyzed game in a simple readable block.
 */
@Composable
fun PredictionSection(prediction: ModelPrediction) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = "Model Output")
        Text(
            text = "Expected Points: ${prediction.awayTeamName} ${formatDouble(prediction.awayTeamExpectedPoints)}, ${prediction.homeTeamName} ${formatDouble(prediction.homeTeamExpectedPoints)}"
        )
        Text(
            text = "Average Sim Score: ${prediction.awayTeamName} ${formatDouble(prediction.averageSimulatedAwayTeamScore)}, ${prediction.homeTeamName} ${formatDouble(prediction.averageSimulatedHomeTeamScore)}"
        )

        Text(
            text = "Moneyline Odds: Away ${prediction.awayTeamModelMoneylineOdds}, Home ${prediction.homeTeamModelMoneylineOdds}"
        )
        Text(
            text = "Spread Cover Prob: Away ${formatPercent(prediction.awayTeamSpreadCoverProbability)}, Home ${formatPercent(prediction.homeTeamSpreadCoverProbability)}"
        )
        Text(
            text = "Spread Odds: Away ${prediction.awayTeamModelSpreadOdds}, Home ${prediction.homeTeamModelSpreadOdds}"
        )
        Text(
            text = "Total Prob: Over ${formatPercent(prediction.overProbability)}, Under ${formatPercent(prediction.underProbability)}"
        )
        Text(
            text = "Total Odds: Over ${prediction.modelOverOdds}, Under ${prediction.modelUnderOdds}"
        )
    }
}

/**
 * Formats decimal values so the card can show readable numbers without too many digits.
 */
fun formatDouble(value: Double): String {
    return String.format(Locale.US, "%.1f", value)
}

/**
 * Formats probability values as simple percentages for the card.
 */
fun formatPercent(value: Double): String {
    return String.format(Locale.US, "%.1f%%", value * 100)
}
