package at.aau.serg.websocketbrokerdemo

import android.content.Context
import android.util.Log
import at.aau.serg.websocketbrokerdemo.data.ChatMessage
import at.aau.serg.websocketbrokerdemo.data.DiceRollMessage
import at.aau.serg.websocketbrokerdemo.data.FirestoreManager
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.data.PullCardMessage
import at.aau.serg.websocketbrokerdemo.data.TaxPaymentMessage
import at.aau.serg.websocketbrokerdemo.data.SoundEffectMessage
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
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
    private val onCardDrawn: (playerId: String, cardType: String, description: String) -> Unit,
    private val onTaxPayment: (playerName: String, amount: Int, taxType: String) -> Unit,
) {
    private val client = OkHttpClient()
    // Use a nullable WebSocket so we can check if it's already connected
    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private var players: List<PlayerMoney> = emptyList()
    private var onPlayerTurnListener: ((String) -> Unit)? = null
    private var propertyBoughtListener: ((String) -> Unit)? = null

    // Load the server URL from the config.properties file in the assets folder.
    private val serverUrl: String = loadServerUrl(context)

    private val request: Request = Request.Builder()
        .url(serverUrl)
        .build()

    // Class for parsing messages:
    private val messageParser = MessageParser(
        gson = gson,
        getPlayers = { players },
        onTaxPayment = { name, amount, type -> onTaxPayment(name, amount, type) },
        onPlayerPassedGo = { name -> onPlayerPassedGo(name) },
        onPropertyBought = { raw -> propertyBoughtListener?.invoke(raw) },
        onGameStateReceived = { state ->
            players = state
            onGameStateReceived(state)
        },
        onPlayerTurn = { sessionId ->
            onPlayerTurn(sessionId)
            onPlayerTurnListener?.invoke(sessionId)
        },
        onDiceRolled = { pid, v -> onDiceRolled(pid, v) },
        onCardDrawn = { pid, type, desc -> onCardDrawn(pid, type, desc) },
        onChatMessageReceived = { pid, msg -> onChatMessageReceived(pid, msg) },
        onMessageReceived = { text -> onMessageReceived(text) }
    )

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

        override fun onMessage(ws: WebSocket, text: String) {
            Log.d("WebSocket", "Received: $text")
            messageParser.parse(text)
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
        sendMessage(message)
        Log.d("WebSocket", "Sent: $message")
    }

    fun setPropertyBoughtListener(listener: (String) -> Unit) {
        propertyBoughtListener = listener
    }

    fun setOnPlayerTurnListener(listener: (String) -> Unit) {
        onPlayerTurnListener = listener
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
        sendMessage("Roll")
    }

    fun manualRollDice(value: Int) {
        if (value in 1..39) {
            sendMessage("MANUAL_ROLL:$value")
        }
    }

    fun sendTaxPayment(playerId: String, amount: Int, taxType: String) {
        val taxMessage = TaxPaymentMessage(
            playerId = playerId,
            amount = amount,
            taxType = taxType
        )
        val json = gson.toJson(taxMessage)
        sendMessage(json)
        Log.d("WebSocket", "Sent tax payment message: $json")
    }
    private fun playSoundEffect(context: Context, fileName: String) {
        try {
            val afd = context.assets.openFd("sounds/$fileName")  // Datei liegt in assets/sounds/
            val mediaPlayer = android.media.MediaPlayer()
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            Log.e("SoundEffect", "Error playing sound: $e")
        }
        playSoundEffect(context, "bell-notification-337658.mp3")
    }


    fun sendGiveUpMessage(userId: String) {
        val payload = mapOf(
            "type" to "GIVE_UP",
            "userId" to userId
        )
        sendMessage(gson.toJson(payload))
    }

    fun sendPullCard(playerId: String, field: Int) {
        val cardType = when (field) {
            2, 17, 33 -> "COMMUNITY_CHEST"
            7, 22, 36 -> "CHANCE"
            else -> return
        }
        val msg = PullCardMessage(playerId = playerId, cardType = cardType)
        val json = gson.toJson(msg)
        sendMessage(json)
        Log.d("WebSocket", "Sent PULL_CARD message: $json")
    }

}