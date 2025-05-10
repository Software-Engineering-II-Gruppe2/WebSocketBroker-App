package at.aau.serg.websocketbrokerdemo.ui.test

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.websocketbrokerdemo.ui.LobbyScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LobbyScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lobbyScreen_displaysAllButtons() {
        val messageState = mutableStateOf("")
        val logState = mutableStateOf("")
        composeTestRule.setContent {
            LobbyScreen(
                message = messageState.value,
                log = logState.value,
                onMessageChange = { messageState.value = it },
                onConnect = {},
                onDisconnect = {},
                onSendMessage = {},
                onLogout = {},
                onProfileClick = {},
                onStatisticsClick = {},
                onLeaderboardClick = {},
                onOpenSettings = {},
                onOpenSoundSelection = {},
                onJoinGame = {}
            )
        }

        composeTestRule.onNodeWithText("Connect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disconnect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Send Message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun lobbyScreen_messageTextFieldUpdates() {
        val messageState = mutableStateOf("")
        val logState = mutableStateOf("")

        composeTestRule.setContent {
            LobbyScreen(
                message = messageState.value,
                log = logState.value,
                onMessageChange = { messageState.value = it },
                onConnect = {},
                onDisconnect = {},
                onSendMessage = {},
                onLogout = {},
                onProfileClick = {},
                onStatisticsClick = {},
                onLeaderboardClick = {},
                onOpenSettings = {},
                onOpenSoundSelection = {},
                onJoinGame = {}
            )
        }

        val testMessage = "Hello, Lobby!"
        composeTestRule.onNode(hasSetTextAction()).performTextInput(testMessage)

        composeTestRule.runOnIdle {
            assertTrue("Message should be updated", messageState.value == testMessage)
        }
    }

    @Test
    fun lobbyScreen_connectButtonTriggersOnConnect() {
        var wasConnectClicked = false
        val messageState = mutableStateOf("")
        val logState = mutableStateOf("")

        composeTestRule.setContent {
            LobbyScreen(
                message = messageState.value,
                log = logState.value,
                onMessageChange = { messageState.value = it },
                onConnect = { wasConnectClicked = true },
                onDisconnect = {},
                onSendMessage = {},
                onLogout = {},
                onProfileClick = {},
                onStatisticsClick = {},
                onLeaderboardClick = {},
                onOpenSettings = {},
                onOpenSoundSelection = {},
                onJoinGame = {}
            )
        }

        composeTestRule.onNodeWithText("Connect").performClick()
        composeTestRule.runOnIdle {
            assertTrue("onConnect callback should be invoked", wasConnectClicked)
        }
    }
}
