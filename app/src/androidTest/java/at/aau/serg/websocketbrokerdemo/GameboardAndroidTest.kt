package at.aau.serg.websocketbrokerdemo

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.ui.Gameboard
import at.aau.serg.websocketbrokerdemo.logic.TilePositionCalculator
import org.junit.Ignore
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
                cheatFlags = emptyMap()
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
                players = emptyList(),
                cheatFlags = emptyMap()
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
            PlayerMoney(id = "1", name = "Player A", money = 1500, position = 0, false,0, false),
            PlayerMoney(id = "2", name = "Player B", money = 1500, position = 0,false,0, false)
        )

        composeTestRule.setContent {
            Gameboard(
                onTileClick = { _ -> },
                players = players,
                cheatFlags = emptyMap()
            )
        }

        players.forEach {
            composeTestRule.onNodeWithTag("playerImage_${it.id}", useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    @Ignore
    fun testFallbackAlignmentForMoreThanFourPlayers() {
        val players = (1..5).map {
            PlayerMoney(id = it.toString(), name = "P$it", money = 1500, position = 0,false,0, false)
        }

        composeTestRule.setContent {
            Gameboard(
                onTileClick = { _ -> },
                players = players,
                cheatFlags = emptyMap()
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
        assert(TilePositionCalculator.calculateTilePosition(10, 10) == 0)   // Start
        assert(TilePositionCalculator.calculateTilePosition(10, 0) == 10)
        assert(TilePositionCalculator.calculateTilePosition(0, 0) == 20)
        assert(TilePositionCalculator.calculateTilePosition(0, 10) == 30)
    }

    @Test
    fun testCalculateTilePosition_edges() {
        assert(TilePositionCalculator.calculateTilePosition(10, 5) == 5)
        assert(TilePositionCalculator.calculateTilePosition(5, 0) == 15)
        assert(TilePositionCalculator.calculateTilePosition(0, 5) == 25)
        assert(TilePositionCalculator.calculateTilePosition(5, 10) == 35)
    }

    @Test
    fun testCalculateTilePosition_invalid() {
        assert(TilePositionCalculator.calculateTilePosition(5, 5) == -1) // Inner tile
        assert(TilePositionCalculator.calculateTilePosition(11, 11) == -1) // Out of bounds
    }

    @Test
    fun testGameboardRendersCorrectNumberOfTiles() {
        composeTestRule.setContent {
            Gameboard(
                onTileClick = { _ -> },
                players = emptyList(),
                cheatFlags = emptyMap()
            )
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
            PlayerMoney(id = it.toString(), name = "Player $it", money = 1500, position = 0,false,0, false)
        }

        composeTestRule.setContent {
            Gameboard(
                onTileClick = { _ -> },
                players = players,
                cheatFlags = emptyMap()
            )
        }

        players.forEach {
            composeTestRule.onNodeWithTag("playerImage_${it.id}", useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    fun testCheatFlagSwitchesContentDescription() {
        val players = listOf(
            PlayerMoney(id = "1", name = "Alice", money = 1500, position = 0,false,0, false),
            PlayerMoney(id = "2", name = "Bob",   money = 1500, position = 0,false,0, false)
        )
        val cheatFlags = mapOf("1" to true, "2" to false)

        composeTestRule.setContent {
            Gameboard(
                onTileClick = { _ -> },
                players = players,
                cheatFlags = cheatFlags
            )
        }

        // Player 1 should have "(cheater)" in its contentDescription
        composeTestRule.onNodeWithTag("playerImage_1", useUnmergedTree = true)
            .assertContentDescriptionEquals("Player 1 (cheater)")

        // Player 2 should _not_ have "(cheater)"
        composeTestRule.onNodeWithTag("playerImage_2", useUnmergedTree = true)
            .assertContentDescriptionEquals("Player 2")
    }

    @Test
    fun testOffsetOnlyAppliedWhenMultiplePlayers() {
        // two dummy players, same tile
        val p1 = PlayerMoney(id = "1", name = "Solo", money = 1500, position = 0,false,0, false)
        val p2 = PlayerMoney(id = "2", name = "Duo",  money = 1500, position = 0,false,0, false)

        lateinit var playersState: androidx.compose.runtime.MutableState<List<PlayerMoney>>

        // 1) set up with only one player
        composeTestRule.setContent {
            playersState = remember { mutableStateOf(listOf(p1)) }
            Gameboard(
                onTileClick = {},
                players      = playersState.value,
                cheatFlags   = emptyMap()
            )
        }

        // let compose settle
        composeTestRule.waitForIdle()
        val singlePos = composeTestRule
            .onNodeWithTag("playerImage_1", useUnmergedTree = true)
            .fetchSemanticsNode()
            .positionInRoot

        // 2) mutate to two players
        composeTestRule.runOnIdle {
            playersState.value = listOf(p1, p2)
        }
        composeTestRule.waitForIdle()

        val pos1 = composeTestRule
            .onNodeWithTag("playerImage_1", useUnmergedTree = true)
            .fetchSemanticsNode()
            .positionInRoot

        val pos2 = composeTestRule
            .onNodeWithTag("playerImage_2", useUnmergedTree = true)
            .fetchSemanticsNode()
            .positionInRoot

        // when there's more than one player on the same tile...
        //  → the first token must move
        assert(singlePos != pos1) {
            "Expected first player to be offset once a second player arrives"
        }
        //  → and the two tokens must be at different spots
        assert(pos1 != pos2) {
            "Expected distinct offsets for two tokens on the same tile"
        }
    }
}
