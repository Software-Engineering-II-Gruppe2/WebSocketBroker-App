package at.aau.serg.websocketbrokerdemo.data

import com.google.gson.JsonObject

data class DrawnCardMessage(
    val type: String,       // == "CARD_DRAWN"
    val playerId: String,
    val cardType: String,   // "CHANCE" or "COMMUNITY_CHEST"
    val card: JsonObject    // <— raw JSON object
)
