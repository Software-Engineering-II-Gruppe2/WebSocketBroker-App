package at.aau.serg.websocketbrokerdemo.data

data class PlayerMoney(
    val id: String,
    val name: String,
    val money: Int,
    val position: Int,
    val inJail:   Boolean,
    val jailTurns:Int,
    val bot: Boolean
)
