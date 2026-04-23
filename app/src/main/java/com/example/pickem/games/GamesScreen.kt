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

private const val STANDARD_SIDE_PRICE = -110

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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "${game.awayTeam} at ${game.homeTeam}")
            SportsbookSection(game = game)

            Button(
                onClick = onAnalyzeClick,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(if (isAnalyzing) "Analyzing..." else "Analyze")
            }

            if (prediction != null) {
                PredictionSection(
                    game = game,
                    prediction = prediction
                )
            }
        }
    }
}

/**
 * Shows the sportsbook lines so the user can compare the offer to the model price.
 */
@Composable
fun SportsbookSection(game: Game) {
    val awaySpread = -game.sportsbookLine.homeSpread

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "Sportsbook")
        Text(text = "${game.awayTeam} ML: ${formatAmericanOdds(game.sportsbookLine.awayMoneyline)}")
        Text(text = "${game.homeTeam} ML: ${formatAmericanOdds(game.sportsbookLine.homeMoneyline)}")
        Text(text = "${game.awayTeam} Spread: ${formatSpread(awaySpread)} (${formatAmericanOdds(STANDARD_SIDE_PRICE)})")
        Text(text = "${game.homeTeam} Spread: ${formatSpread(game.sportsbookLine.homeSpread)} (${formatAmericanOdds(STANDARD_SIDE_PRICE)})")
        Text(text = "Total: ${formatDouble(game.sportsbookLine.overUnder)} (${formatAmericanOdds(STANDARD_SIDE_PRICE)} both sides)")
    }
}

/**
 * Shows the prediction output grouped into raw model prices and simple edge summaries.
 */
@Composable
fun PredictionSection(
    game: Game,
    prediction: ModelPrediction
) {
    val moneylineRecommendation = buildMoneylineRecommendation(game, prediction)
    val spreadRecommendation = buildSpreadRecommendation(game, prediction)
    val totalRecommendation = buildTotalRecommendation(prediction)
    val bestRecommendation = listOf(
        moneylineRecommendation,
        spreadRecommendation,
        totalRecommendation
    ).maxByOrNull { it.edge }

    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Model")
        Text(
            text = "Expected Points: ${prediction.awayTeamName} ${formatDouble(prediction.awayTeamExpectedPoints)}, ${prediction.homeTeamName} ${formatDouble(prediction.homeTeamExpectedPoints)}"
        )
        Text(
            text = "Average Sim Score: ${prediction.awayTeamName} ${formatDouble(prediction.averageSimulatedAwayTeamScore)}, ${prediction.homeTeamName} ${formatDouble(prediction.averageSimulatedHomeTeamScore)}"
        )
        Text(text = "${prediction.awayTeamName} ML fair odds: ${formatAmericanOdds(prediction.awayTeamModelMoneylineOdds)}")
        Text(text = "${prediction.homeTeamName} ML fair odds: ${formatAmericanOdds(prediction.homeTeamModelMoneylineOdds)}")
        Text(text = "${prediction.awayTeamName} spread fair odds: ${formatAmericanOdds(prediction.awayTeamModelSpreadOdds)}")
        Text(text = "${prediction.homeTeamName} spread fair odds: ${formatAmericanOdds(prediction.homeTeamModelSpreadOdds)}")
        Text(text = "Over fair odds: ${formatAmericanOdds(prediction.modelOverOdds)}")
        Text(text = "Under fair odds: ${formatAmericanOdds(prediction.modelUnderOdds)}")

        Text(text = "Edge / Recommendation")
        RecommendationLine(
            market = "Moneyline",
            recommendation = moneylineRecommendation
        )
        RecommendationLine(
            market = "Spread",
            recommendation = spreadRecommendation
        )
        RecommendationLine(
            market = "Total",
            recommendation = totalRecommendation
        )
        Text(text = "Best Overall Edge: ${bestRecommendation?.label ?: "No Clear Edge"}")
    }
}

@Composable
private fun RecommendationLine(
    market: String,
    recommendation: RecommendationSummary
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = "$market: ${recommendation.label}")
        Text(text = recommendation.detail)
    }
}

private data class RecommendationSummary(
    val label: String,
    val detail: String,
    val edge: Double
)

private fun buildMoneylineRecommendation(
    game: Game,
    prediction: ModelPrediction
): RecommendationSummary {
    val awayEdge = calculateEdge(
        sportsbookOdds = game.sportsbookLine.awayMoneyline,
        modelOdds = prediction.awayTeamModelMoneylineOdds
    )
    val homeEdge = calculateEdge(
        sportsbookOdds = game.sportsbookLine.homeMoneyline,
        modelOdds = prediction.homeTeamModelMoneylineOdds
    )

    if (awayEdge > 0.0 && awayEdge >= homeEdge) {
        return RecommendationSummary(
            label = "Value Bet: ${prediction.awayTeamName} Moneyline",
            detail = buildComparisonDetail(
                sideName = "${prediction.awayTeamName} moneyline",
                sportsbookOdds = game.sportsbookLine.awayMoneyline,
                modelOdds = prediction.awayTeamModelMoneylineOdds,
                edge = awayEdge
            ),
            edge = awayEdge
        )
    }

    if (homeEdge > 0.0) {
        return RecommendationSummary(
            label = "Value Bet: ${prediction.homeTeamName} Moneyline",
            detail = buildComparisonDetail(
                sideName = "${prediction.homeTeamName} moneyline",
                sportsbookOdds = game.sportsbookLine.homeMoneyline,
                modelOdds = prediction.homeTeamModelMoneylineOdds,
                edge = homeEdge
            ),
            edge = homeEdge
        )
    }

    return RecommendationSummary(
        label = "No Clear Edge",
        detail = "Neither moneyline price beats the model fair odds.",
        edge = maxOf(awayEdge, homeEdge)
    )
}

private fun buildSpreadRecommendation(
    game: Game,
    prediction: ModelPrediction
): RecommendationSummary {
    val awaySpread = -game.sportsbookLine.homeSpread
    val awayEdge = calculateEdge(
        sportsbookOdds = STANDARD_SIDE_PRICE,
        modelOdds = prediction.awayTeamModelSpreadOdds
    )
    val homeEdge = calculateEdge(
        sportsbookOdds = STANDARD_SIDE_PRICE,
        modelOdds = prediction.homeTeamModelSpreadOdds
    )

    if (awayEdge > 0.0 && awayEdge >= homeEdge) {
        return RecommendationSummary(
            label = "Value Bet: ${prediction.awayTeamName} Spread",
            detail = "${prediction.awayTeamName} ${formatSpread(awaySpread)} at ${formatAmericanOdds(STANDARD_SIDE_PRICE)} vs model ${formatAmericanOdds(prediction.awayTeamModelSpreadOdds)} (${formatPercent(awayEdge)} edge).",
            edge = awayEdge
        )
    }

    if (homeEdge > 0.0) {
        return RecommendationSummary(
            label = "Value Bet: ${prediction.homeTeamName} Spread",
            detail = "${prediction.homeTeamName} ${formatSpread(game.sportsbookLine.homeSpread)} at ${formatAmericanOdds(STANDARD_SIDE_PRICE)} vs model ${formatAmericanOdds(prediction.homeTeamModelSpreadOdds)} (${formatPercent(homeEdge)} edge).",
            edge = homeEdge
        )
    }

    return RecommendationSummary(
        label = "No Clear Edge",
        detail = "Both spread sides are treated as ${formatAmericanOdds(STANDARD_SIDE_PRICE)} and neither beats the model fair odds.",
        edge = maxOf(awayEdge, homeEdge)
    )
}

private fun buildTotalRecommendation(prediction: ModelPrediction): RecommendationSummary {
    val overEdge = calculateEdge(
        sportsbookOdds = STANDARD_SIDE_PRICE,
        modelOdds = prediction.modelOverOdds
    )
    val underEdge = calculateEdge(
        sportsbookOdds = STANDARD_SIDE_PRICE,
        modelOdds = prediction.modelUnderOdds
    )

    if (overEdge > 0.0 && overEdge >= underEdge) {
        return RecommendationSummary(
            label = "Lean: Over",
            detail = buildComparisonDetail(
                sideName = "Over",
                sportsbookOdds = STANDARD_SIDE_PRICE,
                modelOdds = prediction.modelOverOdds,
                edge = overEdge
            ),
            edge = overEdge
        )
    }

    if (underEdge > 0.0) {
        return RecommendationSummary(
            label = "Lean: Under",
            detail = buildComparisonDetail(
                sideName = "Under",
                sportsbookOdds = STANDARD_SIDE_PRICE,
                modelOdds = prediction.modelUnderOdds,
                edge = underEdge
            ),
            edge = underEdge
        )
    }

    return RecommendationSummary(
        label = "No Clear Edge",
        detail = "Both total sides are treated as ${formatAmericanOdds(STANDARD_SIDE_PRICE)} and neither beats the model fair odds.",
        edge = maxOf(overEdge, underEdge)
    )
}

private fun buildComparisonDetail(
    sideName: String,
    sportsbookOdds: Int,
    modelOdds: Int,
    edge: Double
): String {
    return "$sideName: sportsbook ${formatAmericanOdds(sportsbookOdds)} vs model ${formatAmericanOdds(modelOdds)} (${formatPercent(edge)} edge)."
}

private fun calculateEdge(
    sportsbookOdds: Int,
    modelOdds: Int
): Double {
    return impliedProbability(modelOdds) - impliedProbability(sportsbookOdds)
}

private fun impliedProbability(odds: Int): Double {
    return if (odds > 0) {
        100.0 / (odds + 100.0)
    } else {
        -odds.toDouble() / (-odds + 100.0)
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

/**
 * Formats American odds with an explicit plus sign for positive values.
 */
fun formatAmericanOdds(odds: Int): String {
    return if (odds > 0) "+$odds" else odds.toString()
}

/**
 * Formats spread values with a leading plus sign when needed.
 */
fun formatSpread(value: Double): String {
    return if (value > 0) "+${formatDouble(value)}" else formatDouble(value)
}
