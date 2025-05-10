package at.aau.serg.websocketbrokerdemo.logic

import android.content.Context
import android.util.Log
import at.aau.serg.websocketbrokerdemo.data.PlayerProfile
import at.aau.serg.websocketbrokerdemo.data.messages.ChatMessage
import at.aau.serg.websocketbrokerdemo.data.messages.PullCardMessage
import at.aau.serg.websocketbrokerdemo.data.messages.TaxPaymentMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameLogicHandler(
    private val context: Context,
    private val sendMessage: (String) -> Unit,
    private val gson: Gson = Gson(),
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val userProfileProvider: UserProfileProvider = at.aau.serg.websocketbrokerdemo.data.FirestoreManager
) {

    fun sendInitMessage() {
        CoroutineScope(coroutineDispatcher).launch {
            val json = getInitPayload()
            if (json != null) {
                sendMessage(json)
                Log.d("GameLogic", "Sent INIT message: $json")
            }
        }
    }

    suspend fun getInitPayload(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid?.let { userId ->
            val profile: PlayerProfile? = userProfileProvider.getUserProfile(userId)
            val name = profile?.name ?: "Unknown"
            val initMessage = mapOf(
                "type" to "INIT",
                "userId" to userId,
                "name" to name
            )
            gson.toJson(initMessage)
        }
    }

    fun sendChatMessage(playerId: String, message: String) {
        val chat = ChatMessage(playerId = playerId, message = message)
        val json = gson.toJson(chat)
        sendMessage(json)
        Log.d("GameLogic", "Sent chat message: $json")
    }

    fun rollDice() {
        sendMessage("Roll")
        Log.d("GameLogic", "Sent: Roll")
    }

    fun manualRollDice(value: Int) {
        if (value in 1..39) {
            val message = "MANUAL_ROLL:$value"
            sendMessage(message)
            Log.d("GameLogic", "Sent: $message")
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
        Log.d("GameLogic", "Sent tax payment message: $json")
    }

    fun sendGiveUpMessage(userId: String) {
        val payload = mapOf(
            "type" to "GIVE_UP",
            "userId" to userId
        )
        val json = gson.toJson(payload)
        sendMessage(json)
        Log.d("GameLogic", "Sent give-up message: $json")
    }

    fun sendPullCard(playerId: String, field: Int) {
        val cardType = when (field) {
            2, 17, 33 -> "COMMUNITY_CHEST"
            7, 22, 36 -> "CHANCE"
            else -> {
                Log.w("GameLogic", "Invalid field for card: $field")
                return
            }
        }

        val msg = PullCardMessage(playerId = playerId, cardType = cardType)
        val json = gson.toJson(msg)
        sendMessage(json)
        Log.d("GameLogic", "Sent PULL_CARD message: $json")
    }

    fun buyProperty(propertyId: Int) {
        val message = "BUY_PROPERTY:$propertyId"
        sendMessage(message)
        Log.d("GameLogic", "Sent: $message")
    }
}
