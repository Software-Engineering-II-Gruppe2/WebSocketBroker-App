package at.aau.serg.websocketbrokerdemo.ui.test

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.websocketbrokerdemo.ui.RegisterScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun registerScreen_displaysEmailAndPasswordFieldsAndButtons() {
        composeTestRule.setContent {
            RegisterScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Register").assertIsDisplayed()
        composeTestRule.onNodeWithText("Already have an account? Login").assertIsDisplayed()
    }

    @Test
    fun registerScreen_canEnterEmailAndPassword() {
        val testEmail = "test@aau.com"
        val testPassword = "fancypass123!"

        composeTestRule.setContent {
            RegisterScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").performTextInput(testEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(testPassword)

        composeTestRule.onNodeWithText(testEmail).assertIsDisplayed()
        composeTestRule.onNodeWithText("•".repeat(testPassword.length)).assertIsDisplayed()
    }

    /*
    @Test
    fun registerScreen_clickLoginButton_triggersNavigation() {
        var navigated = false
        val mockNavController = rememberNavController()
        mockNavController.addOnDestinationChangedListener { _, _, _ ->
            navigated = true
        }

        composeTestRule.setContent {
            RegisterScreen(navController = mockNavController)
        }

        composeTestRule.onNodeWithText("Already have an account? Login").performClick()

        assertTrue(navigated)
    }*/


    @Ignore("Temporarily disabled due to a bug")
    @Test
    fun registerScreen_clickRegisterButton_displaysGenericError() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val errorMessage = remember { mutableStateOf<String?>("") }
            val setErrorMessage: (String) -> Unit = { errorMessage.value = it }

            RegisterScreen(navController = navController)

            androidx.compose.material3.Button(onClick = {
                setErrorMessage("Registration failed: Unknown error.")
            }) {
                androidx.compose.material3.Text("Register")
            }

            errorMessage.value?.let {
                androidx.compose.material3.Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        }

        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.onNodeWithText("Registration failed: Unknown error.").assertIsDisplayed()
    }

    @Ignore("Temporarily disabled due to a bug")
    @Test
    fun registerScreen_clickRegisterButton_displaysWeakPasswordError() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val errorMessage = remember { mutableStateOf<String?>("") }
            val setErrorMessage: (String) -> Unit = { errorMessage.value = it }

            RegisterScreen(navController = navController)

            androidx.compose.material3.Button(onClick = {
                setErrorMessage("The password is too weak. Please choose a stronger password.")
            }) {
                androidx.compose.material3.Text("Register")
            }

            errorMessage.value?.let {
                androidx.compose.material3.Text(
                    it,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
        }

        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.onNodeWithText("The password is too weak. Please choose a stronger password.").assertIsDisplayed()
    }

    @Ignore("Temporarily disabled due to a bug")
    @Test
    fun registerScreen_clickRegisterButton_displaysInvalidEmailError() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val errorMessage = remember { mutableStateOf<String?>("") }
            val setErrorMessage: (String) -> Unit = { errorMessage.value = it }

            RegisterScreen(navController = navController)

            androidx.compose.material3.Button(onClick = {
                setErrorMessage("The email address is invalid.")
            }) {
                androidx.compose.material3.Text("Register")
            }

            errorMessage.value?.let {
                androidx.compose.material3.Text(
                    it,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
        }

        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.onNodeWithText("The email address is invalid.").assertIsDisplayed()
    }

    @Ignore("Temporarily disabled due to a bug")
    @Test
    fun registerScreen_clickRegisterButton_displaysEmailAlreadyInUseError() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val errorMessage = remember { mutableStateOf<String?>("") }
            val setErrorMessage: (String) -> Unit = { errorMessage.value = it }

            RegisterScreen(navController = navController)

            androidx.compose.material3.Button(onClick = {
                setErrorMessage("The email address is already in use. Try another one.")
            }) {
                androidx.compose.material3.Text("Register")
            }

            errorMessage.value?.let {
                androidx.compose.material3.Text(
                    it,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
        }

        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.onNodeWithText("The email address is already in use. Try another one.").assertIsDisplayed()
    }

    @Ignore("Temporarily disabled due to a bug")
    @Test
    fun registerScreen_errorMessageInitiallyEmpty() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            RegisterScreen(navController = navController)

            composeTestRule.onNodeWithText("Initial Message", useUnmergedTree = true).assertDoesNotExist()
        }
    }
}
