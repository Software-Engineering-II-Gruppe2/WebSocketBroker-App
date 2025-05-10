package at.aau.serg.websocketbrokerdemo.data.messages

data class ChatMessage(
   override val type:String="CHAT_MESSAGE",
    val playerId:String,
    val message:String
):GameMessage
