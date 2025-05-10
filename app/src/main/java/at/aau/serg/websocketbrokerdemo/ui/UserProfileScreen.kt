package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
            Text("Most Money: ${playerProfile.highestMoney}")

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
