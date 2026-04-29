/*
* Screen with current game displayed
* */
package com.example.pickem.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
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
        Text(
            text = "Games",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GameHeader(game = game, prediction = prediction)
            SportsbookSection(game = game)
            ActionRow(
                isAnalyzing = isAnalyzing,
                onAnalyzeClick = onAnalyzeClick
            )

            if (prediction != null) {
                PredictionSection(
                    game = game,
                    prediction = prediction
                )
            }
        }
    }
}

@Composable
private fun GameHeader(
    game: Game,
    prediction: ModelPrediction?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "${game.awayTeam} at ${game.homeTeam}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusPill(
                text = if (prediction == null) "Awaiting analysis" else "Analysis ready",
                modifier = Modifier.weight(1f)
            )
            StatusPill(
                text = "Same-card insights",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Shows the sportsbook lines so the user can compare the offer to the model price.
 */
@Composable
fun SportsbookSection(game: Game) {
    val awaySpread = -game.sportsbookLine.homeSpread

    SectionCard(
        title = "Sportsbook",
        subtitle = "Current line and assumed market price"
    ) {
        TwoColumnGrid {
            MetricTile(
                label = "${game.awayTeam} ML",
                value = formatAmericanOdds(game.sportsbookLine.awayMoneyline)
            )
            MetricTile(
                label = "${game.homeTeam} ML",
                value = formatAmericanOdds(game.sportsbookLine.homeMoneyline)
            )
            MetricTile(
                label = "${game.awayTeam} Spread",
                value = "${formatSpread(awaySpread)}  •  ${formatAmericanOdds(STANDARD_SIDE_PRICE)}"
            )
            MetricTile(
                label = "${game.homeTeam} Spread",
                value = "${formatSpread(game.sportsbookLine.homeSpread)}  •  ${formatAmericanOdds(STANDARD_SIDE_PRICE)}"
            )
            MetricTile(
                label = "Total",
                value = formatDouble(game.sportsbookLine.overUnder)
            )
            MetricTile(
                label = "Market Price",
                value = "${formatAmericanOdds(STANDARD_SIDE_PRICE)} spread / total"
            )
        }
    }
}

@Composable
private fun ActionRow(
    isAnalyzing: Boolean,
    onAnalyzeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onAnalyzeClick,
            modifier = Modifier.weight(1f),
            enabled = !isAnalyzing
        ) {
            Text(if (isAnalyzing) "Analyzing..." else "Analyze")
        }

        OutlinedButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.weight(1f)
        ) {
            Text("Bet Later")
        }
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BestEdgeBanner(bestRecommendation = bestRecommendation)
        ModelSection(prediction = prediction)
        EdgeSection(
            moneylineRecommendation = moneylineRecommendation,
            spreadRecommendation = spreadRecommendation,
            totalRecommendation = totalRecommendation
        )
    }
}

@Composable
private fun BestEdgeBanner(bestRecommendation: RecommendationSummary?) {
    val containerColor = if (bestRecommendation?.edge ?: 0.0 > 0.0) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (bestRecommendation?.edge ?: 0.0 > 0.0) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Best Edge",
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
            Text(
                text = bestRecommendation?.label ?: "No Clear Edge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            Text(
                text = bestRecommendation?.detail ?: "The current market prices do not beat the model fair odds.",
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ModelSection(prediction: ModelPrediction) {
    SectionCard(
        title = "Model",
        subtitle = "Fair prices and expected scoring"
    ) {
        TwoColumnGrid {
            MetricTile(
                label = "${prediction.awayTeamName} ML",
                value = formatAmericanOdds(prediction.awayTeamModelMoneylineOdds)
            )
            MetricTile(
                label = "${prediction.homeTeamName} ML",
                value = formatAmericanOdds(prediction.homeTeamModelMoneylineOdds)
            )
            MetricTile(
                label = "${prediction.awayTeamName} Spread",
                value = formatAmericanOdds(prediction.awayTeamModelSpreadOdds)
            )
            MetricTile(
                label = "${prediction.homeTeamName} Spread",
                value = formatAmericanOdds(prediction.homeTeamModelSpreadOdds)
            )
            MetricTile(
                label = "Over",
                value = formatAmericanOdds(prediction.modelOverOdds)
            )
            MetricTile(
                label = "Under",
                value = formatAmericanOdds(prediction.modelUnderOdds)
            )
            MetricTile(
                label = "Expected Score",
                value = "${prediction.awayTeamName} ${formatDouble(prediction.awayTeamExpectedPoints)}"
            )
            MetricTile(
                label = "Expected Score",
                value = "${prediction.homeTeamName} ${formatDouble(prediction.homeTeamExpectedPoints)}"
            )
            MetricTile(
                label = "Sim Avg",
                value = "${prediction.awayTeamName} ${formatDouble(prediction.averageSimulatedAwayTeamScore)}"
            )
            MetricTile(
                label = "Sim Avg",
                value = "${prediction.homeTeamName} ${formatDouble(prediction.averageSimulatedHomeTeamScore)}"
            )
        }
    }
}

@Composable
private fun EdgeSection(
    moneylineRecommendation: RecommendationSummary,
    spreadRecommendation: RecommendationSummary,
    totalRecommendation: RecommendationSummary
) {
    SectionCard(
        title = "Edge / Recommendation",
        subtitle = "Where the market offer beats the model"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EdgeRow(
                market = "Moneyline",
                recommendation = moneylineRecommendation
            )
            EdgeRow(
                market = "Spread",
                recommendation = spreadRecommendation
            )
            EdgeRow(
                market = "Total",
                recommendation = totalRecommendation
            )
        }
    }
}

@Composable
private fun EdgeRow(
    market: String,
    recommendation: RecommendationSummary
) {
    val hasEdge = recommendation.edge > 0.0
    val containerColor = if (hasEdge) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = market,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                EdgeBadge(
                    text = if (hasEdge) formatPercent(recommendation.edge) else "No edge",
                    highlighted = hasEdge
                )
            }
            Text(
                text = recommendation.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = recommendation.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EdgeBadge(
    text: String,
    highlighted: Boolean
) {
    val backgroundColor = if (highlighted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val textColor = if (highlighted) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@Composable
private fun TwoColumnGrid(
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        content()
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
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
        detail = "Neither moneyline offer beats the model fair price.",
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
