package com.example.pickem.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
diplays settings
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    user: User?
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            Button(onClick = onBackClick) {
                Text("Back")
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Username: ${user?.username}",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Password: ${user?.password}",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        text = "Balance: $${"%.2f".format(user?.balance ?: 0.0)}",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Add Balance")
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onLoginClick) {
                Text("Logout")
            }
        }
    }

    if (showDialog) {
        AddBalanceSection(
            onDismiss = { showDialog = false },
            onAddBalance = { amount ->
                user?.balance = (user?.balance ?: 0.0) + amount

            }
        )
    }
}

/*
gives popup which allows user to enter a number, adding it to the balance for that user
 */
@Composable
fun AddBalanceSection(
    onDismiss: () -> Unit,
    onAddBalance: (Double) -> Unit
) {
    var inputAmount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Balance") },
        text = {
            Column {
                Text("Enter amount:")

                TextField(
                    value = inputAmount,
                    onValueChange = { inputAmount = it },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = inputAmount.toDoubleOrNull()

                    if (amount != null && amount > 0) {
                        onAddBalance(amount)
                        onDismiss()
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}