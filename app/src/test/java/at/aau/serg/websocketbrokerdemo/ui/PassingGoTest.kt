package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.GameWebSocketClient
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import org.junit.Assert.*
import android.content.Context
import at.aau.serg.websocketbrokerdemo.data.ChatEntry
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers

class PassingGoTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockContext = mockk<Context>(relaxed = true)
    private val emptyChat = emptyList<ChatEntry>()
    private val mockWebSocketClient = GameWebSocketClient(
        context = mockContext,
        onConnected = { /* No-op for testing */ },
        onMessageReceived = { /* No-op for testing */ },
        onDiceRolled = { _, _ -> /* No-op for testing */ },
        onGameStateReceived = { /* No-op for testing */ },
        onPlayerTurn = { /* No-op for testing */ },
        onPlayerPassedGo = { /* No-op for testing */ },
        coroutineDispatcher = Dispatchers.IO,
        onCardDrawn = {_,_,_ ->},
        onChatMessageReceived = { _, _ -> },
        onTaxPayment = { _, _, _ -> /* No-op for testing */ }
    )

    @Test
    fun testPassingGoAlertShowsCorrectly() {
        val testPlayer = PlayerMoney("test-id", "Test Player", 1500, 0)
        val players = listOf(testPlayer)

        composeTestRule.setContent {
            PlayboardScreen(
                players = players,
                currentPlayerId = "test-id",
                localPlayerId = "test-id",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 5,
                dicePlayerId = "test-id",
                webSocketClient = mockWebSocketClient,
                showPassedGoAlert = true,
                passedGoPlayerName = "Test Player",
                chatMessages = emptyChat,
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        // Verify the alert is shown with correct text
        composeTestRule.onNodeWithText("Glückwunsch!").assertExists()
        composeTestRule.onNodeWithText("Test Player fuhr über los und erhält 200€!").assertExists()
    }

    @Test
    fun testPassingGoAlertDisappearsAfterDelay() = runTest {
        var showAlert = true
        val testPlayer = PlayerMoney("test-id", "Test Player", 1500, 0)
        val players = listOf(testPlayer)

        composeTestRule.setContent {
            PlayboardScreen(
                players = players,
                currentPlayerId = "test-id",
                localPlayerId = "test-id",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 5,
                dicePlayerId = "test-id",
                webSocketClient = mockWebSocketClient,
                showPassedGoAlert = showAlert,
                passedGoPlayerName = "Test Player",
                chatMessages = emptyChat,
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        // Verify alert is initially shown
        composeTestRule.onNodeWithText("Glückwunsch!").assertExists()

        // Simulate delay
        delay(3000)
        showAlert = false
        composeTestRule.setContent {
            PlayboardScreen(
                players = players,
                currentPlayerId = "test-id",
                localPlayerId = "test-id",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 5,
                dicePlayerId = "test-id",
                webSocketClient = mockWebSocketClient,
                showPassedGoAlert = showAlert,
                passedGoPlayerName = "Test Player",
                chatMessages = emptyChat,
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        // Verify alert is gone
        composeTestRule.onNodeWithText("Glückwunsch!").assertDoesNotExist()
    }

    @Test
    fun testPropertyCardShowsAfterGoAlert() = runTest {
        val testPlayer = PlayerMoney("test-id", "Test Player", 1500, 0)
        val players = listOf(testPlayer)

        composeTestRule.setContent {
            PlayboardScreen(
                players = players,
                currentPlayerId = "test-id",
                localPlayerId = "test-id",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 5,
                dicePlayerId = "test-id",
                webSocketClient = mockWebSocketClient,
                showPassedGoAlert = true,
                passedGoPlayerName = "Test Player",
                chatMessages = emptyChat,
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        // Verify GO alert is shown
        composeTestRule.onNodeWithText("Glückwunsch!").assertExists()

        // Simulate delay
        delay(3000)

        // Verify property card is shown (assuming player landed on a property)
        composeTestRule.onNodeWithText("Exit").assertExists()
    }

    @Test
    fun testMoneyUpdateAfterPassingGo() {
        val initialMoney = 1500
        val testPlayer = PlayerMoney("test-id", "Test Player", initialMoney, 0)
        val players = listOf(testPlayer)

        composeTestRule.setContent {
            PlayboardScreen(
                players = players,
                currentPlayerId = "test-id",
                localPlayerId = "test-id",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 5,
                dicePlayerId = "test-id",
                webSocketClient = mockWebSocketClient,
                showPassedGoAlert = true,
                passedGoPlayerName = "Test Player",
                chatMessages = emptyChat,
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        // Verify player's money is updated (1500 + 200)
        composeTestRule.onNodeWithText("EUR ${initialMoney + 200}").assertExists()
    }
}
