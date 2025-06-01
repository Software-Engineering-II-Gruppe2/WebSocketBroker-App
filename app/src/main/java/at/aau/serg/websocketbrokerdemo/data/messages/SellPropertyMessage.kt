package at.aau.serg.websocketbrokerdemo.data.messages

data class SellPropertyMessage(
    val type: String,
    val playerId: String,
    val propertyId: Int
) 