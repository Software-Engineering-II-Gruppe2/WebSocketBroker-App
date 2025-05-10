package at.aau.serg.websocketbrokerdemo.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import at.aau.serg.websocketbrokerdemo.MainActivity
import at.aau.serg.websocketbrokerdemo.data.FirestoreManager
import at.aau.serg.websocketbrokerdemo.data.PlayerProfile
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AuthManager {

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
                                else -> "Login failed:Unknown error."
                            }
                        }
                        is FirebaseException -> {
                            "An error occurred. Please check your credentials."
                        }
                        else -> {
                            "Unknown error: Unknown error."
                        }
                    }
                    setErrorMessage(errorMessage)
                }
            }
    }

    internal fun registerUser(auth: FirebaseAuth, email: String, password: String, context: Context, setErrorMessage: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    val profile = PlayerProfile(name = "user${userId.takeLast(4)}")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            FirestoreManager.saveUserProfile(userId, profile)
                            FirestoreManager.initializeUserStats(userId)
                            (context as? Activity)?.runOnUiThread {
                                context.startActivity(Intent(context, MainActivity::class.java))
                                context.finish()
                            }
                        } catch (e: Exception) {
                            Log.e("RegisterScreen", "Error saving profile")
                            (context as? Activity)?.runOnUiThread {
                                setErrorMessage("Failed to save profile.")
                            }
                        }
                    }
                } else {
                    Log.e("RegisterScreen", "Failed to get user ID.")
                    (context as? Activity)?.runOnUiThread {
                        setErrorMessage("Failed to get user ID.")
                    }
                }
            }
            .addOnFailureListener { error ->
                // Fehlerbehandlung
                (context as? Activity)?.runOnUiThread {
                    val errorMessage = when (error) {
                        is FirebaseAuthException -> {
                            // Fehlerbehandlung basierend auf dem ErrorCode
                            when (error.errorCode) {
                                "ERROR_WEAK_PASSWORD" -> "The password is too weak. Please choose a stronger password."
                                "ERROR_INVALID_EMAIL" -> "The email address is invalid."
                                "ERROR_EMAIL_ALREADY_IN_USE" -> "The email address is already in use. Try another one."
                                else -> "Registration failed: Unknown error."
                            }
                        }
                        is FirebaseException -> {
                            // Allgemeiner FirebaseException Fehler
                            "An error occurred. Please try again later."
                        }
                        else -> {
                            // Fallback, falls der Fehler ein unerwarteter Typ ist
                            "Unknown error: Unknown error."
                        }
                    }

                    // Den Fehlerstatus setzen, um ihn im UI anzuzeigen
                    setErrorMessage(errorMessage)
                }
            }
    }

}
