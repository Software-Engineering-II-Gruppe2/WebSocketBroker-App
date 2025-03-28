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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import at.aau.serg.websocketbrokerdemo.MainActivity

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(onClick = { loginUser(auth, email, password, context) }) {
            Text("Login")
        }

        TextButton(onClick = { navController.navigate("register") }) {
            Text("Don't have an account? Register")
        }
    }
}

private fun loginUser(auth: FirebaseAuth, email: String, password: String, context: Context) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnSuccessListener {
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as? Activity)?.finish()
        }
        .addOnFailureListener { error ->
            (context as? Activity)?.runOnUiThread {
                error.message?.let { println("Login failed: $it") }
            }
        }
}
