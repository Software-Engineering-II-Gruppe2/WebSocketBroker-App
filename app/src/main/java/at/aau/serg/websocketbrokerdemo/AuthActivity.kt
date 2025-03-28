package at.aau.serg.websocketbrokerdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import at.aau.serg.websocketbrokerdemo.MainActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            // Wenn der Benutzer eingeloggt ist - forward auf MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // AuthActivity closen
        } else {
            // Wenn der Benutzer nicht eingeloggt ist - show login screen
            setContent {
                AuthNavigation()
            }
        }
    }
}

@Composable
fun AuthNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) "main" else "login"

    NavHost(navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
    }
}

@Composable
fun LoginScreen(navController: NavController, context: android.content.Context = LocalContext.current) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(onClick = {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    context.startActivity(Intent(context, MainActivity::class.java))
                    (context as? android.app.Activity)?.finish() // Close AuthActivity
                }
                .addOnFailureListener { errorMessage = it.message }
        }) {
            Text("Login")
        }

        TextButton(onClick = { navController.navigate("register") }) {
            Text("Don't have an account? Register")
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController, context: android.content.Context = LocalContext.current) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(onClick = {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    context.startActivity(Intent(context, MainActivity::class.java))
                    (context as? android.app.Activity)?.finish() // Close AuthActivity
                }
                .addOnFailureListener { errorMessage = it.message }
        }) {
            Text("Register")
        }
        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login")
        }
    }
}
