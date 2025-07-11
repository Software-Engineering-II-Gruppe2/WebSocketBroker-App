package at.aau.serg.websocketbrokerdemo

import android.content.Context
import android.util.Log
import at.aau.serg.websocketbrokerdemo.data.PlayerMoney
import at.aau.serg.websocketbrokerdemo.data.messages.DealProposalMessage
import at.aau.serg.websocketbrokerdemo.data.messages.DealResponseMessage
import at.aau.serg.websocketbrokerdemo.logic.GameLogicHandler
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.util.Properties

class GameWebSocketClient(
    private val context: Context,
    private var onPlayerInJail: ((String) -> Unit)? = null,
    private val onConnected: () -> Unit,
    private var onMessageReceived: (String) -> Unit,
    private val onDiceRolled: (playerId: String, value: Int, manual: Boolean, isPasch: Boolean) -> Unit,
    private val onGameStateReceived: (List<PlayerMoney>) -> Unit,
    private val onPlayerTurn: (playerId: String) -> Unit,
    private val onPlayerPassedGo: (playerName: String) -> Unit,
    private val onHasWon: (winnerId: String) -> Unit,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val onChatMessageReceived: (playerId: String, message: String) -> Unit,
    private val onCheatMessageReceived: (playerId: String, message: String) -> Unit,
    private val onCardDrawn: (playerId: String, cardType: String, description: String, id: Int) -> Unit,
    private val onTaxPayment: (playerName: String, amount: Int, taxType: String) -> Unit,
    private val onClearChat: () -> Unit,
    private val onDealProposal: (DealProposalMessage) -> Unit,
    private val onDealResponse: (DealResponseMessage) -> Unit,
    private val onGiveUpReceived: (userId: String) -> Unit
    ) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val gson = Gson()

    private var players: List<PlayerMoney> = emptyList()
    private var onPlayerTurnListener: ((String) -> Unit)? = null
    private var propertyBoughtListener: ((String) -> Unit)? = null

    private val logicHandler = GameLogicHandler(
        context = context,
        sendMessage = { sendMessage(it) },
        gson = gson,
        coroutineDispatcher = coroutineDispatcher
    )

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
        onDiceRolled = { pid, v, manual, isPasch -> onDiceRolled(pid, v, manual, isPasch) },
        onCardDrawn = { pid, type, desc, cardId -> onCardDrawn(pid, type, desc, cardId) },
        onChatMessageReceived = { pid, msg -> onChatMessageReceived(pid, msg) },
        onCheatMessageReceived = { pid, msg -> onCheatMessageReceived(pid, msg) },
        onClearChat = onClearChat,
        onHasWon = { winnerId -> onHasWon(winnerId) },
        onMessageReceived = { text -> onMessageReceived(text) },
        onDealProposal = { dealProposal -> onDealProposal(dealProposal) },
        onGiveUpReceived = { givingUpUserId -> onGiveUpReceived(givingUpUserId) },
        onDealResponse = { dealResponse -> onDealResponse(dealResponse) },
        onReset = { logicHandler.sendInitMessage() }
    )

    private val serverUrl: String = loadServerUrl(context)
    private val request: Request = Request.Builder().url(serverUrl).build()

    fun connect() {
        if (webSocket == null) {
            initWebSocket()

        } else {
            Log.d("WebSocket", "Already connected or connection already exists.")
        }
    }

    private fun initWebSocket() {
        webSocket = client.newWebSocket(request, createListener())
    }

    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "Connected to server with response: ${response.message}")
            onConnected();
            logicHandler.sendInitMessage()
        }

        override fun onMessage(ws: WebSocket, text: String) {
            Log.d("WebSocket", "Received: $text")
            try {
                val json = JSONObject(text)
                if (json.optString("type") == "PLAYER_IN_JAIL") {
                    val playerId = json.optString("playerId")
                    onPlayerInJail?.invoke(playerId)
                    return
                }
            } catch (e: Exception) {
                // ignorieren, wenn kein JSON
            }
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
            this@GameWebSocketClient.webSocket = null
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "Error: ${t.message}", t)
            response?.let {
                Log.e("WebSocket", "Response: ${it.code} - ${it.message}")
            }
            this@GameWebSocketClient.webSocket = null
        }
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "Goodbye!")
        webSocket = null
    }

    fun setPropertyBoughtListener(listener: (String) -> Unit) {
        propertyBoughtListener = listener
    }

    fun setOnPlayerTurnListener(listener: (String) -> Unit) {
        onPlayerTurnListener = listener
    }
    private var dealProposalListener: ((DealProposalMessage) -> Unit)? = null
    private var dealResponseListener: ((DealResponseMessage) -> Unit)? = null

    fun setDealProposalListener(callback: (DealProposalMessage) -> Unit) {
        dealProposalListener = callback
    }

    fun setDealResponseListener(callback: (DealResponseMessage) -> Unit) {
        dealResponseListener = callback
    }

    fun setPlayerInJailListener(listener: (String) -> Unit) {
        onPlayerInJail = listener
    }

    // Zugriffe auf GameLogicHandler – optional von außen nutzbar
    fun logic(): GameLogicHandler = logicHandler

    private fun loadServerUrl(context: Context): String {
        val properties = Properties()
        context.assets.open("config.properties").use { input ->
            properties.load(input)
        }
        return properties.getProperty("server.url")
    }
}
