package at.aau.serg.websocketbrokerdemo.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import kotlinx.coroutines.delay
import at.aau.serg.websocketbrokerdemo.data.properties.Property
import at.aau.serg.websocketbrokerdemo.data.properties.PropertyViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.TextStyle

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType

import at.aau.serg.websocketbrokerdemo.ui.components.TurnTimer

import androidx.compose.ui.zIndex
import at.aau.serg.websocketbrokerdemo.GameWebSocketClient
import at.aau.serg.websocketbrokerdemo.data.ChatEntry
import at.aau.serg.websocketbrokerdemo.data.CheatEntry

import at.aau.serg.websocketbrokerdemo.data.messages.DealProposalMessage
import at.aau.serg.websocketbrokerdemo.data.messages.DealResponseMessage
import at.aau.serg.websocketbrokerdemo.data.messages.DealResponseType
import at.aau.serg.websocketbrokerdemo.data.messages.ShakeMessage
import at.aau.serg.websocketbrokerdemo.logic.ShakeDetector
import com.google.gson.Gson

import at.aau.serg.websocketbrokerdemo.ui.components.DiceRollingButton
import at.aau.serg.websocketbrokerdemo.ui.components.EnableImmersiveMode
import at.aau.serg.websocketbrokerdemo.ui.components.PlayerCard
import at.aau.serg.websocketbrokerdemo.ui.components.dialogs.ActionMenuDialog
import at.aau.serg.websocketbrokerdemo.ui.components.alerts.CardPopup
import at.aau.serg.websocketbrokerdemo.ui.components.alerts.ChatOverlay
import at.aau.serg.websocketbrokerdemo.ui.components.alerts.CheatTerminalOverlay
import at.aau.serg.websocketbrokerdemo.ui.components.alerts.PassedGoAlertBox
import at.aau.serg.websocketbrokerdemo.ui.components.alerts.PropertyPopup
import at.aau.serg.websocketbrokerdemo.ui.components.alerts.TaxPaymentAlertBox


fun extractPlayerId(message: String): String {
    val regex = """Player ([\w-]+) bought""".toRegex()
    return regex.find(message)?.groupValues?.get(1) ?: ""
}

fun extractPropertyId(message: String): Int {
    val regex = """property (\d+)""".toRegex()
    return regex.find(message)?.groupValues?.get(1)?.toIntOrNull() ?: -1
}

@Composable
fun PlayboardScreen(
    players: List<PlayerMoney>,
    avatarMap: Map<String, Int>,
    currentPlayerId: String,
    gameEvents: SnapshotStateList<String>,
    localPlayerId: String,
    onRollDice: () -> Unit,
    onBackToLobby: () -> Unit,
    diceResult:     Int?,
    dicePlayerId:   String?,
    hasRolled: Boolean,
    hasPasch: Boolean,
    setHasRolled: (Boolean) -> Unit,
    setHasPasch: (Boolean) -> Unit,
    webSocketClient: GameWebSocketClient,
    chatMessages: List<ChatEntry>,
    cheatMessages: List<CheatEntry>,
    showPassedGoAlert: Boolean,
    passedGoPlayerName: String,
    showTaxPaymentAlert: Boolean,
    taxPaymentPlayerName: String,
    taxPaymentAmount: Int,
    taxPaymentType: String,
    cheatFlags: Map<String, Boolean>,
    incomingDeal: DealProposalMessage?,
    showIncomingDialog: Boolean,
    setIncomingDeal: (DealProposalMessage?) -> Unit,
    setShowIncomingDialog: (Boolean) -> Unit,
    onGiveUp: () -> Unit = {},
    drawnCardType: String?,         // "CHANCE" or "COMMUNITY_CHEST"
    drawnCardId:   Int?,            // e.g. 1..8
    drawnCardDesc: String?,         // the description (fallback) if drawable not found
    onCardDialogDismiss: () -> Unit // called to clear the popup
) {
    EnableImmersiveMode()
    val context = LocalContext.current
    val propertyViewModel = remember { PropertyViewModel() }
    val properties = remember {
        mutableStateListOf<Property>().apply {
            addAll(
                propertyViewModel.getProperties(context)
            )
        }
    }

    val turnId = webSocketClient.currentTurnId.collectAsState().value
    val turnPlayer = players.firstOrNull { it.id == turnId }
    val isMyTurnEvenIfBot = turnId == localPlayerId
    val isBotTurn = turnPlayer?.bot == true
    val isMyTurn = isMyTurnEvenIfBot && !isBotTurn


    var turnEnded by remember { mutableStateOf(false) }
    var selectedProperty by remember { mutableStateOf<Property?>(null) }
    var canBuy by remember { mutableStateOf(false) }
    var openedByClick by remember { mutableStateOf(false) }
    var lastPlayerPosition by remember { mutableStateOf<Int?>(null) }
    var manualDiceValue by remember { mutableStateOf("") }
    var chatOpen by remember { mutableStateOf(false) }
    var cheatTerminalOpen by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    var cheatInput by remember { mutableStateOf("") }
    var rentPaid by remember { mutableStateOf(false) }
    val amInJail = players.find { it.id == localPlayerId }?.inJail ?: false
    var timeLeft by remember { mutableStateOf<Int?>(null) }
    val nameColors = listOf(
        Color(0xFFE57373), // Rot
        Color(0xFF64B5F6), // Blau
        Color(0xFF81C784), // Grün
        Color(0xFFFFD54F)  // Gelb
    )

    val playerColorMap = players
        .mapIndexed { index, player -> player.id to nameColors[index % nameColors.size] }
        .toMap()
    var showDealDialog by remember { mutableStateOf(false) }
    var selectedReceiver by remember { mutableStateOf<PlayerMoney?>(null) }

    var isCountering by remember { mutableStateOf(false) }

    var ownedProperties by remember { mutableStateOf<List<Property>>(emptyList()) }

    var showActionMenu by remember { mutableStateOf(false) }


    // ShakeDetector:
    ShakeDetector(shakingThreshold = 15f) {
        val shakeMsg = ShakeMessage(playerId = localPlayerId)
        val json = Gson().toJson(shakeMsg)
        webSocketClient.sendMessage(json)
    }

    // Update owned properties when properties or players change
    LaunchedEffect(properties, players) {
        ownedProperties = properties.filter { it.ownerId == localPlayerId }
    }

    LaunchedEffect(Unit) {
        webSocketClient.setDealProposalListener {
            setIncomingDeal(it)
            setShowIncomingDialog(true)
        }
    }

    LaunchedEffect(Unit) {
        webSocketClient.setPlayerInJailListener { playerId ->
            if (playerId == localPlayerId) {
                val msg = "🚓 You ended up in prison!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                gameEvents.add(msg)
            }
        }
    }

    LaunchedEffect(players, dicePlayerId) {
        val currentPlayer = players.find { it.id == dicePlayerId }
        val newPosition = currentPlayer?.position

        if (newPosition != null && newPosition != lastPlayerPosition) {
            lastPlayerPosition = newPosition

            // Check for tax squares
            when (newPosition) {
                4 -> { // Einkommensteuer
                    if (showPassedGoAlert) {
                        delay(3000) // Wait for GO alert to finish
                    }
                    webSocketClient.logic().sendTaxPayment(
                        playerId = currentPlayer.id,
                        amount = 200,
                        taxType = "EINKOMMENSTEUER"
                    )
                }

                38 -> { // Zusatzsteuer
                    if (showPassedGoAlert) {
                        delay(3000) // Wait for GO alert to finish
                    }
                    webSocketClient.logic().sendTaxPayment(
                        playerId = currentPlayer.id,
                        amount = 100,
                        taxType = "ZUSATZSTEUER"
                    )
                }
            }

            when (newPosition) {
                2, 17, 7, 22, 33, 36 -> {
                    webSocketClient.logic().sendPullCard(currentPlayer.id, newPosition)
                }
            }

            val landedProperty = properties.find { it.position == newPosition }
            if (landedProperty != null) {
                // If player passed GO, delay showing property card
                if (showPassedGoAlert) {
                    delay(3000) // Wait for GO notification to disappear
                }
                selectedProperty = landedProperty
                openedByClick = false
                val isDicePlayerHuman = dicePlayerId == localPlayerId && !isBotTurn
                canBuy = isDicePlayerHuman && landedProperty.ownerId == null
            }
        }
    }

    LaunchedEffect(Unit) {
        webSocketClient.setPropertyBoughtListener { message ->
            val playerId = extractPlayerId(message)
            val propertyId = extractPropertyId(message)

            val index = properties.indexOfFirst { it.id == propertyId }
            if (index != -1) {
                val updated = properties[index].copyWithOwner(playerId)
                val newList = properties.toMutableList()
                newList[index] = updated
                properties.clear()
                properties.addAll(newList)
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
                onTileClick = { tilePos ->
                    // Find the player who rolled the dice (dicePlayerId)
                    val currentPlayer = players.find { it.id == dicePlayerId }
                    selectedProperty = properties.find { it.position == tilePos }
                    openedByClick = true
                    canBuy = isMyTurn &&
                            (currentPlayer?.position == tilePos) &&
                            (selectedProperty?.ownerId == null)
                },
                cheatFlags = cheatFlags,
                players = players,
                avatarMap = avatarMap
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
            val diceEnabled = isMyTurn && !amInJail && (!hasRolled || hasPasch)
            DiceRollingButton(
                text = "Roll Dice",
                color = if (diceEnabled) Color(0xFF3FAF3F) else Color.Gray,
                onClick = onRollDice,
                diceValue = diceResult,
                enabled = diceEnabled,
            )

            // Manual Dice Roll Section
            if (isMyTurn) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = manualDiceValue,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toIntOrNull() in 1..39) {
                            manualDiceValue = newValue
                        }
                    },
                    label = { Text("Manual Dice (1-39)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )

                Button(
                    onClick = {
                        manualDiceValue.toIntOrNull()?.let { value ->
                            webSocketClient.logic().manualRollDice(value)
                            manualDiceValue = ""
                        }
                    },
                    enabled = manualDiceValue.toIntOrNull() in 1..39,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Custom Dice", fontSize = 10.sp)
                }
            }
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
                            allProperties = properties,
                            isCurrentPlayer = (player.id == turnId),
                            playerIndex = players.indexOf(player),
                            onPropertySetClicked = { colorSet ->
                                println("Clicked on color set: $colorSet")
                            },
                            webSocketClient = webSocketClient
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            val endTurnEnabled = isMyTurn && !turnEnded && (hasRolled || amInJail)

            if (isMyTurn) {
                Button(
                    onClick = {
                        webSocketClient.sendMessage("NEXT_TURN")
                        turnEnded = true
                    },
                    enabled = endTurnEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (endTurnEnabled) Color(0xFF0074cc)
                        else Color.Gray      // deaktiviert
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("End Turn", fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { showActionMenu = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Action Menu", fontSize = 18.sp, color = Color.White)
            }
        }

        // Reset wenn neuer Zug
        LaunchedEffect(turnId) {
            if (isMyTurn) {
                turnEnded = false
                setHasRolled(false)
                setHasPasch(false)
                rentPaid = false
            }
        }

        // Timer
        LaunchedEffect(turnId) {
            // nur starten, wenn jemand dran ist, der kein Bot ist
            if (turnId != null && !players.first { it.id == turnId }.bot) {
                timeLeft = 30
                while (timeLeft!! > 0) {
                    delay(1_000)
                    timeLeft = timeLeft!! - 1
                }
            } else {
                timeLeft = null                     // Bots brauchen keinen Timer
            }
        }

        // ACTION MENU
        if (showActionMenu) {
            ActionMenuDialog(
                isMyTurn = isMyTurn,
                turnEnded = turnEnded,
                onDismiss = { showActionMenu = false },
                onStartDeal = { showDealDialog = true },
                onBackToLobby = onBackToLobby,
                onGiveUp = onGiveUp
            )
        }

        // Popup für Grundstück
        if (selectedProperty != null) {
            // Automatisch nach 1,5 Sekunden schließen – nur für Spieler, die NICHT dran sind
            LaunchedEffect(selectedProperty, localPlayerId == currentPlayerId) {
                if (selectedProperty != null && localPlayerId != currentPlayerId && !openedByClick) {
                    delay(1500)
                    selectedProperty = null
                    canBuy = false
                }
            }

            PropertyPopup(
                selectedProperty = selectedProperty!!,
                canBuy = canBuy,
                isMyTurn = currentPlayerId == localPlayerId,
                openedByClick = openedByClick,
                localPlayerId = localPlayerId,
                gameEvents = gameEvents,
                webSocketClient = webSocketClient,
                onDismiss = {
                    selectedProperty = null
                    openedByClick = false
                    canBuy = false
                }
            )

            // Passed GO Alert
            if (showPassedGoAlert) {
                PassedGoAlertBox(playerName = passedGoPlayerName)
            }

            // Tax Payment Alert
            if (showTaxPaymentAlert) {
                TaxPaymentAlertBox(
                    playerName = taxPaymentPlayerName,
                    amount = taxPaymentAmount,
                    taxType = taxPaymentType
                )
            }
            // Tax Payment Alert
            if (showTaxPaymentAlert) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.3f)
                        .background(Color(0xFFFFA500).copy(alpha = 0.9f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Autsch!",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$taxPaymentPlayerName muss ${taxPaymentAmount}€ $taxPaymentType an die Bank zahlen!",
                            style = TextStyle(
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cheat-terminal toggle
                Button(
                    onClick = { cheatTerminalOpen = !cheatTerminalOpen },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3933cc)),
                    modifier = Modifier
                        .width(140.dp)
                        .height(48.dp)
                ) {
                    Text(
                        if (cheatTerminalOpen) "Close Terminal" else "Open Terminal",
                        fontSize = 12.sp
                    )
                }

                // Chat toggle
                Button(
                    onClick = { chatOpen = !chatOpen },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0074cc)),
                    modifier = Modifier
                        .width(140.dp)
                        .height(48.dp)
                ) {
                    Text(
                        if (chatOpen) "Close Chat" else "Open Chat",
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Chat Overlay

        if (chatOpen) {
            ChatOverlay(
                chatMessages = chatMessages,
                chatInput = chatInput,
                onChatInputChange = { chatInput = it },
                currentPlayerId = currentPlayerId,
                onSendMessage = {
                    if (chatInput.isNotBlank()) {
                        webSocketClient.logic().sendChatMessage(currentPlayerId, chatInput)
                        chatInput = ""
                    }
                },
                playerColorMap = playerColorMap,
                onClose = { chatOpen = false }
            )
        }

        // DEALS

        if (showDealDialog) {
            DealDialog(
                players = players.filter { it.id != localPlayerId },
                senderId = localPlayerId,
                allProperties = properties,
                receiver = selectedReceiver,
                avatarMap = avatarMap,
                onReceiverChange = { selectedReceiver = it },
                onSendDeal = { deal ->
                    val json = Gson().toJson(deal)
                    webSocketClient.sendMessage(json)
                    showDealDialog = false
                    selectedReceiver = null
                },
                onDismiss = {
                    showDealDialog = false
                    selectedReceiver = null
                }
            )
        }

        if (showIncomingDialog && incomingDeal != null) {
            val receiverProps = properties.filter { it.ownerId == localPlayerId }.map { it.id }

            IncomingDealDialog(
                proposal = incomingDeal,
                senderName = players.find { it.id == incomingDeal.fromPlayerId }?.name ?: "???",
                allProperties = properties,
                receiverProperties = receiverProps,
                isMyTurn = isMyTurn,
                onAccept = {
                    val response = DealResponseMessage(
                        type = "DEAL_RESPONSE",
                        fromPlayerId = localPlayerId,
                        toPlayerId = incomingDeal.fromPlayerId,
                        responseType = DealResponseType.ACCEPT,
                        counterPropertyIds = listOf(),
                        counterMoney = 0
                    )
                    webSocketClient.sendMessage(Gson().toJson(response))
                    setShowIncomingDialog(false)
                    setIncomingDeal(null)
                },
                onDecline = {
                    val response = DealResponseMessage(
                        type = "DEAL_RESPONSE",
                        fromPlayerId = localPlayerId,
                        toPlayerId = incomingDeal.fromPlayerId,
                        responseType = DealResponseType.DECLINE,
                        counterPropertyIds = listOf(),
                        counterMoney = 0
                    )
                    webSocketClient.sendMessage(Gson().toJson(response))
                    setShowIncomingDialog(false)
                    setIncomingDeal(null)
                },
                onCounter = {
                    isCountering = true
                    setShowIncomingDialog(false)
                }
            )
        }

        if (isCountering && incomingDeal != null) {
            DealDialog(
                players = players.filter { it.id != localPlayerId },
                senderId = localPlayerId,
                allProperties = properties,
                receiver = players.find { it.id == incomingDeal.fromPlayerId },
                avatarMap = avatarMap,
                initialRequested = incomingDeal.offeredPropertyIds,
                initialOffered = incomingDeal.requestedPropertyIds,
                initialMoney = incomingDeal.offeredMoney,
                onSendDeal = { counter ->
                    val counterProposal = DealProposalMessage(
                        type = "DEAL_PROPOSAL",
                        fromPlayerId = localPlayerId,
                        toPlayerId = incomingDeal.fromPlayerId,
                        offeredPropertyIds = counter.requestedPropertyIds,
                        requestedPropertyIds = counter.offeredPropertyIds,
                        offeredMoney = counter.offeredMoney
                    )
                    webSocketClient.sendMessage(Gson().toJson(counterProposal))
                    isCountering = false
                    setIncomingDeal(null)
                },
                onDismiss = {
                    isCountering = false
                    setIncomingDeal(null)
                }
            )
        }

        // Cheat Terminal Overview
        if (cheatTerminalOpen) {
            CheatTerminalOverlay(
                cheatMessages = cheatMessages,
                cheatInput = cheatInput,
                onCheatInputChange = { cheatInput = it },
                onSendCheat = {
                    if (cheatInput.isNotBlank()) {
                        webSocketClient.logic().sendCheatMessage(currentPlayerId, cheatInput)
                        cheatInput = ""
                    }
                },
                onClose = { cheatTerminalOpen = false }
            )
        }
        // Popup for CHANCE and COMMUNITY_CHEST
        if (drawnCardType != null && drawnCardId != null) {
            CardPopup(
                cardType = drawnCardType,
                cardId = drawnCardId,
                cardDesc = drawnCardDesc,
                onDismiss = onCardDialogDismiss
            )
        }

        timeLeft?.let { secs ->
            TurnTimer(
                seconds = secs,
                modifier = Modifier
                    .align(Alignment.TopCenter)   // zentriert oben
                    .padding(top = 6.dp)
                    .zIndex(3f)                   // höher als alles andere
            )

        }
    }
}
