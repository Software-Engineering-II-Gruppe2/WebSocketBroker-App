package at.aau.serg.websocketbrokerdemo.ui
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPlayboardScreenDisplaysPlayers() {
        val players = listOf(
            PlayerMoney(id = "1", name = "Player 1", money = 1500),
            PlayerMoney(id = "2", name = "Player 2", money = 1500)
        )

        composeTestRule.setContent {
            PlayboardScreen(players = players, currentPlayerId = "1", onRollDice = {}, onBackToLobby = {}, diceResult = 5,
            dicePlayerId = "")
        }

        // Check if player names are displayed
        composeTestRule.onNodeWithText("Player 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Player 2").assertIsDisplayed()
    }

    @Test
    fun testPlayboardScreenDisplaysNoPlayersMessage() {
        composeTestRule.setContent {
            PlayboardScreen(players = emptyList(), currentPlayerId = "", onRollDice = {}, onBackToLobby = {}, diceResult = 5,
                dicePlayerId = "")
        }

        // Check if the "Not enough players connected yet" message is displayed
        composeTestRule.onNodeWithText("Not enough players connected yet").assertIsDisplayed()
    }
}