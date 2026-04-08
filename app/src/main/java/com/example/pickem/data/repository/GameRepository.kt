/*
 * This repository gives the rest of the app a simple way to request seeded games.
 */
package com.example.pickem.data.repository

import android.content.Context
import com.example.pickem.data.local.asset.GameAssetDataSource
import com.example.pickem.data.model.Game

class GameRepository(
    private val gameAssetDataSource: GameAssetDataSource = GameAssetDataSource()
) {

    /**
     * Returns the list of pre-made games stored in the app assets.
     */
    fun getGames(context: Context): List<Game> {
        return gameAssetDataSource.loadGames(context)
    }
}
