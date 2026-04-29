package com.example.pickem.login

import android.content.Context
import com.example.pickem.user.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun login(context: Context, username: String, password: String): User? {
    val users = loadUsersFromAssets(context)

    for (user in users) {
        if (user.username == username && user.password == password) {
            return user
        }
    }

    return null
}

fun loadUsersFromAssets(context: Context): List<User> {
    return try {
        val json = context.assets.open("defUser.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<User>>() {}.type
        Gson().fromJson(json, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}