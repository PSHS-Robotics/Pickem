package com.example.pickem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.pickem.games.GamesScreen
import com.example.pickem.home.HomeScreen
import com.example.pickem.login.LoginScreen
import com.example.pickem.user.HistoryScreen
import com.example.pickem.user.ProfileScreen
import com.example.pickem.user.SettingsScreen
import com.example.pickem.user.User

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
    var currentUser by remember { mutableStateOf<User?>(null) }

    val filePath = "app/src/main/assets/defUser.json"

    when (currentScreen) {
        "home" -> HomeScreen(
            onSettingsClick = {
                currentScreen = "settings"
            },
            onGamesClick = {
                currentScreen = "games"
            },
            onHistoryClick = {
                currentScreen = "history"
            },
            onProfileClick = {
                currentScreen = "profile"
            }
        )

        "settings" -> SettingsScreen(
            onBackClick = {
                currentScreen = "home"
            },
            onLoginClick = {
                currentScreen = "login"
            }
        )

        "login" -> LoginScreen(
            filePath = filePath,
            onLoginSuccess = { user ->
                currentUser = user
                currentScreen = "home"
            }
        )

        "games" -> GamesScreen (
            onBackClick = {
                currentScreen = "home"
            }
        )

        "history" -> HistoryScreen (
            onBackClick = {
                currentScreen = "home"
            }
        )

        "profile" -> ProfileScreen (
            onBackClick = {
                currentScreen = "home"
            }
        )
    }
}