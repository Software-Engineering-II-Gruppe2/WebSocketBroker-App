package at.aau.serg.websocketbrokerdemo.ui

import android.content.Context
import android.content.Intent
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import at.aau.serg.websocketbrokerdemo.MainActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

    // Funktion zum Setzen der Fehlermeldung
    val setErrorMessage: (String) -> Unit = { message ->
        errorMessage = message
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation() // Passwort im Maskierungsmodus
        )

        // Fehlermeldung anzeigen, wenn gesetzt
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(onClick = { loginUser(auth, email, password, context, setErrorMessage) }) {
            Text("Login")
        }

        TextButton(onClick = { navController.navigate("register") }) {
            Text("Don't have an account? Register")
        }
    }
}



internal fun loginUser(auth: FirebaseAuth, email: String, password: String, context: Context, setErrorMessage: (String) -> Unit) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnSuccessListener {
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as? Activity)?.finish()
        }
        .addOnFailureListener { error ->
            (context as? Activity)?.runOnUiThread {
                val errorMessage = when (error) {
                    is FirebaseAuthException -> {
                        when (error.errorCode) {
                            "ERROR_WRONG_PASSWORD" -> "The password is incorrect. Please try again."
                            "ERROR_INVALID_EMAIL" -> "The email address is invalid. Please check and try again."
                            "ERROR_USER_NOT_FOUND" -> "No account found with this email. Please register first."
                            else -> "Login failed: ${error.message ?: "Unknown error."}"
                        }
                    }
                    is FirebaseException -> {
                        "An error occurred. Please check your credentials."
                    }
                    else -> {
                        "Unknown error: ${error.message ?: "Unknown error."}"
                    }
                }
                setErrorMessage(errorMessage)
            }
        }
}
