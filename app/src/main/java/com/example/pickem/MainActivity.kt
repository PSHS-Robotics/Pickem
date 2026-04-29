package com.example.pickem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.pickem.games.GamesScreen
import com.example.pickem.home.HomeScreen
import com.example.pickem.login.LoginScreen
import com.example.pickem.user.HistoryScreen
import com.example.pickem.user.ProfileScreen
import com.example.pickem.user.SettingsScreen
import com.example.pickem.user.User
import com.example.pickem.user.UserRepository

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
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf("login") }
    var currentUser by remember { mutableStateOf<User?>(null) }

    when (currentScreen) {
        "home" -> HomeScreen(
            onSettingsClick = { currentScreen = "settings" },
            onGamesClick = { currentScreen = "games" },
            onHistoryClick = { currentScreen = "history" },
            onProfileClick = { currentScreen = "profile" }
        )

        "settings" -> SettingsScreen(
            user = UserRepository.currentUser,
            onBackClick = { currentScreen = "home" },
            onLoginClick = {
                currentUser = null
                UserRepository.currentUser = null
                currentScreen = "login"
            }
        )

        "login" -> LoginScreen(
            context = context,
            onLoginSuccess = { user ->
                currentUser = user
                UserRepository.currentUser = user
                currentScreen = "home"
            }
        )

        "games" -> GamesScreen(
            onBackClick = { currentScreen = "home" }
        )

        "history" -> HistoryScreen(
            user = UserRepository.currentUser,
            onBackClick = { currentScreen = "home" }

        )

        "profile" -> ProfileScreen(
            onBackClick = { currentScreen = "home" }
        )
    }
}
