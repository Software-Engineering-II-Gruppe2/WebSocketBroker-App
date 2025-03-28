package at.aau.serg.websocketbrokerdemo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation.compose.rememberNavController
import at.aau.serg.websocketbrokerdemo.ui.LoginScreen
import at.aau.serg.websocketbrokerdemo.ui.RegisterScreen
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class AuthActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Test LoginScreen UI
    @Test
    fun testLoginScreen_UI() {
        // Set the content for the test
        composeTestRule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()

        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account? Register").assertIsDisplayed()
    }

    // Test Login Button functionality (successful login scenario)
    @Ignore("Test is temporarily disabled.")
    @Test
    fun testLoginButton_EnableAndClick() {
        composeTestRule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Login").assertIsEnabled()

        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Email").assertTextEquals("")
    }

    // Test Register screen navigation
    @Ignore("Test is temporarily disabled.")
    @Test
    fun testNavigateToRegisterScreen() {
        composeTestRule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        composeTestRule.onNodeWithText("Already have an account? Login").assertIsDisplayed()
    }

    // Test RegisterScreen UI
    @Test
    fun testRegisterScreen_UI() {
        composeTestRule.setContent {
            RegisterScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()

        composeTestRule.onNodeWithText("Register").assertIsDisplayed()
        composeTestRule.onNodeWithText("Already have an account? Login").assertIsDisplayed()
    }

    // Test Register Button functionality (successful registration scenario)
    @Ignore("Test is temporarily disabled.")
    @Test
    fun testRegisterButton_EnableAndClick() {
        composeTestRule.setContent {
            RegisterScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").performTextInput("newuser@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("newpassword123")

        composeTestRule.onNodeWithText("Register").assertIsEnabled()
        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.onNodeWithText("Email").assertTextEquals("")
    }

    // Test navigation back to Login screen from Register screen
    @Ignore("Test is temporarily disabled.")
    @Test
    fun testNavigateBackToLoginScreen() {
        composeTestRule.setContent {
            RegisterScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Already have an account? Login").performClick()
        composeTestRule.onNodeWithText("Don't have an account? Register").assertIsDisplayed()
    }
}
