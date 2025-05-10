package at.aau.serg.websocketbrokerdemo.ui

import com.example.myapplication.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney

@Composable
fun Gameboard(
    modifier: Modifier = Modifier,
    onTileClick: (tilePosition: Int) -> Unit = {},
    players: List<PlayerMoney> = emptyList()
) {
    // Make the corners bigger like in a real monopoly board
    val cornerFactor = 1.5f
    val regularFactor = 1f

    // TODO: change the circles to real game pieces later
    //Changed the cirles to pictures
    val playerImages = listOf(
        R.drawable.player_red,
        R.drawable.player_blue,
        R.drawable.player_green,
        R.drawable.player_yellow
    )
    val boardPainter = painterResource(R.drawable.monopoly_board)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(0.dp))
            .testTag("gameboard")
    ) {
        Image(
            painter = boardPainter,
            contentDescription = "Monopoly board",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(Modifier.fillMaxSize()) {
            repeat(11) { row ->
                val rowWeight = if (row == 0 || row == 10) cornerFactor else regularFactor

                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(rowWeight),
                    verticalAlignment = Alignment.Bottom
                ) {
                    repeat(11) { col ->
                        val tilePos = calculateTilePosition(row, col)
                        val playersOnTile = players.filter { it.position == tilePos }
                        val isCorner = (row == 0 || row == 10) && (col == 0 || col == 10)
                        val tileWeight = if (isCorner) cornerFactor else regularFactor

                        Box(
                            modifier = Modifier
                                .weight(tileWeight)
                                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                                .background(Color.Transparent)
                                .clickable(enabled = tilePos >= 0) { onTileClick(tilePos) }
                                .semantics { contentDescription = "Tile($row,$col)" }
                                .testTag("tile_${row}_$col"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (playersOnTile.isNotEmpty()) {
                                val gridAlignments = listOf(
                                    Alignment.TopStart,
                                    Alignment.TopEnd,
                                    Alignment.BottomStart,
                                    Alignment.BottomEnd
                                )

                                Box(modifier = Modifier.fillMaxSize(0.8f)) {
                                    playersOnTile.forEachIndexed { index, player ->
                                        val idx = players.indexOfFirst { it.id == player.id }
                                        val imageRes =
                                            playerImages.getOrElse(idx) { R.drawable.player_red }

                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .align(gridAlignments.getOrElse(index) { Alignment.Center }) // Max. 4 Spieler
                                        ) {
                                            Image(
                                                painter = painterResource(id = imageRes),
                                                contentDescription = "Player ${player.id}",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .testTag("playerImage_${player.id}"),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 0 = Start-Tile
 * Indices are increased clockwise (0-39)
 */
fun calculateTilePosition(row: Int, col: Int): Int {
    return when {
        // Corners:
        row == 10 && col == 10 -> 0  // Bottom-right corner (Start)
        row == 10 && col == 0 -> 10  // Bottom-left corner
        row == 0 && col == 0 -> 20   // Top-left corner
        row == 0 && col == 10 -> 30  // Top-right corner

        // Normal Tiles:
        row == 10 -> if (col > 0 && col < 10) 10 - col else -1  // Bottom row (1-9)
        col == 0 -> if (row > 0 && row < 10) 10 + (10 - row) else -1  // Left column (11-19)
        row == 0 -> if (col > 0 && col < 10) 20 + col else -1  // Top row (21-29)
        col == 10 -> if (row > 0 && row < 10) 30 + row else -1  // Right column (31-39)

        else -> -1  // Inner or invalid positions
    }
}
