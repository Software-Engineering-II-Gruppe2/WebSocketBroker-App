package at.aau.serg.websocketbrokerdemo.data.messages

data class CheatMessage(
    // fixme extract to enum (see backend comments)
    override val type:String="CHEAT_MESSAGE",
    val playerId:String,
    val message:String
):GameMessage