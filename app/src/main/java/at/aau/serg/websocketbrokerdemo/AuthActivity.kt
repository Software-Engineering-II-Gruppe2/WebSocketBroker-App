package at.aau.serg.websocketbrokerdemo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import at.aau.serg.websocketbrokerdemo.ui.LoginScreen
import at.aau.serg.websocketbrokerdemo.ui.RegisterScreen
import androidx.navigation.compose.rememberNavController


class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            try {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorMessage("An error occurred while navigating to MainActivity.")
            }
        } else {
            setContent {
                var showStartScreen by remember { mutableStateOf(true) }

                if (showStartScreen) {
                    StartScreen(onEnterClick = { showStartScreen = false })
                } else {
                    AuthNavigation()
                }
            }
        }
    }

    private fun showErrorMessage(message: String) {
        println("Error: $message")
    }
}

@Composable
fun AuthNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
    }
}
