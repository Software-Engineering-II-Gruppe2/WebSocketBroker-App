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
        Color(0x80FF0000), // Less saturated Red
        Color(0x800000FF), // Less saturated Blue
        Color(0x8000FF00), // Less saturated Green
        Color(0x80FFFF00)  // Less saturated Yellow
    )

    val backgroundColor = playerColors[playerIndex].copy(alpha = 0.4f)

    var selectedColorSet by remember { mutableStateOf<PropertyColor?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(
                if (isCurrentPlayer) {
                    Modifier.border(
                        width = 4.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "EUR ${player.money}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pos: ${player.position}",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "ID: ${player.id}",
                color = Color.White,
                fontSize = 6.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 1.dp,
                color = Color.White
            )

            BesitzkartenGrid(
                ownedProperties = ownedProperties,
                allProperties = allProperties,
                onPropertySetClicked = { colorSet -> selectedColorSet = colorSet }
            )

            if (selectedColorSet != null) {
                PropertySetPopup(
                    colorSet = selectedColorSet!!,
                    ownedProperties = ownedProperties.filter { getColorForPosition(it.position) == selectedColorSet },
                    allProperties = allProperties.filter { getColorForPosition(it.position) == selectedColorSet },
                    onDismiss = { selectedColorSet = null },
                    onSellProperty = { propertyId ->
                        webSocketClient.logic().sellProperty(propertyId)
                    }
                )
            }
        }
    }
}