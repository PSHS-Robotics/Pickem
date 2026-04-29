package com.example.pickem.user

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.math.abs

object UserRepository {

    private const val SAVED_USERS_FILE = "saved_users.json"
    private const val SEED_USERS_FILE = "defUser.json"
    private const val TAG = "UserRepository"

    private val gson = Gson()
    private val userListType = object : TypeToken<List<User>>() {}.type

    var currentUser by mutableStateOf<User?>(null)
    private var users: List<User> = emptyList()

    /*
    loads all users to a list from defUser.json
     */
    fun loadUsers(context: Context): List<User> {
        if (users.isNotEmpty()) {
            return users
        }

        val savedUsersFile = getSavedUsersFile(context)
        users = if (savedUsersFile.exists()) {
            loadUsersFromInternalStorage(savedUsersFile)
        } else {
            loadUsersFromSeedAssets(context).also {
                saveUsers(context, it)
            }
        }

        return users
    }

    /*
    sets current user to the input if the username and password matches and searches user list
     */
    fun login(context: Context, username: String, password: String): User? {
        val user = loadUsers(context).firstOrNull {
            it.username == username && it.password == password
        }

        currentUser = user
        return user
    }

    /*
    adds bet to bet history
     */
    fun addBet(context: Context, bet: BetHistory) {
        val user = currentUser ?: return

        user.history.add(bet)

        val updatedUser = if (bet.win) {
            user.copy(
                balance = user.balance + bet.amountWon,
                history = user.history
            )
        } else {
            user.copy(
                balance = user.balance - bet.amountBet,
                history = user.history
            )
        }

        currentUser = updatedUser
        users = loadUsers(context).map {
            if (it.userID == updatedUser.userID) updatedUser else it
        }
        saveUsers(context, users)
    }

    /*
    calculates potential profit based on the odds and the amount bet
     */
    fun calculatePotentialProfit(amountBet: Double, odds: Int): Double {
        return if (odds > 0) {
            amountBet * (odds / 100.0)
        } else {
            amountBet * (100.0 / abs(odds))
        }
    }

    /*
    returns list of users
     */
    private fun loadUsersFromInternalStorage(file: File): List<User> {
        return try {
            val json = file.readText()
            Log.d(TAG, "Loaded users from internal storage: ${file.absolutePath}")
            gson.fromJson<List<User>>(json, userListType) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load users from internal storage", e)
            emptyList()
        }
    }

    private fun loadUsersFromSeedAssets(context: Context): List<User> {
        return try {
            val json = context.assets.open(SEED_USERS_FILE)
                .bufferedReader()
                .use { it.readText() }

            Log.d(TAG, "Loaded users from seed assets")
            gson.fromJson<List<User>>(json, userListType) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load users from seed assets", e)
            emptyList()
        }
    }

    private fun saveUsers(context: Context, usersToSave: List<User>) {
        try {
            val file = getSavedUsersFile(context)
            file.writeText(gson.toJson(usersToSave, userListType))
            Log.d(TAG, "Saved users to internal storage: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save users to internal storage", e)
        }
    }

    private fun getSavedUsersFile(context: Context): File {
        return File(context.filesDir, SAVED_USERS_FILE)
    }
}
