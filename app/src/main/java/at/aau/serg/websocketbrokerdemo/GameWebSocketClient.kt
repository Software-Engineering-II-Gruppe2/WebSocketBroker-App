package at.aau.serg.websocketbrokerdemo

import android.content.Context
import android.util.Log
import at.aau.serg.websocketbrokerdemo.data.ChatMessage
import at.aau.serg.websocketbrokerdemo.data.DiceRollMessage
import at.aau.serg.websocketbrokerdemo.data.FirestoreManager
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.data.TaxPaymentMessage
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import java.util.Properties

class GameWebSocketClient(
    private val context: Context,
    private val onConnected: () -> Unit,
    private var onMessageReceived: (String) -> Unit,
    private val onDiceRolled: (playerId: String, value: Int) -> Unit,
    private val onGameStateReceived: (List<PlayerMoney>) -> Unit,
    private val onPlayerTurn: (playerId: String) -> Unit,
    private val onPlayerPassedGo: (playerName: String) -> Unit,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val onChatMessageReceived: (playerId: String, message: String) -> Unit,
    private val onTaxPayment: (playerName: String, amount: Int, taxType: String) -> Unit,
) {
    private val client = OkHttpClient()

    // Use a nullable WebSocket so we can check if it's already connected
    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private var players: List<PlayerMoney> = emptyList()

    // Load the server URL from the config.properties file in the assets folder.
    private val serverUrl: String = loadServerUrl(context)

    private val request: Request = Request.Builder()
        .url(serverUrl)
        .build()

    /**
     * Connects to the server if not already connected.
     */
    fun connect() {
        if (webSocket == null) {
            initWebSocket()
            sendInitMessage()
        } else {
            Log.d("WebSocket", "Already connected or connection already exists.")
        }
    }

    // Initializes the WebSocket connection.
    private fun initWebSocket() {
        webSocket = client.newWebSocket(request, createListener())
    }

    private fun sendInitMessage() {
        CoroutineScope(coroutineDispatcher).launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.uid?.let { userId ->
                CoroutineScope(Dispatchers.IO).launch {
                    val profile = FirestoreManager.getUserProfile(userId)
                    val name = profile?.name ?: "Unknown"
                    val initMessage = """{
                    "type": "INIT",
                    "userId": "$userId",
                    "name": "$name"
                }""".trimIndent()
                    webSocket?.send(initMessage)
                    Log.d("WebSocket", "Sent INIT message: $initMessage")
                }
            }
        }
    }

    // Creates a WebSocketListener that handles connection events.
    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "Connected to server with response: ${response.message}")
            onConnected()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "Received: $text")

            // Check for tax payment message
            try {
                val taxMessage = gson.fromJson(text, TaxPaymentMessage::class.java)
                if (taxMessage.type == "TAX_PAYMENT") {
                    val playerName =
                        players.find { it.id == taxMessage.playerId }?.name ?: "Unknown Player"
                    onTaxPayment(playerName, taxMessage.amount, taxMessage.taxType)
                    return
                }
            } catch (_: Exception) { /* not a tax payment message */
            }

            // Check for "passed GO" message
            if (text.contains("passed GO and collected")) {
                val playerId = text.substringAfter("Player ").substringBefore(" passed")
                val playerName = players.find { it.id == playerId }?.name ?: "Unknown Player"
                onPlayerPassedGo(playerName)
            }

            if (text.contains("PROPERTY_BOUGHT")) {
                propertyBoughtListener?.invoke(text)
            }
            // Check if this is a game state message
            if (text.startsWith("GAME_STATE:")) {
                try {
                    val jsonData = text.substring("GAME_STATE:".length)
                    val type = object : TypeToken<List<PlayerMoney>>() {}.type
                    players = gson.fromJson<List<PlayerMoney>>(jsonData, type)
                    onGameStateReceived(players)
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing game state: ${e.message}", e)
                }
            }

            // Player's turn message:
            if (text.startsWith("PLAYER_TURN")) {
                try {
                    // drop the prefix, grab everything after the colon
                    val sessionId = text.substringAfter("PLAYER_TURN:")
                    onPlayerTurn(sessionId)
                    return
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing PLAYER_TURN: ${e.message}", e)
                }
            }

            // try to parse dice‐roll JSON first
            try {
                val obj = gson.fromJson(text, DiceRollMessage::class.java)
                if (obj.type == "DICE_ROLL") {
                    onDiceRolled(obj.playerId, obj.value)
                    return  // don't fall through to the generic log handler
                }
            } catch (_: Exception) { /* not a dice‐roll */
            }

            try {
                val chatMessage = gson.fromJson(text, ChatMessage::class.java)
                if (chatMessage.type == "CHAT_MESSAGE") {

                    onChatMessageReceived(chatMessage.playerId, chatMessage.message)
                    return
                }
            } catch (_: Exception) { /* not a chat-message */
            }

            // Always call the general message handler
            onMessageReceived(text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("WebSocket", "Received bytes: ${bytes.hex()}")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket", "Closing: $reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket", "Closed: $reason")
            // Reset the WebSocket variable when closed.
            this@GameWebSocketClient.webSocket = null
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "Error: ${t.message}", t)
            response?.let {
                Log.e("WebSocket", "Response: ${it.code} - ${it.message}")
            }
            // Optionally, reset the webSocket so that connect() can try again.
            this@GameWebSocketClient.webSocket = null
        }
    }


    fun sendMessage(message: String) {
        webSocket?.send(message)
    }


    fun close() {
        webSocket?.close(1000, "Goodbye!")
        // Reset the connection.
        webSocket = null
    }

    /**
     * Sends a property buy request to the server.
     * @param propertyId The ID of the property to buy
     */
    fun buyProperty(propertyId: Int) {
        val message = "BUY_PROPERTY:$propertyId"
        webSocket?.send(message)
        Log.d("WebSocket", "Sent: $message")
    }

    private var propertyBoughtListener: ((String) -> Unit)? = null

    fun setPropertyBoughtListener(listener: (String) -> Unit) {
        propertyBoughtListener = listener
    }


    private fun loadServerUrl(context: Context): String {
        val properties = Properties()
        context.assets.open("config.properties").use { input ->
            properties.load(input)
        }
        return properties.getProperty("server.url")
    }

    fun sendChatMessage(playerId: String, message: String) {
        val chat = ChatMessage(playerId = playerId, message = message)
        val json = gson.toJson(chat)
        sendMessage(json)
    }

    fun rollDice() {
        webSocket?.send("Roll")
    }

    fun manualRollDice(value: Int) {
        if (value in 1..39) {
            webSocket?.send("MANUAL_ROLL:$value")
        }
    }

    fun sendTaxPayment(playerId: String, amount: Int, taxType: String) {
        val taxMessage = TaxPaymentMessage(
            playerId = playerId,
            amount = amount,
            taxType = taxType
        )
        val json = gson.toJson(taxMessage)
        webSocket?.send(json)
        Log.d("WebSocket", "Sent tax payment message: $json")
    }

    fun sendGiveUpMessage(userId: String) {
        val message = """
        {
            "type": "GIVE_UP",
            "userId": "$userId"
        }
    """.trimIndent()

        webSocket?.send(message)
    }
}