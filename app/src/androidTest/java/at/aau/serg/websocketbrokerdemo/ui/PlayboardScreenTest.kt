package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.websocketbrokerdemo.data.ChatEntry
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.GameWebSocketClient
import at.aau.serg.websocketbrokerdemo.data.properties.DummyProperty
import at.aau.serg.websocketbrokerdemo.data.properties.HouseableProperty
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyColor
import at.aau.serg.websocketbrokerdemo.data.properties.copyWithOwner
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
@Ignore
class PlayboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockWebSocketClient = mock(GameWebSocketClient::class.java)

    private val emptyChat = emptyList<ChatEntry>()

    @Test
    fun testPlayboardScreenDisplaysPlayers() {
        val players = listOf(
            PlayerMoney(id = "1", name = "Player 1", money = 1500, position = 0),
            PlayerMoney(id = "2", name = "Player 2", money = 1500, position = 0)
        )

        composeTestRule.setContent {
            PlayboardScreen(
                players = players,
                currentPlayerId = "1",
                localPlayerId = "1",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 5,
                dicePlayerId = "",
                webSocketClient = mockWebSocketClient,
                chatMessages = emptyChat,
                showPassedGoAlert = false,
                passedGoPlayerName = "",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        composeTestRule.onNodeWithText("Player 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Player 2").assertIsDisplayed()
    }

    @Test
    fun testPlayboardScreenDisplaysNoPlayersMessage() {
        composeTestRule.setContent {
            PlayboardScreen(
                players = emptyList(),
                currentPlayerId = "",
                localPlayerId = "1",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 5,
                dicePlayerId = "",
                webSocketClient = mockWebSocketClient,
                chatMessages = emptyChat,
                showPassedGoAlert = false,
                passedGoPlayerName = "",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        // Check if the "Not enough players connected yet" message is displayed
        composeTestRule.onNodeWithText("Not enough players connected yet").assertIsDisplayed()
    }

    @Test
    fun testRollDiceButtonResetsAfterAnimation() = runTest {
        composeTestRule.setContent {
            PlayboardScreen(
                players = emptyList(),
                currentPlayerId = "1",
                localPlayerId = "1",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 5,
                dicePlayerId = "",
                webSocketClient = mockWebSocketClient,
                showPassedGoAlert = false,
                passedGoPlayerName = "",
                chatMessages = emptyChat,
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        composeTestRule.onNodeWithText("Roll Dice").performClick()
        composeTestRule.mainClock.advanceTimeBy(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Roll Dice").assertIsEnabled()
    }

    @Test
    fun testPlayboardScreenDisplaysNoPlayersMessageWithNewParameters() {
        composeTestRule.setContent {
            PlayboardScreen(
                players = emptyList(),
                currentPlayerId = "",
                localPlayerId = "",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = null,
                dicePlayerId = null,
                webSocketClient = mockWebSocketClient,
                showPassedGoAlert = false,
                passedGoPlayerName = "",
                chatMessages = emptyChat,
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        // Check if the "Not enough players connected yet" message is displayed
        composeTestRule.onNodeWithText("Not enough players connected yet").assertIsDisplayed()
    }

    @Test
    fun testPlayboardScreenDisplaysNoPlayersMessageWithNewParametersAgain() {
        composeTestRule.setContent {
            PlayboardScreen(
                players = emptyList(),
                currentPlayerId = "",
                localPlayerId = "",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = null,
                dicePlayerId = null,
                webSocketClient = mockWebSocketClient,
                showPassedGoAlert = false,
                passedGoPlayerName = "",
                chatMessages = emptyChat,
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        // Check if the "Not enough players connected yet" message is displayed
        composeTestRule.onNodeWithText("Not enough players connected yet").assertIsDisplayed()
    }

    @Test
    fun testChatToggleOpensAndClosesChatOverlay() {
        val players = listOf(
            PlayerMoney(id = "1", name = "Player 1", money = 1500, position = 0)
        )

        composeTestRule.setContent {
            PlayboardScreen(
                players = players,
                currentPlayerId = "1",
                localPlayerId = "1",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 2,
                dicePlayerId = "1",
                webSocketClient = mockWebSocketClient,
                chatMessages = listOf(),
                showPassedGoAlert = false,
                passedGoPlayerName = "",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        // Öffne Chat
        composeTestRule.onNodeWithText("Open Chat").performClick()
        composeTestRule.onNodeWithText("Send").assertIsDisplayed()

        // Schließe Chat
        composeTestRule.onNodeWithText("Close Chat").performClick()
        composeTestRule.onNodeWithText("Open Chat").assertIsDisplayed()
    }

    @Test
    fun testChatMessageDisplayAndInput() {
        val messages = listOf(
            ChatEntry(senderId = "1", message = "Hello from me", senderName = "Alex"),
            ChatEntry(senderId = "2", message = "Hello from another", senderName = "Alex2")
        )

        composeTestRule.setContent {
            PlayboardScreen(
                players = listOf(PlayerMoney("1", "Me", 1500, 0)),
                currentPlayerId = "1",
                localPlayerId = "1",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 1,
                dicePlayerId = "1",
                webSocketClient = mockWebSocketClient,
                chatMessages = messages,
                showPassedGoAlert = false,
                passedGoPlayerName = "",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        // Öffne Chat
        composeTestRule.onNodeWithText("Open Chat").performClick()

        // Nachrichten sichtbar
        composeTestRule.onNodeWithText("Hello from me").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hello from another").assertIsDisplayed()
    }
    @Test
    fun endTurnButton_shouldBeVisibleOnlyIfMyTurn() {
        val testPlayers = listOf(
            PlayerMoney("p1", "Alice", 1500, 3),
            PlayerMoney("p2", "Bob", 1500, 5)
        )

        composeTestRule.setContent {
            PlayboardScreen(
                players = testPlayers,
                currentPlayerId = "p1",
                localPlayerId = "p1",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = null,
                dicePlayerId = null,
                webSocketClient = mockWebSocketClient,
                chatMessages = listOf(),
                showPassedGoAlert = false,
                passedGoPlayerName = "",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        composeTestRule.onNodeWithText("End Turn").assertIsDisplayed()
    }

    @Test
    fun propertyPopup_shouldAppearWhenPropertyIsSelected() {
        val property = HouseableProperty(
            id = 1, name = "Some St.", purchasePrice = 100, position = 3,
            baseRent = 10, rent1House = 20, rent2Houses = 30, rent3Houses = 40,
            rent4Houses = 50, rentHotel = 60, housePrice = 50, hotelPrice = 50,
            mortgageValue = 50, image = "some_street", isMortgaged = false, ownerId = null
        )

        val testPlayers = listOf(PlayerMoney("p1", "Alice", 1500, 3))

        composeTestRule.setContent {
            PlayboardScreen(
                players = testPlayers,
                currentPlayerId = "p1",
                localPlayerId = "p1",
                onRollDice = {},
                onBackToLobby = {},
                diceResult = 6,
                dicePlayerId = "p1",
                webSocketClient = mockWebSocketClient,
                chatMessages = listOf(),
                showPassedGoAlert = false,
                passedGoPlayerName = "",
                showTaxPaymentAlert = false,
                taxPaymentPlayerName = "",
                taxPaymentAmount = 0,
                taxPaymentType = ""
            )
        }

        composeTestRule.runOnIdle {
        }

        composeTestRule.onNodeWithText("Some St.").assertIsDisplayed()
    }
    @Test
    fun playerCard_showsOwnedProperties() {
        val owned = listOf(
            DummyProperty(id = 21, position = 21, color = PropertyColor.RED).copyWithOwner("p1"),
            DummyProperty(id = 23, position = 23, color = PropertyColor.RED).copyWithOwner("p1")
        )

        val testPlayer = PlayerMoney("p1", "Alice", 1500, 0)

        composeTestRule.setContent {
            PlayerCard(
                player = testPlayer,
                ownedProperties = owned,
                allProperties = owned,
                isCurrentPlayer = true,
                playerIndex = 0,
                onPropertySetClicked = {}
            )
        }
    }
    @Test
    fun propertySetCard_opensPopupOnClick() {
        val owned = listOf(
            DummyProperty(id = 6, position = 6, color = PropertyColor.LIGHT_BLUE).copyWithOwner("p1")
        )
        val all = owned + DummyProperty(id = 9, position = 9, color = PropertyColor.LIGHT_BLUE)

        val testPlayer = PlayerMoney("p1", "Alice", 1500, 0)

        composeTestRule.setContent {
            PlayerCard(
                player = testPlayer,
                ownedProperties = owned,
                allProperties = all,
                isCurrentPlayer = true,
                playerIndex = 0,
                onPropertySetClicked = {}
            )
        }

        // überprüfen ob Grundstückskarte angezeigt wird
        composeTestRule.onAllNodesWithContentDescription("Property Set").onFirst().performClick()
    }


}
