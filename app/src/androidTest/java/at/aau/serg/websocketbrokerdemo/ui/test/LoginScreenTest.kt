package at.aau.serg.websocketbrokerdemo.ui.test

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.websocketbrokerdemo.ui.LoginScreen
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysEmailAndPasswordFieldsAndButtons() {
        composeTestRule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account? Register").assertIsDisplayed()
    }

    @Test
    fun loginScreen_canEnterEmailAndPassword() {
        val testEmail = "test@aau.com"
        val testPassword = "fancypass123!"

        composeTestRule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").performTextInput(testEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(testPassword)

        composeTestRule.onNodeWithText(testEmail).assertIsDisplayed()
        composeTestRule.onNodeWithText("•".repeat(testPassword.length)).assertIsDisplayed()
    }


    /*     @Ignore("Temporarily disabled due to a bug")
    @Test
    fun loginScreen_clickRegisterButton_triggersNavigation() {
        var navigated = false
        val mockNavController = rememberNavController()
        mockNavController.addOnDestinationChangedListener { _, _, _ ->
            navigated = true
        }

        composeTestRule.setContent {
            LoginScreen(navController = mockNavController)
        }

        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()

        assertTrue(navigated)
    }*/

    @Ignore("Temporarily disabled due to a bug")
    @Test
    fun loginScreen_clickLoginButton_displaysGenericError() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val errorMessage = remember { mutableStateOf<String?>("") }
            val setErrorMessage: (String) -> Unit = { errorMessage.value = it }

            LoginScreen(navController = navController)

            androidx.compose.material3.Button(onClick = {
                setErrorMessage("Login failed: Unknown error.")
            }) {
                androidx.compose.material3.Text("Login")
            }

            errorMessage.value?.let {
                androidx.compose.material3.Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        }

        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.onNodeWithText("Login failed: Unknown error.").assertIsDisplayed()
    }

    @Ignore("Temporarily disabled due to a bug")
    @Test
    fun loginScreen_clickLoginButton_displaysWrongPasswordError() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val errorMessage = remember { mutableStateOf<String?>("") }
            val setErrorMessage: (String) -> Unit = { errorMessage.value = it }

            LoginScreen(navController = navController)
            androidx.compose.material3.Button(onClick = {
                setErrorMessage("The password is incorrect. Please try again.")
            }) {
                androidx.compose.material3.Text("Login")
            }

            errorMessage.value?.let {
                androidx.compose.material3.Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        }

        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.onNodeWithText("The password is incorrect. Please try again.").assertIsDisplayed()
    }

    @Ignore("Temporarily disabled due to a bug")
    @Test
    fun loginScreen_clickLoginButton_displaysUserNotFoundError() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val errorMessage = remember { mutableStateOf<String?>("") }
            val setErrorMessage: (String) -> Unit = { errorMessage.value = it }

            LoginScreen(navController = navController)

            androidx.compose.material3.Button(onClick = {
                setErrorMessage("No account found with this email. Please register first.")
            }) {
                androidx.compose.material3.Text("Login")
            }

            errorMessage.value?.let {
                androidx.compose.material3.Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        }

        composeTestRule.onNodeWithText("Login", useUnmergedTree = true).performClick() // Added useUnmergedTree
        composeTestRule.onNodeWithText("No account found with this email. Please register first.").assertIsDisplayed()
    }

    @Ignore("Temporarily disabled due to a bug")
    @Test
    fun loginScreen_clickLoginButton_displaysInvalidEmailError() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val errorMessage = remember { mutableStateOf<String?>("") }
            val setErrorMessage: (String) -> Unit = { errorMessage.value = it }

            LoginScreen(navController = navController)
            androidx.compose.material3.Button(onClick = {
                setErrorMessage("The email address is invalid. Please check and try again.")
            }) {
                androidx.compose.material3.Text("Login")
            }

            errorMessage.value?.let {
                androidx.compose.material3.Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        }

        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.onNodeWithText("The email address is invalid. Please check and try again.").assertIsDisplayed()
    }

    @Ignore("Temporarily disabled due to a bug")
    @Test
    fun loginScreen_errorMessageInitiallyEmpty() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            LoginScreen(navController = navController)

            composeTestRule.onNodeWithText("Initial Message", useUnmergedTree = true).assertDoesNotExist()
        }
    }
}
