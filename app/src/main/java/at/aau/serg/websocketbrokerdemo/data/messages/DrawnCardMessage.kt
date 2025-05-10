package at.aau.serg.websocketbrokerdemo.data.messages

import com.google.gson.JsonObject

data class DrawnCardMessage(
   override val type: String,       // == "CARD_DRAWN"
    val playerId: String,
    val cardType: String,   // "CHANCE" or "COMMUNITY_CHEST"
    val card: JsonObject    // <— raw JSON object
):GameMessage
