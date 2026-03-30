package com.example.pickem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.pickem.home.HomeScreen
import com.example.pickem.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("login") }

    when (currentScreen) {
        "home" -> HomeScreen(
            onLoginClick = {
                currentScreen = "login"
            }
        )

        "login" -> LoginScreen(
            onBackClick = {
                currentScreen = "home"
            }
        )
    }
}