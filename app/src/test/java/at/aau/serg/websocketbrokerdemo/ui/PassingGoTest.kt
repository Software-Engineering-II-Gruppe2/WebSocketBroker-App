package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.GameWebSocketClient
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import at.aau.serg.websocketbrokerdemo.data.ChatEntry
import at.aau.serg.websocketbrokerdemo.data.CheatEntry
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers


class PassingGoTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockContext = mockk<Context>(relaxed = true)
    private val emptyChat = emptyList<ChatEntry>()
    private val emptyCheat = emptyList<CheatEntry>()
    private val gameEvents = mutableStateListOf<String>()

    private val mockWebSocketClient = GameWebSocketClient(
        context = mockContext,
        onConnected = { /* No-op for testing */ },
        onMessageReceived = { /* No-op for testing */ },
        onDiceRolled = { _, _, _, _-> /* No-op for testing */ },
        onGameStateReceived = { /* No-op for testing */ },
        onPlayerTurn = { /* No-op for testing */ },
        onPlayerPassedGo = { /* No-op for testing */ },
        coroutineDispatcher = Dispatchers.IO,
        onChatMessageReceived = { _, _ -> },
        onCheatMessageReceived = { _, _ -> },
        onCardDrawn = { _, _, _, _ -> },
        onClearChat = { /* No-op for testing */ },
        onHasWon = { _ -> },
        onTaxPayment = { _, _, _ -> /* No-op for testing */ },
        onDealProposal = { /* No-op for testing */ },
        onGiveUpReceived = {/* No-op for testing */},
        onDealResponse = { /* No-op for testing */ }
    )

    @Test
    fun testPassingGoAlertShowsCorrectly() {
        val testPlayer = PlayerMoney("test-id", "Test Player", 1500, 0,false,0, false)
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
                hasRolled = false,
                hasPasch = false,
                setHasRolled = {},
                setHasPasch = {},
                webSocketClient = mockWebSocketClient,
                chatMessages = emptyChat,
                cheatMessages = emptyCheat,
                showPassedGoAlert = true,
                passedGoPlayerName = "Test Player",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = "",
                cheatFlags = emptyMap(),
                incomingDeal = null,
                showIncomingDialog = false,
                setIncomingDeal = {},
                setShowIncomingDialog = {},
                onGiveUp = {},
                drawnCardType = null,
                drawnCardId = null,
                drawnCardDesc = null,
                onCardDialogDismiss = {},
                gameEvents = gameEvents,
                avatarMap = emptyMap()
            )
        }

        // Verify the alert is shown with correct text
        composeTestRule.onNodeWithText("Glückwunsch!").assertExists()
        composeTestRule.onNodeWithText("Test Player fuhr über los und erhält 200€!").assertExists()
    }

    @Test
    fun testPassingGoAlertDisappearsAfterDelay() = runTest {
        var showAlert = true
        val testPlayer = PlayerMoney("test-id", "Test Player", 1500, 0,false,0, false)
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
                hasRolled = false,
                hasPasch = false,
                setHasRolled = {},
                setHasPasch = {},
                webSocketClient = mockWebSocketClient,
                chatMessages = emptyChat,
                cheatMessages = emptyCheat,
                showPassedGoAlert = showAlert,
                passedGoPlayerName = "Test Player",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = "",
                cheatFlags = emptyMap(),
                incomingDeal = null,
                showIncomingDialog = false,
                setIncomingDeal = {},
                setShowIncomingDialog = {},
                onGiveUp = {},
                drawnCardType = null,
                drawnCardId = null,
                drawnCardDesc = null,
                onCardDialogDismiss = {},
                gameEvents = gameEvents,
                avatarMap = emptyMap()
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
                hasRolled = false,
                hasPasch = false,
                setHasRolled = {},
                setHasPasch = {},
                webSocketClient = mockWebSocketClient,
                chatMessages = emptyChat,
                cheatMessages = emptyCheat,
                showPassedGoAlert = showAlert,
                passedGoPlayerName = "Test Player",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = "",
                cheatFlags = emptyMap(),
                incomingDeal = null,
                showIncomingDialog = false,
                setIncomingDeal = {},
                setShowIncomingDialog = {},
                onGiveUp = {},
                drawnCardType = null,
                drawnCardId = null,
                drawnCardDesc = null,
                onCardDialogDismiss = {},
                gameEvents = gameEvents,
                avatarMap = emptyMap()
            )
        }

        // Verify alert is gone
        composeTestRule.onNodeWithText("Glückwunsch!").assertDoesNotExist()
    }

    @Test
    fun testPropertyCardShowsAfterGoAlert() = runTest {
        val testPlayer = PlayerMoney("test-id", "Test Player", 1500, 0,false,0, false)
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
                hasRolled = false,
                hasPasch = false,
                setHasRolled = {},
                setHasPasch = {},
                webSocketClient = mockWebSocketClient,
                chatMessages = emptyChat,
                cheatMessages = emptyCheat,
                showPassedGoAlert = true,
                passedGoPlayerName = "Test Player",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = "",
                cheatFlags = emptyMap(),
                incomingDeal = null,
                showIncomingDialog = false,
                setIncomingDeal = {},
                setShowIncomingDialog = {},
                onGiveUp = {},
                drawnCardType = null,
                drawnCardId = null,
                drawnCardDesc = null,
                onCardDialogDismiss = {},
                gameEvents = gameEvents,
                avatarMap = emptyMap()
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
        val testPlayer = PlayerMoney("test-id", "Test Player", initialMoney, 0,false,0, false)
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
                hasRolled = false,
                hasPasch = false,
                setHasRolled = {},
                setHasPasch = {},
                webSocketClient = mockWebSocketClient,
                chatMessages = emptyChat,
                cheatMessages = emptyCheat,
                showPassedGoAlert = true,
                passedGoPlayerName = "Test Player",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = "",
                cheatFlags = emptyMap(),
                incomingDeal = null,
                showIncomingDialog = false,
                setIncomingDeal = {},
                setShowIncomingDialog = {},
                onGiveUp = {},
                drawnCardType = null,
                drawnCardId = null,
                drawnCardDesc = null,
                onCardDialogDismiss = {},
                gameEvents = gameEvents,
                avatarMap = emptyMap()
            )
        }

        // Verify player's money is updated (1500 + 200)
        composeTestRule.onNodeWithText("EUR ${initialMoney + 200}").assertExists()
    }
}
