package com.example.pickem.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onLoginClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onGamesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Screen")

        Button(
            onClick = onLoginClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "logout")
        }

        Button(
            onClick = onSettingsClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "settings")
        }

        Button(
            onClick = onGamesClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "games")
        }
    }
}

