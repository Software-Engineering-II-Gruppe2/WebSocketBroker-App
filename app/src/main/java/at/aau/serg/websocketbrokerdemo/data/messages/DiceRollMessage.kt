package at.aau.serg.websocketbrokerdemo.data.messages

data class DiceRollMessage(
   override val type:String,
    val playerId: String,
    val value:  Int
):GameMessage
