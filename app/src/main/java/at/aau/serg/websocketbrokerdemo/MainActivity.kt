//main activity
package at.aau.serg.websocketbrokerdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import at.aau.serg.websocketbrokerdemo.ui.LobbyScreen
import at.aau.serg.websocketbrokerdemo.data.ChatEntry
import at.aau.serg.websocketbrokerdemo.data.CheatEntry
import at.aau.serg.websocketbrokerdemo.data.PlayerProfile
import at.aau.serg.websocketbrokerdemo.data.FirestoreManager
import at.aau.serg.websocketbrokerdemo.ui.SettingsScreen
import at.aau.serg.websocketbrokerdemo.ui.SoundSelectionScreen
import at.aau.serg.websocketbrokerdemo.ui.UserProfileScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import at.aau.serg.websocketbrokerdemo.ui.PlayboardScreen
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.data.messages.DealProposalMessage
import at.aau.serg.websocketbrokerdemo.data.messages.DealResponseMessage
import at.aau.serg.websocketbrokerdemo.data.messages.DealResponseType
import at.aau.serg.websocketbrokerdemo.ui.GameHelp
import at.aau.serg.websocketbrokerdemo.ui.StatisticsScreen
import at.aau.serg.websocketbrokerdemo.ui.LeaderboardScreen
import at.aau.serg.websocketbrokerdemo.ui.WinScreen
import com.example.myapplication.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MonopolyWebSocketApp() }
    }

    @Composable
    fun MonopolyWebSocketApp() {
        val context = LocalContext.current
        fun showToast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        var showHelp by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("") }
        var log by remember { mutableStateOf("Logs:\n") }
        var playerProfile by remember { mutableStateOf<PlayerProfile?>(null) }
        var playerMoneyList by remember { mutableStateOf<List<PlayerMoney>>(emptyList()) }
        val playerCount by remember { derivedStateOf { playerMoneyList.size } }
        var diceValue by remember { mutableStateOf<Int?>(null) }
        var dicePlayer by remember { mutableStateOf<String?>(null) }
        var hasRolled by remember { mutableStateOf(false) }
        var hasPasch by remember { mutableStateOf(false) }
        val cheatFlags = remember { mutableStateMapOf<String, Boolean>() }
        var currentGamePlayerId by remember { mutableStateOf<String?>(null) }
        val chatMessages = remember { mutableStateListOf<ChatEntry>() }
        val cheatMessages = remember { mutableStateListOf<CheatEntry>() }
        var localPlayerId by remember { mutableStateOf<String?>(null) }
        var showPassedGoAlert by remember { mutableStateOf(false) }
        var passedGoPlayerName by remember { mutableStateOf("") }
        var showTaxPaymentAlert by remember { mutableStateOf(false) }
        var taxPaymentPlayerName by remember { mutableStateOf("") }
        var taxPaymentAmount by remember { mutableStateOf(0) }
        var taxPaymentType by remember { mutableStateOf("") }
        var youWon by remember { mutableStateOf(false) }
        var currentDealProposal by remember { mutableStateOf<DealProposalMessage?>(null) }
        var currentDealResponse by remember { mutableStateOf<DealResponseMessage?>(null) }
        var showIncomingDialog by remember { mutableStateOf(false) }
        var drawnCardType by remember { mutableStateOf<String?>(null) }
        var drawnCardId by remember { mutableStateOf<Int?>(null) }
        var drawnCardDesc by remember { mutableStateOf<String?>(null) }
        var shouldNavigateToLobby by remember { mutableStateOf(false) }
        var hasGivenUp by remember { mutableStateOf(false) }
        val avatarMap = remember { mutableStateMapOf<String, Int>() }

        // Firebase Auth instance
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        val availableAvatars = remember {
            mutableStateListOf(
                R.drawable.player_red,
                R.drawable.player_blue,
                R.drawable.player_green,
                R.drawable.player_yellow
            )
        }

        // Show passed GO alert for 3 seconds
        LaunchedEffect(showPassedGoAlert) {
            if (showPassedGoAlert) {
                delay(3000)
                showPassedGoAlert = false
                // After GO alert, show tax alert if needed
                if (showTaxPaymentAlert) {
                    showTaxPaymentAlert = false
                }
            }
        }

        // Show tax payment alert for 3 seconds (only if not triggered by passing GO)
        LaunchedEffect(showTaxPaymentAlert) {
            if (showTaxPaymentAlert && !showPassedGoAlert) {
                delay(3000)
                showTaxPaymentAlert = false
            }
        }

        // Update currentGamePlayerId when game state changes
        LaunchedEffect(playerMoneyList, userId) {
            if (userId != null) {
                // Find the first player that matches our Firebase ID or assign a new one
                currentGamePlayerId = playerMoneyList.find { it.id == userId }?.id
                    ?: playerMoneyList.firstOrNull()?.id
                            ?: userId // Fallback to Firebase ID if no players exist yet
            }
            //Figurzuweisung gleich hier machen
            playerMoneyList.forEach { player ->
                if (avatarMap[player.id] == null && availableAvatars.isNotEmpty()) {
                    avatarMap[player.id] = availableAvatars.removeAt(0)
                }
            }
        }

        LaunchedEffect(userId) {
            if (userId != null) {
                FirestoreManager.listenToUserProfile(userId) { updatedProfile ->
                    playerProfile = updatedProfile
                }
            }
        }

        // Create websocket client
        val gameEvents = remember { mutableStateListOf<String>() }
        val webSocketClient = remember {
            GameWebSocketClient(
                context = context,
                onConnected = { log += "Connected to server\n" },
                onMessageReceived = { msg -> log += "Received: $msg\n" },
                onDiceRolled = { pid, value, manual, isPasch ->
                    dicePlayer = pid
                    diceValue = value
                    cheatFlags[pid] = manual

                    if (pid == localPlayerId) {
                        hasRolled = !isPasch
                        hasPasch = isPasch
                    }
                    if (isPasch && pid == localPlayerId) {
                        gameEvents.add("🎉 Double rolled!!")

                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                "🎲 Double rolled! You can dice again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onHasWon = { winnerId ->
                    if (winnerId == userId) {
                        youWon = true           // just set state here
                    }
                },
                onGameStateReceived = { players ->
                    playerMoneyList = players
                    // (you already had logic for matching firebase ID → session-ID)
                    currentGamePlayerId = players.find { it.id == userId }?.id ?: userId
                },
                onPlayerTurn = { sessionId ->
                    // here's where we grab "my" session-id from the server
                    localPlayerId = sessionId
                    Log.d("WebSocket", "It's now YOUR turn; session ID = $sessionId")
                },
                onChatMessageReceived = { senderId, text ->
                    val senderName = playerMoneyList.find { it.id == senderId }?.name ?: "Unknown"
                    chatMessages.add(ChatEntry(senderId, senderName, text))
                },
                onCheatMessageReceived = { senderId, text ->
                    val senderName = playerMoneyList.find { it.id == senderId }?.name ?: "Unknown"
                    cheatMessages.add(CheatEntry(senderId, senderName, text))
                },
                onPlayerPassedGo = { playerName ->
                    passedGoPlayerName = playerName
                    showPassedGoAlert = true
                },
                onTaxPayment = { playerName, amount, taxType ->
                    taxPaymentPlayerName = playerName
                    taxPaymentAmount = amount
                    taxPaymentType = taxType
                    showTaxPaymentAlert = true
                },
                onCardDrawn = { _, cardType, description, cardId ->
                    // Open the dialog in Compose
                    drawnCardType = cardType
                    drawnCardDesc = description
                    drawnCardId = cardId
                },
                onClearChat = {
                    chatMessages.clear()
                    cheatMessages.clear()
                },
                onDealProposal = { proposal ->
                    currentDealProposal = proposal
                    showIncomingDialog = true
                },
                onDealResponse = { response ->
                    currentDealResponse = response
                    val msg = when (response.responseType) {
                        DealResponseType.ACCEPT -> "✅ Deal accepted"
                        DealResponseType.DECLINE -> "❌ Deal declined"
                        DealResponseType.COUNTER -> "🤝 Counter-proposal sent"
                    }
                    gameEvents.add(msg)
                },
                onGiveUpReceived = { givingUpUserId ->
                    // Only navigate user who has given up, not everyone
                    if (givingUpUserId == userId) {
                        hasGivenUp = true
                        shouldNavigateToLobby = true
                    }
                },

                coroutineDispatcher = Dispatchers.IO
            )
        }

        LaunchedEffect(webSocketClient) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.uid?.let { userId ->
                val profile = FirestoreManager.getUserProfile(userId)
                val name = profile?.name ?: "Unknown"
                val initMessage = """{
                "type": "INIT",
                "userId": "$userId",
                "name": "$name"
            }""".trimIndent()
                webSocketClient.sendMessage(initMessage)
                Log.d("WebSocket", "Sent INIT message: $initMessage")
            }
        }


        val navController = rememberNavController()

        // Go to LobbyScreen
        LaunchedEffect(shouldNavigateToLobby) {
            if (shouldNavigateToLobby) {
                navController.navigate("lobby") {
                    popUpTo("lobby") { inclusive = false }
                }
                shouldNavigateToLobby = false
            }
        }

        // 3) When that state flips, actually navigate:
        LaunchedEffect(youWon) {
            if (youWon) {
                navController.navigate("win")
            }
        }

        NavHost(navController, startDestination = "lobby") {
            composable("lobby") {
                LobbyScreen(
                    message = message,
                    log = log,
                    playerCount = playerCount,
                    onMessageChange = { message = it },
                    onConnect = { webSocketClient.connect()
                        showToast("✅ Connection with the server")},
                    onDisconnect = {
                        webSocketClient.close()
                        log = "Logs:\n" // Clear the log
                        log += "Disconnected from server\n"
                        showToast("⚠️ Disconnected from server.")
                    },
                    onSendMessage = {
                        if (message.isNotEmpty()) {
                            webSocketClient.sendMessage(message)
                            log += "Sent: $message\n"
                            message = ""
                        }
                    },
                    onLogout = {
                        auth.signOut()
                        val intent = Intent(context, AuthActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
                    onProfileClick = { navController.navigate("profile") },
                    onJoinGame = { navController.navigate("playerInfo") },
                    onStatisticsClick = { navController.navigate("statistics") },
                    onLeaderboardClick = { navController.navigate("leaderboard") },

                    onOpenSettings = { navController.navigate("settings") },
                    onOpenSoundSelection = { navController.navigate("soundSelection") },
                    onHelpClick = { showHelp = true }

                )
            }
            composable("win") {
                WinScreen(onTimeout = {
                    navController.popBackStack("lobby", inclusive = false)
                    youWon = false           // reset so you can play again
                })
            }
            composable("profile") {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                var profile by remember { mutableStateOf<PlayerProfile?>(null) }
                DisposableEffect(userId) {
                    if (userId != null) {
                        val listenerRegistration = FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .addSnapshotListener { snapshot, error ->
                                if (error != null) {
                                    Log.e("ProfileScreen", "Listener failed", error)
                                    return@addSnapshotListener
                                }

                                if (snapshot != null && snapshot.exists()) {
                                    profile = snapshot.toObject(PlayerProfile::class.java)
                                }
                            }
                        onDispose {
                            listenerRegistration.remove()
                        }
                    } else {
                        onDispose { /* nothing */ }
                    }
                }

                UserProfileScreen(
                    playerProfile = profile,
                    onNameChange = { newName ->
                        CoroutineScope(Dispatchers.IO).launch {
                            userId?.let {
                                FirestoreManager.updateUserProfileName(it, newName)
                                // Kein manuelles Neuladen nötig – Listener bekommt Update automatisch
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("statistics") {
                StatisticsScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("playerInfo") {
                // Add debug logging for player ID
                android.util.Log.d(
                    "MainActivity",
                    "Passing current game player ID to PlayboardScreen: $currentGamePlayerId"
                )
                android.util.Log.d(
                    "MainActivity",
                    "Passing current game player ID to PlayboardScreen: $currentGamePlayerId"
                )
                android.util.Log.d("MainActivity", "Current game state players: $playerMoneyList")

                PlayboardScreen(
                    players = playerMoneyList,
                    avatarMap = avatarMap,
                    currentPlayerId = currentGamePlayerId ?: "",
                    onRollDice = { webSocketClient.sendMessage("Roll") },
                    onBackToLobby = { navController.navigate("lobby") },
                    diceResult = diceValue,
                    dicePlayerId = dicePlayer,
                    hasRolled = hasRolled,
                    hasPasch = hasPasch,
                    setHasRolled = { hasRolled = it },
                    setHasPasch = { hasPasch = it },
                    cheatFlags = cheatFlags,
                    webSocketClient = webSocketClient,
                    localPlayerId = localPlayerId ?: "",
                    chatMessages = chatMessages,
                    cheatMessages = cheatMessages,
                    showPassedGoAlert = showPassedGoAlert,
                    passedGoPlayerName = passedGoPlayerName,
                    showTaxPaymentAlert = showTaxPaymentAlert,
                    taxPaymentPlayerName = taxPaymentPlayerName,
                    taxPaymentAmount = taxPaymentAmount,
                    taxPaymentType = taxPaymentType,
                    incomingDeal = currentDealProposal,
                    setIncomingDeal = { currentDealProposal = it },
                    showIncomingDialog = showIncomingDialog,
                    setShowIncomingDialog = { showIncomingDialog = it },
                    gameEvents = gameEvents,
                    onGiveUp = {
                        localPlayerId?.let {
                            webSocketClient.logic().sendGiveUpMessage(it)
                            navController.navigate("lobby")
                        }
                    },
                    drawnCardType = drawnCardType,
                    drawnCardId = drawnCardId,
                    drawnCardDesc = drawnCardDesc,
                    onCardDialogDismiss = {
                        drawnCardType = null
                        drawnCardId = null
                        drawnCardDesc = null
                    }
                )
            }
            composable("leaderboard") {
                LeaderboardScreen(
                    onBack = { navController.popBackStack() },
                    currentUsername = playerProfile?.name
                )
            }
            composable("settings") {
                SettingsScreen()
            }
            composable("soundSelection") {
                SoundSelectionScreen()
            }
        }

        if (showHelp) {
            GameHelp(onClose = { showHelp = false })
        }
    }

}
