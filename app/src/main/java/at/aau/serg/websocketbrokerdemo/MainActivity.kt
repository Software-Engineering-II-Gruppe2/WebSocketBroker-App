package at.aau.serg.websocketbrokerdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import at.aau.serg.websocketbrokerdemo.data.PlayerProfile
import at.aau.serg.websocketbrokerdemo.data.FirestoreManager
import at.aau.serg.websocketbrokerdemo.ui.UserProfileScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import at.aau.serg.websocketbrokerdemo.ui.PlayboardScreen
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.ui.StatisticsScreen
import at.aau.serg.websocketbrokerdemo.ui.LeaderboardScreen
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MonopolyWebSocketApp() }
    }

    @Composable
    fun MonopolyWebSocketApp() {
        val context = LocalContext.current
        var message by remember { mutableStateOf("") }
        var log by remember { mutableStateOf("Logs:\n") }
        var playerProfile by remember { mutableStateOf<PlayerProfile?>(null) }
        var playerMoneyList by remember { mutableStateOf<List<PlayerMoney>>(emptyList()) }
        var diceValue by remember { mutableStateOf<Int?>(null) }
        var dicePlayer by remember { mutableStateOf<String?>(null) }
        var currentGamePlayerId by remember { mutableStateOf<String?>(null) }
        val chatMessages = remember { mutableStateListOf<ChatEntry>() }
        var localPlayerId by remember { mutableStateOf<String?>(null) }
        var showPassedGoAlert by remember { mutableStateOf(false) }
        var passedGoPlayerName by remember { mutableStateOf("") }
        var showTaxPaymentAlert by remember { mutableStateOf(false) }
        var taxPaymentPlayerName by remember { mutableStateOf("") }
        var taxPaymentAmount by remember { mutableStateOf(0) }
        var taxPaymentType by remember { mutableStateOf("") }

        // Firebase Auth instance
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

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
        }

        LaunchedEffect(userId) {
            if (userId != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    playerProfile = FirestoreManager.getUserProfile(userId)
                }
            }
        }

        // Create websocket client
        val webSocketClient = remember {
            GameWebSocketClient(
                context = context,
                onConnected = { log += "Connected to server\n" },
                onMessageReceived = { msg -> log += "Received: $msg\n" },
                onDiceRolled = { pid, value -> dicePlayer = pid; diceValue = value },
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
                onCardDrawn = { _, cardType, description ->
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast
                            .makeText(
                                context,
                                "You drew a $cardType card: $description",
                                Toast.LENGTH_LONG
                            )
                            .show()
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

        NavHost(navController, startDestination = "lobby") {
            composable("lobby") {
                LobbyScreen(
                    message = message,
                    log = log,
                    onMessageChange = { message = it },
                    onConnect = { webSocketClient.connect() },
                    onDisconnect = {
                        webSocketClient.close()
                        log = "Logs:\n" // Clear the log
                        log += "Disconnected from server\n"
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
                    onLeaderboardClick = { navController.navigate("leaderboard") }
                )
            }
            composable("profile") {
                UserProfileScreen(
                    playerProfile = playerProfile,
                    onNameChange = { newName ->
                        CoroutineScope(Dispatchers.IO).launch {
                            userId?.let { FirestoreManager.updateUserProfileName(it, newName) }
                            playerProfile = playerProfile?.copy(name = newName)
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
                    currentPlayerId = currentGamePlayerId ?: "",
                    onRollDice = { webSocketClient.sendMessage("Roll") },
                    onBackToLobby = { navController.navigate("lobby") },
                    diceResult = diceValue,
                    dicePlayerId = dicePlayer,
                    webSocketClient = webSocketClient,
                    localPlayerId = localPlayerId ?: "",
                    chatMessages = chatMessages,
                    showPassedGoAlert = showPassedGoAlert,
                    passedGoPlayerName = passedGoPlayerName,
                    showTaxPaymentAlert = showTaxPaymentAlert,
                    taxPaymentPlayerName = taxPaymentPlayerName,
                    taxPaymentAmount = taxPaymentAmount,
                    taxPaymentType = taxPaymentType,
                    onGiveUp = {
                        localPlayerId?.let {
                            webSocketClient.sendGiveUpMessage(it)
                            webSocketClient.close()
                            navController.navigate("lobby")
                        }
                    }
                )
            }
            composable("leaderboard") {
                LeaderboardScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

}

