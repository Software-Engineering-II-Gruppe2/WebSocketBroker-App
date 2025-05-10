package at.aau.serg.websocketbrokerdemo.ui.test

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.websocketbrokerdemo.data.PlayerProfile
import at.aau.serg.websocketbrokerdemo.ui.UserProfileScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore


@RunWith(AndroidJUnit4::class)
class UserProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun userProfileScreen_displaysLoadingState() {
        composeTestRule.setContent {
            UserProfileScreen(playerProfile = null, onNameChange = {}, onBack = {})
        }
        composeTestRule.onNodeWithText("Profil wird geladen...").assertIsDisplayed()
    }

    @Test
    fun userProfileScreen_displaysProfileInformation() {
        val testProfile = PlayerProfile(name = "TestUser", level = 5, gamesPlayed = 10, wins = 7, highestMoney = 1000, averageMoney = 0.0)
        composeTestRule.setContent {
            UserProfileScreen(playerProfile = testProfile, onNameChange = {}, onBack = {})
        }
        composeTestRule.onNodeWithText("Name: TestUser").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level: 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Games Played: 10").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wins: 7").assertIsDisplayed()
        composeTestRule.onNodeWithText("Most Money: 1000").assertIsDisplayed()
        composeTestRule.onNodeWithText("Neuer Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Namen ändern").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zurück").assertIsDisplayed()
    }

    @Test
    fun userProfileScreen_canEnterNewName() {
        val testProfile = PlayerProfile(name = "OldName")
        val newNameInput = "NewTestName"
        composeTestRule.setContent {
            UserProfileScreen(playerProfile = testProfile, onNameChange = {}, onBack = {})
        }
        composeTestRule.onNodeWithText("Neuer Name").performTextInput(newNameInput)
    }

    @Test
    fun userProfileScreen_clickBackButton_triggersOnBack() {
        var backClicked = false
        val onBack: () -> Unit = { backClicked = true }

        composeTestRule.setContent {
            UserProfileScreen(playerProfile = PlayerProfile(1,2, "testUser", 2, 0.0, 0), onNameChange = {}, onBack = onBack)
        }

        composeTestRule.onNodeWithText("Zurück").performClick()

        assertTrue(backClicked)
    }
}