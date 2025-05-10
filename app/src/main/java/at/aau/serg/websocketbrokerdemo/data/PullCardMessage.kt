package at.aau.serg.websocketbrokerdemo.data

data class PullCardMessage(
    val type: String = "PULL_CARD",
    val playerId: String,
    val cardType: String
)
