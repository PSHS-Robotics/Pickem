package com.example.pickem.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
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
    onSettingsClick: () -> Unit,
    onGamesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    UserDropdown(onSettingsClick, onHistoryClick, onProfileClick)
    val games = remember { mutableListOf("game1", "game2") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Screen")

        Button(
            onClick = onGamesClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Games")
        }

        UserDropdown(
            onSettingsClick = onSettingsClick,
            onHistoryClick = onHistoryClick,
            onProfileClick = onProfileClick
        )

        Column(modifier = Modifier.padding(top = 16.dp)) {
            games.forEach { game ->
                Text(text = game)
            }
        }
    }
}

@Composable
fun UserDropdown(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(top = 32.dp)) {
        Button(onClick = { expanded = true }) {
            Text("Menu")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Profile") },
                onClick = {
                    onProfileClick()
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("History") },
                onClick = {
                    onHistoryClick()
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = {
                    onSettingsClick()
                    expanded = false
                }
            )
        }
    }
}