/*
 * This ViewModel is the place for login-screen state and login-screen logic once login becomes more real.
 * It can later hold username input, validation state, and local sign-in flow without putting that logic in the screen file.
 */
package com.example.pickem.login

import androidx.lifecycle.ViewModel
import com.example.pickem.MainActivity
import com.example.pickem.user.User
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File


class LoginViewModel : ViewModel()

fun login(filePath: String, username: String, password: String): User? {
    val users = loadUsers(filePath)

    for (user in users) {
        if (user.username == username && user.password == password) {
            return user
        }
    }

    return null
}

fun loadUsers(filePath: String): List<User> {
    val json = File(filePath).readText()
    val type = object : TypeToken<List<User>>() {}.type
    return Gson().fromJson(json, type)
}

fun saveUsers(filePath: String, users: List<User>) {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val json = gson.toJson(users)
    File(filePath).writeText(json)
}

fun addUser(filePath: String, username: String, password: String, userID: Int): Boolean {
    val users = loadUsers(filePath).toMutableList()

    // Prevent duplicate usernames
    if (users.any { it.username == username }) {
        println("User already exists.")
        return false
    }

    val newUser = User(username, password, userID)
    users.add(newUser)

    saveUsers(filePath, users)
    println("User added successfully!")
    return true
}

