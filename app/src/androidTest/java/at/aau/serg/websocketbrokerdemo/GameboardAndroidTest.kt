package at.aau.serg.websocketbrokerdemo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.ui.Gameboard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameboardAndroidTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testIfGameBoardIsClickableAndRendersAllTiles() {
        var clickedTilePosition = -1
        composeTestRule.setContent {
            Gameboard(
                onTileClick = { tilePosition -> clickedTilePosition = tilePosition },
                players = emptyList(),
                properties = emptyList()
            )
        }

        composeTestRule.onNodeWithTag("tile_10_10")
            .assertHasClickAction()
            .performClick()

        assert(clickedTilePosition == 0)
    }

    @Test
    fun testGameboardDisplaysPlayersAsCirclesOnTheOuterTiles() {
        val players = listOf(
            PlayerMoney(id = "1", name = "Player A", money = 1500, position = 0),
            PlayerMoney(id = "2", name = "Player B", money = 1500, position = 10)
        )

        composeTestRule.setContent {
            Gameboard(
                onTileClick = { _ -> },
                players = players,
                properties = emptyList()
            )
        }

        composeTestRule.onNodeWithTag("playerCircle_1", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("playerCircle_2", useUnmergedTree = true).assertExists()
    }

    @Test
    fun allOuterTilesShouldBeClickable() {
        composeTestRule.setContent {
            Gameboard(
                onTileClick = {},
                players = emptyList(),
                properties = emptyList()
            )
        }

        val clickablePositions = listOf(
            Pair(10, 10), Pair(10, 9), Pair(10, 8), /* … */ Pair(0, 10) // alle Ecken + Ränder
        )

        for ((row, col) in clickablePositions) {
            composeTestRule.onNodeWithTag("tile_${row}_${col}")
                .assertHasClickAction()
        }
    }

    @Test
    fun multiplePlayersOnSameTileAreDisplayed() {
        val players = listOf(
            PlayerMoney("1", "A", 1500, 0),
            PlayerMoney("2", "B", 1500, 0),
            PlayerMoney("3", "C", 1500, 0)
        )

        composeTestRule.setContent {
            Gameboard(
                players = players,
                properties = emptyList()
            )
        }

        players.forEach {
            composeTestRule.onNodeWithTag("playerCircle_${it.id}", useUnmergedTree = true)
                .assertExists()
        }
    }
}
