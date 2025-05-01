package at.aau.serg.websocketbrokerdemo.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import kotlinx.coroutines.delay
import at.aau.serg.websocketbrokerdemo.data.properties.Property
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import at.aau.serg.websocketbrokerdemo.data.properties.getDrawableIdFromName
import at.aau.serg.websocketbrokerdemo.GameWebSocketClient
import at.aau.serg.websocketbrokerdemo.data.ChatEntry
import at.aau.serg.websocketbrokerdemo.data.ChatMessage
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyColor

fun extractPlayerId(message: String): String {
    val regex = """Player ([a-f0-9\-]+) bought""".toRegex()
    return regex.find(message)?.groupValues?.get(1) ?: ""
}

fun extractPropertyId(message: String): Int {
    val regex = """property (\d+)""".toRegex()
    return regex.find(message)?.groupValues?.get(1)?.toIntOrNull() ?: -1
}

@Composable
fun PlayboardScreen(
    players: List<PlayerMoney>,
    currentPlayerId: String,
    localPlayerId: String,
    onRollDice: () -> Unit,
    onBackToLobby: () -> Unit,
    diceResult:     Int?,
    dicePlayerId:   String?,
    webSocketClient: GameWebSocketClient,
    chatMessages: List<ChatEntry>
) {
    val context = LocalContext.current
    val propertyViewModel = remember { PropertyViewModel() }
    val properties = remember { mutableStateListOf<Property>().apply { addAll(propertyViewModel.getProperties(context)) } }
    val isMyTurn = currentPlayerId == localPlayerId

    var selectedProperty by remember { mutableStateOf<Property?>(null) }
    var canBuy by remember { mutableStateOf(false) }
    var openedByClick by remember { mutableStateOf(false) }
    var lastPlayerPosition by remember { mutableStateOf<Int?>(null) }
    var chatOpen by remember{ mutableStateOf(false)}
    var chatInput by remember { mutableStateOf("") }
    val nameColors = listOf(
        Color(0xFFE57373), // Rot
        Color(0xFF64B5F6), // Blau
        Color(0xFF81C784), // Grün
        Color(0xFFFFD54F)  // Gelb
    )

    val playerColorMap = players
        .mapIndexed { index, player -> player.id to nameColors[index % nameColors.size] }
        .toMap()



    LaunchedEffect(players, dicePlayerId) {
        val currentPlayer = players.find { it.id == dicePlayerId }
        val newPosition = currentPlayer?.position

        if (newPosition != null && newPosition != lastPlayerPosition) {
            lastPlayerPosition = newPosition
            val landedProperty = properties.find { it.position == newPosition }
            if (landedProperty != null) {
                selectedProperty = landedProperty
                openedByClick = false
                canBuy = true
            }
        }
    }

    LaunchedEffect(Unit) {
        webSocketClient.setPropertyBoughtListener { message ->
            val playerId = extractPlayerId(message)
            val propertyId = extractPropertyId(message)

            properties.find { it.id == propertyId }?.let { property ->
                property.ownerId = playerId
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8F3DC)) // Light green background
    ) {
        // Main content area (70% of screen width)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.6f)
                .fillMaxHeight(0.9f)
                .padding(end = 16.dp)
        ) {
            Gameboard(
                modifier = Modifier.fillMaxSize(),
                players = players,
                properties = properties,
                onTileClick = { tilePos ->
                    // Find the player who rolled the dice (dicePlayerId)
                    val currentPlayer = players.find { it.id == dicePlayerId }
                    selectedProperty = properties.find { it.position == tilePos }
                    openedByClick = true
                    canBuy = currentPlayer?.position == tilePos
                }
            )
        }

        // Dice info column on the left (10% of screen width)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, top = 16.dp)
                .fillMaxWidth(0.1f)
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DiceRollingButton(
                text = "Roll Dice",
                color = Color(0xFF3FAF3F),
                onClick = onRollDice,
                diceValue = diceResult,
                enabled   = isMyTurn
            )
        }

        // Player info column on the right (20% of screen width)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.2f)
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (players.isEmpty()) {
                Text(
                    text = "Not enough players connected yet",
                    color = Color.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(players.take(4)) { player ->
                        PlayerCard(
                            player = player,
                            ownedProperties = properties.filter { it.ownerId == player.id },
                            isCurrentPlayer = player.id == currentPlayerId,
                            playerIndex = players.indexOf(player),
                            onPropertySetClicked = { colorSet ->
                                println("Clicked on color set: $colorSet")
                            },
                            allProperties = properties,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBackToLobby,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0074cc)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Back to Lobby", fontSize = 18.sp)
            }
        }
        // Popup für Grundstück
        if (selectedProperty != null) {
            val imageResId = getDrawableIdFromName(selectedProperty!!.image, context)

            AlertDialog(
                modifier = Modifier
                    .width(300.dp)
                    .height(400.dp),
                onDismissRequest = {
                    selectedProperty = null
                    openedByClick = false
                    canBuy = false
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedProperty!!.name,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (imageResId != 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(imageResId),
                                    contentDescription = selectedProperty!!.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(240.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                selectedProperty = null
                                openedByClick = false
                                canBuy = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0074cc))
                        ) {
                            Text("Exit")
                        }
                        if (canBuy) {
                            Button(
                                onClick = {
                                    webSocketClient.sendMessage("BUY_PROPERTY:${selectedProperty?.id}")
                                    selectedProperty = null
                                    openedByClick = false
                                    canBuy = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0074cc))
                            ) {
                                Text("Buy")
                            }
                        }
                    }
                },
                dismissButton = {}
            )
        }

        // Chat Open/Close Button (immer sichtbar, unten rechts)
        Button(

            onClick = { chatOpen = !chatOpen },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0074cc)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(if (chatOpen) "Close Chat" else "Open Chat", fontSize = 16.sp)
        }

// Chat Overlay

        if (chatOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)) // halbtransparenter schwarzer Hintergrund
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    // Nachrichtenliste
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                        reverseLayout = true
                    ) {
                        items(chatMessages.reversed()) { entry ->
                            val isOwnMessage = entry.senderId == currentPlayerId

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isOwnMessage) Color(0xFFDCF8C6) else Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp)
                                        .widthIn(max = 240.dp)
                                ) {
                                    Column {
                                        val nameColor = playerColorMap[entry.senderId] ?: Color.Gray
                                        Text(
                                            text = entry.senderName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = nameColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = entry.message,
                                            color = Color.Black,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Eingabe und Senden
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type your message...") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    webSocketClient.sendChatMessage(currentPlayerId, chatInput)
                                    chatInput = "" // Nach Senden Eingabefeld leeren
                                }
                            }
                        ) {
                            Text("Send")
                        }
                    }
                }
            }
        }


    }
}

@Composable
fun PlayerCard(
    player: PlayerMoney,
    ownedProperties: List<Property>,
    allProperties: List<Property>,
    isCurrentPlayer: Boolean,
    playerIndex: Int,
    onPropertySetClicked: (PropertyColor) -> Unit
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
            .wrapContentHeight(),
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

            Divider(
                color = Color.White,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            BesitzkartenGrid(
                ownedProperties = ownedProperties,
                allProperties = allProperties,
                onPropertySetClicked = { colorSet -> selectedColorSet = colorSet }
            )

            if (selectedColorSet != null) {
                PropertySetPopup(
                    colorSet = selectedColorSet!!,
                    ownedProperties = ownedProperties,
                    allProperties = allProperties,
                    onDismiss = { selectedColorSet = null }
                )
            }
        }
    }
}

@Composable
fun BesitzkartenGrid(
    ownedProperties: List<Property>,
    allProperties: List<Property>,
    onPropertySetClicked: (PropertyColor) -> Unit
) {
    val propertySets = PropertyColor.values()

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .heightIn(max = 120.dp)
    ) {
        items(propertySets.size) { index ->
            val colorSet = propertySets[index]
            PropertySetCard(
                colorSet = colorSet,
                ownedProperties = ownedProperties,
                allProperties = allProperties,
                onClick = { onPropertySetClicked(colorSet) }
            )
        }
    }
}

@Composable
fun PropertySetCard(
    colorSet: PropertyColor,
    ownedProperties: List<Property>,
    allProperties: List<Property>,
    onClick: () -> Unit
) {
    val propertiesInSet = ownedProperties.filter { getColorForPosition(it.position) == colorSet }
    val ownsCompleteSet = checkCompleteSet(colorSet, propertiesInSet, allProperties)

    val cardAlpha = if (propertiesInSet.isEmpty()) 0.3f else if (ownsCompleteSet) 1f else 0.6f

    Card(
        modifier = Modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = getColorForSet(colorSet).copy(alpha = cardAlpha)
        )
    ) {}
}

fun checkCompleteSet(colorSet: PropertyColor, owned: List<Property>, allProperties: List<Property>): Boolean {
    val totalInSet = allProperties.count { getColorForPosition(it.position) == colorSet }
    return owned.size == totalInSet
}

fun getColorForSet(colorSet: PropertyColor): Color {
    return when (colorSet) {
        PropertyColor.BROWN -> Color(0xFF964B00)
        PropertyColor.LIGHT_BLUE -> Color(0xFFADD8E6)
        PropertyColor.PINK -> Color(0xFFFFC0CB)
        PropertyColor.ORANGE -> Color(0xFFFFA500)
        PropertyColor.RED -> Color.Red
        PropertyColor.YELLOW -> Color.Yellow
        PropertyColor.GREEN -> Color.Green
        PropertyColor.DARK_BLUE -> Color(0xFF00008B)
        PropertyColor.RAILROAD -> Color(0xFF8B4513)
        PropertyColor.UTILITY -> Color (0xFF20B2AA)
        PropertyColor.NONE -> Color (0xFFA9A9A9)
    }
}

fun getColorForPosition(position: Int): PropertyColor {
    return when (position) {
        1, 3 -> PropertyColor.BROWN
        6, 8, 9 -> PropertyColor.LIGHT_BLUE
        11, 13, 14 -> PropertyColor.PINK
        16, 18, 19 -> PropertyColor.ORANGE
        21, 23, 24 -> PropertyColor.RED
        26, 27, 29 -> PropertyColor.YELLOW
        31, 32, 34 -> PropertyColor.GREEN
        37, 39 -> PropertyColor.DARK_BLUE
        5, 15, 25, 35 -> PropertyColor.RAILROAD
        12, 28 -> PropertyColor.UTILITY
        else -> PropertyColor.NONE
    }
}

@Composable
fun PropertySetPopup(
    colorSet: PropertyColor,
    ownedProperties: List<Property>,
    allProperties: List<Property>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val propertiesInSet = allProperties.filter { getColorForPosition(it.position) == colorSet }

    AlertDialog(
        modifier = Modifier
            .width(420.dp)
            .height(400.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${colorSet.name} Set",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                propertiesInSet.forEach { property ->
                    val imageResId = getDrawableIdFromName(property.image, context)

                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .aspectRatio(0.7f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Color.LightGray.copy(alpha = 1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = property.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(if (property.ownerId != null) 1f else 0.4f)
                            )
                        } else {
                            Text(
                                text = property.name,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0074cc)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Exit", color = Color.White)
            }
        },
        dismissButton = {}
    )
}


@Composable
fun DiceRollingButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    diceValue: Int?,
    enabled: Boolean = true
) {

    var isPressed by remember { mutableStateOf(false) }
    var rotateAngle by remember { mutableFloatStateOf(0f) }

    val rotation by animateFloatAsState(
        targetValue = rotateAngle,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    val scale by animateFloatAsState(if (isPressed && enabled) 1.1f else 1f, animationSpec = tween(150))
    val buttonColor by animateColorAsState(
        targetValue = when {
            !enabled      -> Color.Gray
            isPressed     -> color.copy(alpha = 0.7f)
            else          -> color
            },
        animationSpec = tween(durationMillis = 150)
    )

    Button(
        onClick = {
            if (!enabled) return@Button
            isPressed = true
            rotateAngle += 720f
            onClick()
            },
        enabled = enabled,
        modifier = Modifier.height(56.dp).scale(scale).rotate(rotation),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Text(text, fontSize = 18.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    // show the face
    DiceFace(diceValue)

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(1000)
            isPressed = false
        }
    }
}

@Composable
fun DiceFace(diceValue: Int?) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = diceValue?.toString() ?: "?",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
