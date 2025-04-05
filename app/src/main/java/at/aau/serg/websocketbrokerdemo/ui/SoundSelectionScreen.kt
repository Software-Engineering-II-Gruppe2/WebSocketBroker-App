package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SoundSelectionScreen() {
    val sounds = listOf("Classic", "Chime", "Beep", "Nature")
    var selectedSound by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Wähle deinen Sound:")
        sounds.forEach { sound ->
            Button(onClick = {
                selectedSound = sound
                    message = "✅ Sound gespeichert: $sound"

            }, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(sound)
            }
        }
        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
    }
}
