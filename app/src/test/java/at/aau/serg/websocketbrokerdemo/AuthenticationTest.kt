package at.aau.serg.websocketbrokerdemo

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import at.aau.serg.websocketbrokerdemo.ui.LoginScreen
import at.aau.serg.websocketbrokerdemo.ui.RegisterScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowApplication
import at.aau.serg.websocketbrokerdemo.ui.loginUser
import at.aau.serg.websocketbrokerdemo.ui.registerUser

@RunWith(RobolectricTestRunner::class)
class AuthActivityTest {

    @Before
    fun setup() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun authActivity_currentUserNotNull_startsMainActivity() {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword("test@example.com", "password").addOnCompleteListener {
            if (it.isSuccessful) {
                val scenario = ActivityScenario.launch(AuthActivity::class.java)
                val shadowApp = ShadowApplication.getInstance()
                val startedIntent = shadowApp.nextStartedActivity
                assertEquals(MainActivity::class.java.name, startedIntent?.component?.className)
                scenario.close()
                auth.signOut()
            }
        }.addOnFailureListener {
            throw it
        }
    }

    @Test
    fun authActivity_currentUserNull_showsStartScreen() {
        val scenario = ActivityScenario.launch(AuthActivity::class.java)
        //Verify that Auth Activity started without any error.
        scenario.close()
    }
}

@RunWith(RobolectricTestRunner::class)
class LoginScreenTest {

    private lateinit var context: Context
    private lateinit var auth: FirebaseAuth

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        auth = mockk(relaxed = true)
    }

    @Test
    fun loginUser_success_startsMainActivity() {
        val email = "test@example.com"
        val password = "password"
        val intent = Intent(context, MainActivity::class.java)
        every { auth.signInWithEmailAndPassword(email, password) } answers {
            val task: com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> = mockk(relaxed = true)
            every { task.addOnSuccessListener(any()) } answers {
                (it.invocation.args[0] as (com.google.firebase.auth.AuthResult) -> Unit).invoke(mockk())
                task
            }
            every { task.addOnFailureListener(any()) } answers { task }
            task
        }

        loginUser(auth, email, password, context, {})

        verify { context.startActivity(intent) }
    }

    @Test
    fun loginUser_failure_setsErrorMessage() {
        val email = "test@example.com"
        val password = "password"
        val errorMessageState = mutableStateOf<String?>(null)
        val setErrorMessage: (String) -> Unit = { errorMessageState.value = it }
        val exception = mockk<com.google.firebase.auth.FirebaseAuthException>(relaxed = true)
        every { exception.errorCode } returns "ERROR_USER_NOT_FOUND"
        every { auth.signInWithEmailAndPassword(email, password) } answers {
            val task: com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> = mockk(relaxed = true)
            every { task.addOnSuccessListener(any()) } answers { task }
            every { task.addOnFailureListener(any()) } answers {
                (it.invocation.args[0] as (Throwable) -> Unit).invoke(exception)
                task
            }
            task
        }

        loginUser(auth, email, password, context, setErrorMessage)

        assertEquals("No account found with this email. Please register first.", errorMessageState.value)
    }

    @Composable
    @Test
    fun loginScreen_navigatesToRegister() {
        val navController = mockk<NavController>(relaxed = true)
        LoginScreen(navController)
        navController.navigate("register")
        verify { navController.navigate("register") }
    }
}

@RunWith(RobolectricTestRunner::class)
class RegisterScreenTest {

    private lateinit var context: Context
    private lateinit var auth: FirebaseAuth

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        auth = mockk(relaxed = true)
    }

    @Test
    fun registerUser_success_startsMainActivity() {
        val email = "test@example.com"
        val password = "password"
        val intent = Intent(context, MainActivity::class.java)
        every { auth.createUserWithEmailAndPassword(email, password) } answers {
            val task: com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> = mockk(relaxed = true)
            every { task.addOnSuccessListener(any()) } answers {
                (it.invocation.args[0] as (com.google.firebase.auth.AuthResult) -> Unit).invoke(mockk())
                task
            }
            every { task.addOnFailureListener(any()) } answers { task }
            task
        }

        registerUser(auth, email, password, context, {})

        verify { context.startActivity(intent) }
    }

    @Test
    fun registerUser_failure_setsErrorMessage() {
        val email = "test@example.com"
        val password = "password"
        val errorMessageState = mutableStateOf<String?>(null)
        val setErrorMessage: (String) -> Unit = { errorMessageState.value = it }
        val exception = mockk<com.google.firebase.auth.FirebaseAuthException>(relaxed = true)
        every { exception.errorCode } returns "ERROR_EMAIL_ALREADY_IN_USE"
        every { auth.createUserWithEmailAndPassword(email, password) } answers {
            val task: com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> = mockk(relaxed = true)
            every { task.addOnSuccessListener(any()) } answers { task }
            every { task.addOnFailureListener(any()) } answers {
                (it.invocation.args[0] as (Throwable) -> Unit).invoke(exception)
                task
            }
            task
        }

        registerUser(auth, email, password, context, setErrorMessage)

        assertEquals("The email address is already in use. Try another one.", errorMessageState.value)
    }

    @Composable
    @Test
    fun registerScreen_navigatesToLogin() {
        val navController = mockk<NavController>(relaxed = true)
        RegisterScreen(navController)
        navController.navigate("login")
        verify { navController.navigate("login") }
    }
}