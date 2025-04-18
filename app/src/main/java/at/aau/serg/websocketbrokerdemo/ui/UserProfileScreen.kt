package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.data.PlayerProfile

@Composable
fun UserProfileScreen(
    playerProfile: PlayerProfile?,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit //zurück zur Main
) {
    var newName by remember { mutableStateOf(playerProfile?.name ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (playerProfile != null) {
            Text("Name: ${playerProfile.name}")
            Text("Level: ${playerProfile.level}")
            Text("Games Played: ${playerProfile.gamesPlayed}")
            Text("Wins: ${playerProfile.wins}")
            Text("Most Money: ${playerProfile.mostMoney}")

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Neuer Name") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { onNameChange(newName) }) {
                Text("Namen ändern")
            }
        } else {
            Text("Profil wird geladen...")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack) { // Zurück-Button
            Text("Zurück")
        }
    }
}