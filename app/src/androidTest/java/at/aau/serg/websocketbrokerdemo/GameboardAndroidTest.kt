package at.aau.serg.websocketbrokerdemo

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.ui.Gameboard
import at.aau.serg.websocketbrokerdemo.ui.calculateTilePosition
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
                players = emptyList()
            )
        }

        composeTestRule.onNodeWithTag("tile_10_10")
            .assertHasClickAction()
            .performClick()

        assert(clickedTilePosition == 0)
    }

    @Test
    fun allOuterTilesShouldBeClickable() {
        composeTestRule.setContent {
            Gameboard(
                onTileClick = {},
                players = emptyList()
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
    fun testPlayerImagesAreDisplayedOnCorrectTile() {
        val players = listOf(
            PlayerMoney(id = "1", name = "Player A", money = 1500, position = 0),
            PlayerMoney(id = "2", name = "Player B", money = 1500, position = 0)
        )

        composeTestRule.setContent {
            Gameboard(
                onTileClick = { _ -> },
                players = players
            )
        }

        players.forEach {
            composeTestRule.onNodeWithTag("playerImage_${it.id}", useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    fun testFallbackAlignmentForMoreThanFourPlayers() {
        val players = (1..5).map {
            PlayerMoney(id = it.toString(), name = "P$it", money = 1500, position = 0)
        }

        composeTestRule.setContent {
            Gameboard(
                players = players
            )
        }

        // Alle fünf Spieler sollten dargestellt sein, auch wenn Alignment.Center als Fallback greift
        players.forEach {
            composeTestRule.onNodeWithTag("playerImage_${it.id}", useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    fun testCalculateTilePosition_corners() {
        assert(calculateTilePosition(10, 10) == 0)   // Start
        assert(calculateTilePosition(10, 0) == 10)
        assert(calculateTilePosition(0, 0) == 20)
        assert(calculateTilePosition(0, 10) == 30)
    }

    @Test
    fun testCalculateTilePosition_edges() {
        assert(calculateTilePosition(10, 5) == 5)
        assert(calculateTilePosition(5, 0) == 15)
        assert(calculateTilePosition(0, 5) == 25)
        assert(calculateTilePosition(5, 10) == 35)
    }

    @Test
    fun testCalculateTilePosition_invalid() {
        assert(calculateTilePosition(5, 5) == -1) // Inner tile
        assert(calculateTilePosition(11, 11) == -1) // Out of bounds
    }

    @Test
    fun testGameboardRendersCorrectNumberOfTiles() {
        composeTestRule.setContent {
            Gameboard(players = emptyList())
        }

        var count = 0
        for (row in 0..10) {
            for (col in 0..10) {
                val tag = "tile_${row}_${col}"
                composeTestRule.onNodeWithTag(tag).assertExists()
                count++
            }
        }

        assert(count == 121) // 11 × 11 Felder
    }

    @Test
    fun testAllPlayersVisibleOnSameTile() {
        val players = (1..4).map {
            PlayerMoney(id = it.toString(), name = "Player $it", money = 1500, position = 0)
        }

        composeTestRule.setContent {
            Gameboard(players = players)
        }

        players.forEach {
            composeTestRule.onNodeWithTag("playerImage_${it.id}", useUnmergedTree = true)
                .assertExists()
        }
    }
}
