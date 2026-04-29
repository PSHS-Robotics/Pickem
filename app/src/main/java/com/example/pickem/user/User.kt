package com.example.pickem.user

data class User(
    val username: String,
    val password: String,
    val userID: String,
    var balance: Double = 0.0,
    val history: MutableList<BetHistory> = mutableListOf()
)