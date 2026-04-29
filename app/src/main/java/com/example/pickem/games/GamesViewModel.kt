/*
 * This ViewModel is the place for game-screen state and game-screen logic as the Games area grows.
 * It can later hold loaded games, filtering state, and other screen-specific behavior without cluttering the composable.
 */
package com.example.pickem.games

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import com.example.pickem.data.model.DeterministicPredictionResult
import com.example.pickem.data.model.Game
import com.example.pickem.data.model.ModelPrediction
import com.example.pickem.data.repository.GameRepository
import com.example.pickem.data.repository.PredictionRepository
import com.example.pickem.user.BetHistory
import com.example.pickem.user.UserRepository
import kotlin.random.Random

class GamesViewModel(
    private val gameRepository: GameRepository = GameRepository(),
    private val predictionRepository: PredictionRepository = PredictionRepository()
) : ViewModel() {

    var games by mutableStateOf<List<Game>>(emptyList())
        private set

    val predictionsByGameId: SnapshotStateMap<String, ModelPrediction> = mutableStateMapOf()
    val analyzingGameIds: SnapshotStateMap<String, Boolean> = mutableStateMapOf()

    var selectedBet by mutableStateOf<SelectedBet?>(null)
        private set

    var wagerAmount by mutableStateOf("")
        private set

    var isConfirmingBet by mutableStateOf(false)
        private set

    var validationError by mutableStateOf<String?>(null)
        private set

    var resultMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Loads the seeded games once and logs any team names that do not exist in the team stats asset.
     */
    fun loadGames(context: Context) {
        if (games.isNotEmpty()) {
            return
        }

        games = gameRepository.getGames(context)

        val missingTeams = gameRepository.findMissingTeamStats(context)
        if (missingTeams.isNotEmpty()) {
            Log.w("GamesViewModel", "Missing team stats for: ${missingTeams.joinToString()}")
        }
    }

    /**
     * Runs prediction for one specific game and stores the result under that game's id.
     */
    fun analyzeGame(context: Context, game: Game) {
        if (analyzingGameIds[game.id] == true) {
            return
        }

        analyzingGameIds[game.id] = true

        val predictionResult = predictionRepository.predictGame(context, game)
        predictionsByGameId[game.id] = predictionResult.modelPrediction
        analyzingGameIds[game.id] = false

        val summary = buildDeterministicPredictionSummary(predictionResult)
        Log.d("GamesViewModel", summary)
    }

    /**
     * Returns the stored prediction result for one game if that game has already been analyzed.
     */
    fun getPredictionForGame(gameId: String): ModelPrediction? {
        return predictionsByGameId[gameId]
    }

    /**
     * Returns true when a game is currently being analyzed.
     */
    fun isAnalyzingGame(gameId: String): Boolean {
        return analyzingGameIds[gameId] == true
    }

    fun startBet(context: Context, game: Game, betOption: BetOption) {
        val prediction = predictionsByGameId[game.id] ?: analyzeGameForBet(context, game)

        selectedBet = buildSelectedBet(game, prediction, betOption)
        wagerAmount = ""
        isConfirmingBet = false
        validationError = null
        resultMessage = null
    }

    fun updateWagerAmount(value: String) {
        wagerAmount = value
        validationError = null
    }

    fun moveToConfirmation() {
        if (validateWager() != null) {
            isConfirmingBet = true
        }
    }

    fun returnToAmountEntry() {
        isConfirmingBet = false
        validationError = null
    }

    fun dismissBetDialog() {
        selectedBet = null
        wagerAmount = ""
        isConfirmingBet = false
        validationError = null
    }

    fun placeSelectedBet(context: Context) {
        val bet = selectedBet ?: return
        val amountBet = validateWager() ?: return
        val potentialProfit = UserRepository.calculatePotentialProfit(amountBet, bet.odds)
        val win = Random.nextDouble(0.0, 1.0) <= bet.winProbability
        val amountWon = if (win) potentialProfit else 0.0

        UserRepository.addBet(
            context = context,
            bet = BetHistory(
                gameID = bet.gameId,
                awayTeam = bet.awayTeam,
                homeTeam = bet.homeTeam,
                betType = bet.betType,
                selectedSide = bet.selectedSide,
                line = bet.line,
                odds = bet.odds,
                amountBet = amountBet,
                potentialProfit = potentialProfit,
                win = win,
                amountWon = amountWon
            )
        )

        resultMessage = if (win) "Bet won!" else "Bet lost."
        dismissBetDialog()
    }

    fun potentialProfitForCurrentWager(): Double? {
        val bet = selectedBet ?: return null
        val amountBet = wagerAmount.toDoubleOrNull() ?: return null
        if (amountBet <= 0.0) {
            return null
        }

        return UserRepository.calculatePotentialProfit(amountBet, bet.odds)
    }

    private fun validateWager(): Double? {
        val user = UserRepository.currentUser
        val amountBet = wagerAmount.toDoubleOrNull()

        validationError = when {
            user == null -> "Log in before placing a bet."
            amountBet == null || amountBet <= 0.0 -> "Enter a valid wager amount."
            amountBet > user.balance -> "Wager cannot be greater than current balance."
            else -> null
        }

        return if (validationError == null) amountBet else null
    }

    private fun analyzeGameForBet(context: Context, game: Game): ModelPrediction {
        val predictionResult = predictionRepository.predictGame(context, game)
        predictionsByGameId[game.id] = predictionResult.modelPrediction

        val summary = buildDeterministicPredictionSummary(predictionResult)
        Log.d("GamesViewModel", summary)

        return predictionResult.modelPrediction
    }

    private fun buildSelectedBet(
        game: Game,
        prediction: ModelPrediction,
        betOption: BetOption
    ): SelectedBet {
        val awaySpread = -game.sportsbookLine.homeSpread

        return when (betOption) {
            BetOption.AwayMoneyline -> SelectedBet(
                gameId = game.id,
                awayTeam = game.awayTeam,
                homeTeam = game.homeTeam,
                betType = "Moneyline",
                selectedSide = game.awayTeam,
                line = null,
                odds = game.sportsbookLine.awayMoneyline,
                winProbability = prediction.awayTeamWinProbability
            )
            BetOption.HomeMoneyline -> SelectedBet(
                gameId = game.id,
                awayTeam = game.awayTeam,
                homeTeam = game.homeTeam,
                betType = "Moneyline",
                selectedSide = game.homeTeam,
                line = null,
                odds = game.sportsbookLine.homeMoneyline,
                winProbability = prediction.homeTeamWinProbability
            )
            BetOption.AwaySpread -> SelectedBet(
                gameId = game.id,
                awayTeam = game.awayTeam,
                homeTeam = game.homeTeam,
                betType = "Spread",
                selectedSide = "${game.awayTeam} ${formatSignedLine(awaySpread)}",
                line = awaySpread,
                odds = STANDARD_SIDE_PRICE,
                winProbability = prediction.awayTeamSpreadCoverProbability
            )
            BetOption.HomeSpread -> SelectedBet(
                gameId = game.id,
                awayTeam = game.awayTeam,
                homeTeam = game.homeTeam,
                betType = "Spread",
                selectedSide = "${game.homeTeam} ${formatSignedLine(game.sportsbookLine.homeSpread)}",
                line = game.sportsbookLine.homeSpread,
                odds = STANDARD_SIDE_PRICE,
                winProbability = prediction.homeTeamSpreadCoverProbability
            )
            BetOption.Over -> SelectedBet(
                gameId = game.id,
                awayTeam = game.awayTeam,
                homeTeam = game.homeTeam,
                betType = "Total",
                selectedSide = "Over ${game.sportsbookLine.overUnder}",
                line = game.sportsbookLine.overUnder,
                odds = STANDARD_SIDE_PRICE,
                winProbability = prediction.overProbability
            )
            BetOption.Under -> SelectedBet(
                gameId = game.id,
                awayTeam = game.awayTeam,
                homeTeam = game.homeTeam,
                betType = "Total",
                selectedSide = "Under ${game.sportsbookLine.overUnder}",
                line = game.sportsbookLine.overUnder,
                odds = STANDARD_SIDE_PRICE,
                winProbability = prediction.underProbability
            )
        }
    }

    private fun formatSignedLine(value: Double): String {
        return if (value > 0) "+$value" else value.toString()
    }

    /**
     * Builds a readable debug summary of the full prediction output for one matchup.
     */
    private fun buildDeterministicPredictionSummary(
        predictionResult: DeterministicPredictionResult
    ): String {
        return """
            Full prediction for ${predictionResult.modelPrediction.awayTeamName} at ${predictionResult.modelPrediction.homeTeamName}
            League averages:
            2PT made=${predictionResult.leagueAverages.leagueAverageTwoPointMakesPerGame}, 3PT made=${predictionResult.leagueAverages.leagueAverageThreePointMakesPerGame}, FT made=${predictionResult.leagueAverages.leagueAverageFreeThrowsMadePerGame}
            2PT allowed=${predictionResult.leagueAverages.leagueAverageTwoPointMakesAllowedPerGame}, 3PT allowed=${predictionResult.leagueAverages.leagueAverageThreePointMakesAllowedPerGame}, FT allowed=${predictionResult.leagueAverages.leagueAverageFreeThrowsMadeAllowedPerGame}
            Away ratings:
            2PT attack=${predictionResult.awayTeamRatings.teamTwoPointAttackRating}, 3PT attack=${predictionResult.awayTeamRatings.teamThreePointAttackRating}, FT attack=${predictionResult.awayTeamRatings.teamFreeThrowAttackRating}
            2PT defense=${predictionResult.awayTeamRatings.teamTwoPointDefenseRating}, 3PT defense=${predictionResult.awayTeamRatings.teamThreePointDefenseRating}, FT defense=${predictionResult.awayTeamRatings.teamFreeThrowDefenseRating}
            Home ratings:
            2PT attack=${predictionResult.homeTeamRatings.teamTwoPointAttackRating}, 3PT attack=${predictionResult.homeTeamRatings.teamThreePointAttackRating}, FT attack=${predictionResult.homeTeamRatings.teamFreeThrowAttackRating}
            2PT defense=${predictionResult.homeTeamRatings.teamTwoPointDefenseRating}, 3PT defense=${predictionResult.homeTeamRatings.teamThreePointDefenseRating}, FT defense=${predictionResult.homeTeamRatings.teamFreeThrowDefenseRating}
            Away expected makes:
            2PT=${predictionResult.modelPrediction.awayTeamExpectedTwoPointMakes}, 3PT=${predictionResult.modelPrediction.awayTeamExpectedThreePointMakes}, FT=${predictionResult.modelPrediction.awayTeamExpectedFreeThrowsMade}
            Home expected makes:
            2PT=${predictionResult.modelPrediction.homeTeamExpectedTwoPointMakes}, 3PT=${predictionResult.modelPrediction.homeTeamExpectedThreePointMakes}, FT=${predictionResult.modelPrediction.homeTeamExpectedFreeThrowsMade}
            Expected points:
            away=${predictionResult.modelPrediction.awayTeamExpectedPoints}, home=${predictionResult.modelPrediction.homeTeamExpectedPoints}
            Average simulated score:
            away=${predictionResult.modelPrediction.averageSimulatedAwayTeamScore}, home=${predictionResult.modelPrediction.averageSimulatedHomeTeamScore}
            Win probabilities:
            away=${predictionResult.modelPrediction.awayTeamWinProbability}, home=${predictionResult.modelPrediction.homeTeamWinProbability}
            Model moneyline odds:
            away=${predictionResult.modelPrediction.awayTeamModelMoneylineOdds}, home=${predictionResult.modelPrediction.homeTeamModelMoneylineOdds}
            Spread cover probabilities:
            away=${predictionResult.modelPrediction.awayTeamSpreadCoverProbability}, home=${predictionResult.modelPrediction.homeTeamSpreadCoverProbability}
            Model spread odds:
            away=${predictionResult.modelPrediction.awayTeamModelSpreadOdds}, home=${predictionResult.modelPrediction.homeTeamModelSpreadOdds}
            Total probabilities:
            over=${predictionResult.modelPrediction.overProbability}, under=${predictionResult.modelPrediction.underProbability}
            Total odds:
            over=${predictionResult.modelPrediction.modelOverOdds}, under=${predictionResult.modelPrediction.modelUnderOdds}
        """.trimIndent()
    }
}

private const val STANDARD_SIDE_PRICE = -110

enum class BetOption {
    AwayMoneyline,
    HomeMoneyline,
    AwaySpread,
    HomeSpread,
    Over,
    Under
}

data class SelectedBet(
    val gameId: String,
    val awayTeam: String,
    val homeTeam: String,
    val betType: String,
    val selectedSide: String,
    val line: Double?,
    val odds: Int,
    val winProbability: Double
)
