package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
