package at.aau.serg.websocketbrokerdemo.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.logic.TilePositionCalculator
import com.example.myapplication.R


@Composable
fun Gameboard(
    modifier: Modifier = Modifier,
    onTileClick: (tilePosition: Int) -> Unit = {},
    players: List<PlayerMoney> = emptyList(),
    cheatFlags: Map<String, Boolean>,
    avatarMap: Map<String, Int> = emptyMap(),
    gameEvents: List<String> = emptyList()
) {
    val context = LocalContext.current
    LaunchedEffect(gameEvents) {
        if (gameEvents.isNotEmpty()) {
            val latestEvent = gameEvents.last()
            Toast.makeText(context, latestEvent, Toast.LENGTH_LONG).show()
        }
    }
    // Make the corners bigger like in a real monopoly board
    val cornerFactor = 1.5f
    val regularFactor = 1f
    val tokenSize: Dp = 100.dp

    val pullIn = 12.dp
    val offsets = listOf(
        pullIn to pullIn,     // TopStart → right/down
        -pullIn to pullIn,    // TopEnd   → left /down
        pullIn to -pullIn,    // BottomStart → right/up
        -pullIn to -pullIn    // BottomEnd → left/up
    )

    //Changed the circles to pictures
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
                        val tilePos = TilePositionCalculator.calculateTilePosition(row, col)
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
                                    playersOnTile.forEachIndexed { idxOnTile, player ->
                                        // find which slot this player is in (0–3)
                                        val cheated = cheatFlags[player.id] == true
                                        val baseAvatar = avatarMap[player.id] ?: R.drawable.player_red // fallback normal

                                        val imageRes = if (cheated) {
                                            when (baseAvatar) {
                                                R.drawable.player_red -> R.drawable.player_red_cheat
                                                R.drawable.player_blue -> R.drawable.player_blue_cheat
                                                R.drawable.player_green -> R.drawable.player_green_cheat
                                                R.drawable.player_yellow -> R.drawable.player_yellow_cheat
                                                else -> R.drawable.player_red_cheat
                                            }
                                        } else {
                                            baseAvatar
                                        }


                                        // Apply offsets only if multiple game pieces are on a tile
                                        val applyOffset = if (playersOnTile.size > 1)
                                            offsets.getOrElse(idxOnTile) { 0.dp to 0.dp }
                                        else
                                            0.dp to 0.dp

                                        Image(
                                            painter = painterResource(id = imageRes),
                                            contentDescription = "Player ${player.id}" + if (cheated) " (cheater)" else "",
                                            modifier = Modifier
                                                .size(tokenSize)
                                                .align(gridAlignments[idxOnTile])
                                                .offset(x = applyOffset.first, y = applyOffset.second)
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
