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

class GamesViewModel(
    private val gameRepository: GameRepository = GameRepository(),
    private val predictionRepository: PredictionRepository = PredictionRepository()
) : ViewModel() {

    var games by mutableStateOf<List<Game>>(emptyList())
        private set

    val predictionsByGameId: SnapshotStateMap<String, ModelPrediction> = mutableStateMapOf()
    val analyzingGameIds: SnapshotStateMap<String, Boolean> = mutableStateMapOf()

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
