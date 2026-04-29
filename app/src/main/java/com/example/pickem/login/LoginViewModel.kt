package com.example.pickem.login

import android.content.Context
import com.example.pickem.user.User
import com.example.pickem.user.UserRepository

/*
runs login in userRepository wh
 */
fun login(context: Context, username: String, password: String): User? {
    return UserRepository.login(context, username, password)
}
