package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun SettingsScreen (){
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var brightness by remember { mutableStateOf(0.5f) }
    var showSavedMessage by remember { mutableStateOf(false) }
    if (user == null) {

        Text("Bitte melden Sie sich an, um die Einstellungen zu ändern.", color = MaterialTheme.colorScheme.error)
    } else {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Helligkeitseinstellung", style = MaterialTheme.typography.headlineSmall)


            Slider(
                value = brightness,
                onValueChange = { newBrightness -> brightness = newBrightness },
                valueRange = 0f..1f,
                steps = 5, // Optionale vordefinierte Stufen
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Text("Helligkeit: ${(brightness * 100).toInt()}%", style = MaterialTheme.typography.bodyLarge)


            Button(onClick = {

                saveBrightnessSetting(brightness)
                showSavedMessage = true
            }) {
                Text("Speichern")
            }
        }
    }
}

fun saveBrightnessSetting(brightness: Float) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser


    if (user != null) {
        val db = FirebaseFirestore.getInstance()


        val userSettings = hashMapOf(
            "brightness" to brightness
        )


        db.collection("users")
            .document(user.uid)
            .set(userSettings, SetOptions.merge())
            .addOnSuccessListener {

                println("Helligkeitseinstellung wurde gespeichert")
            }
            .addOnFailureListener { e ->

                println("Fehler beim Speichern der Helligkeit: $e")
            }
    } else {
        println("Benutzer ist nicht angemeldet")
    }
}

