package at.aau.serg.websocketbrokerdemo.data.messages

data class PullCardMessage(
   override val type: String = "PULL_CARD",
    val playerId: String,
    val cardType: String
):GameMessage
