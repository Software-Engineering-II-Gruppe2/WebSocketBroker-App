package at.aau.serg.websocketbrokerdemo.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import at.aau.serg.websocketbrokerdemo.GameWebSocketClient
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.data.properties.Property
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyColor

@Composable
fun PlayerCard(
    player: PlayerMoney,
    ownedProperties: List<Property>,
    allProperties: List<Property>,
    isCurrentPlayer: Boolean,
    playerIndex: Int,
    onPropertySetClicked: (PropertyColor) -> Unit,
    webSocketClient: GameWebSocketClient
) {


    val playerColors = listOf(
        Color(0x80FF0000),
        Color(0x800000FF),
        Color(0x8000FF00),
        Color(0x80FFFF00)
    )


    val backgroundColor = if (player.bot) {
        Color(0xFFB0BEC5)          // Light Grey
    } else {
        playerColors[playerIndex].copy(alpha = 0.4f)
    }

    val displayName = if (player.bot) "${player.name} 🤖" else player.name


    var selectedColorSet by remember { mutableStateOf<PropertyColor?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(
                if (isCurrentPlayer) {
                    Modifier.border(4.dp, Color.Black, RoundedCornerShape(8.dp))
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape  = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* Kopfzeile: Name, Geld, Position */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(displayName,      color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("EUR ${player.money}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Pos: ${player.position}", color = Color.White, fontSize = 12.sp)
            }


            Text("ID: ${player.id}", color = Color.White, fontSize = 6.sp)

            HorizontalDivider(
                modifier   = Modifier.padding(vertical = 4.dp),
                thickness  = 1.dp,
                color      = Color.White
            )


            BesitzkartenGrid(
                ownedProperties   = ownedProperties,
                allProperties     = allProperties,
                onPropertySetClicked = { selectedColorSet = it }
            )


            selectedColorSet?.let { colorSet ->
                PropertySetPopup(
                    colorSet        = colorSet,
                    ownedProperties = ownedProperties.filter { getColorForPosition(it.position) == colorSet },
                    allProperties   = allProperties.filter { getColorForPosition(it.position) == colorSet },
                    onDismiss       = { selectedColorSet = null },
                    onSellProperty  = { propertyId -> webSocketClient.logic().sellProperty(propertyId) }
                )
            }
        }
    }
}
